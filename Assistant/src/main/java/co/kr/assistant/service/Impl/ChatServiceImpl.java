package co.kr.assistant.service.Impl;

import co.kr.assistant.client.RagServiceClient;
import co.kr.assistant.model.dto.chat.ChatDTO;
import co.kr.assistant.model.dto.list.ChatListDTO;
import co.kr.assistant.model.dto.rag.RagItem;
import co.kr.assistant.model.dto.rag.RagRes;
import co.kr.assistant.model.entity.Assistant;
import co.kr.assistant.model.entity.AssistantChat;
import co.kr.assistant.dao.AssistantChatRepository;
import co.kr.assistant.dao.AssistantRepository;
import co.kr.assistant.service.ChatService;
import co.kr.assistant.util.PrivacyUtil;
import co.kr.assistant.util.ProfanityFilter;
import co.kr.assistant.util.TokenUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final AssistantRepository assistantRepository;
    private final AssistantChatRepository assistantChatRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final ChatClient.Builder chatClientBuilder;
    private final ProfanityFilter profanityFilter;
    private final PrivacyUtil privacyUtil;
    private final RagServiceClient ragServiceClient;

    private final Map<String, Object> promptTemplates = new HashMap<>();

    // RAG 데이터의 신뢰도를 결정하는 최소 점수 임계값
    private static final double MIN_SCORE_THRESHOLD = 0.0006;

    @PostConstruct
    public void initPrompts() {
        try {
            ClassPathResource resource = new ClassPathResource("data/prompts.json");
            try (InputStream is = resource.getInputStream()) {
                Map<String, Object> loaded = objectMapper.readValue(is, new TypeReference<>() {});
                this.promptTemplates.putAll(loaded);
                log.info("GutJJeu 고도화 프롬프트 로드 완료: {}개 섹션", promptTemplates.size());
            }
        } catch (Exception e) {
            log.error("프롬프트 파일 로드 실패 (src/main/resources/data/prompts.json)", e);
        }
    }

    @Override
    @Transactional
    public String start(String accessToken, String ip, String ua) {
        String chatToken = UUID.randomUUID().toString();
        Long usersIdx = (accessToken != null) ? TokenUtil.getUserIdFromToken(accessToken) : null;
        Assistant assistant = Assistant.builder().assistantCode(chatToken).usersIdx(usersIdx).ipAddress(ip).userAgent(ua).build();
        assistantRepository.save(assistant);
        redisTemplate.opsForValue().set("session:" + chatToken, ip + "|" + ua, 1, TimeUnit.HOURS);
        return chatToken;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatListDTO> list(String chatToken) {
        String redisKey = "chat_history:" + chatToken;
        String cachedData = (String) redisTemplate.opsForValue().get(redisKey);
        if (cachedData != null) {
            try { return objectMapper.readValue(cachedData, new TypeReference<List<ChatListDTO>>() {}); }
            catch (JsonProcessingException e) { log.error("Cache parsing error", e); }
        }
        Assistant assistant = assistantRepository.findByAssistantCodeAndDel(chatToken, 0).orElseThrow(() -> new IllegalArgumentException("Invalid Session"));
        List<ChatListDTO> dbChats = assistantChatRepository.findByAssistant_AssistantIdxAndDelOrderByCreatedAtDesc(assistant.getAssistantIdx(), 0)
                .stream().map(chat -> ChatListDTO.builder().question(chat.getQuestion()).answer(chat.getAnswer()).time(chat.getCreatedAt()).build()).collect(Collectors.toList());
        if (!dbChats.isEmpty()) {
            try { redisTemplate.opsForValue().set(redisKey, objectMapper.writeValueAsString(dbChats), 1, TimeUnit.HOURS); }
            catch (JsonProcessingException e) { log.error("Cache save error", e); }
        }
        return dbChats;
    }

    @Override
    // 🚨 속도 및 안정성을 위해 @Transactional 제거 (외부 통신 중 DB 커넥션 점유 방지)
    public ChatDTO ask(String chatToken, String question) {
        // 1. 세션 확인 및 질문 정제
        Assistant assistant = assistantRepository.findByAssistantCodeAndDel(chatToken, 0)
                .orElseThrow(() -> new IllegalArgumentException("세션이 만료되었습니다."));

        if (profanityFilter.getCleanLength(question) <= 1) {
            throw new IllegalArgumentException("질문 내용이 적절하지 않습니다.");
        }

        String processedQuestion = privacyUtil.maskAll(profanityFilter.filter(question));

        // 2. RAG 서버 호출 및 검색 결과 필터링
        RagRes ragRes = ragServiceClient.searchKnowledge(processedQuestion, 5);
        String intent = (ragRes != null && ragRes.getIntent() != null) ? ragRes.getIntent() : "GENERAL_CHAT";

        // LLM 프롬프트용 텍스트와 프론트엔드용 JSON 맵 리스트를 분리해서 준비
        List<Map<String, Object>> responseDataList = new ArrayList<>();
        String retrievedContext = "";

        if (ragRes != null && ragRes.getResults() != null) {
            List<RagItem> validItems = ragRes.getResults().stream()
                    .filter(item -> item.getScore() != null && item.getScore() >= MIN_SCORE_THRESHOLD)
                    .collect(Collectors.toList());

            if (!validItems.isEmpty()) {
                StringBuilder ctxBuilder = new StringBuilder();
                for (int i = 0; i < validItems.size(); i++) {
                    RagItem item = validItems.get(i);
                    // LLM이 참고할 텍스트 문맥 생성
                    ctxBuilder.append(formatRagItemForLLM(item, i + 1)).append("\n");
                    // 프론트엔드에 내려줄 깔끔한 맞춤형 Map 추출
                    responseDataList.add(extractDataForFrontend(item));
                }
                retrievedContext = ctxBuilder.toString();
            }
        }

        if (retrievedContext.isEmpty()) {
            retrievedContext = "관련된 직접적인 지식을 찾지 못했습니다. 일반적인 가이드라인에 따라 답변하십시오.";
        }

        // 3. 고도화된 시스템 프롬프트 조립
        Map<String, Object> common = (Map<String, Object>) promptTemplates.get("COMMON");
        Map<String, Object> intentConfig = (Map<String, Object>) promptTemplates.getOrDefault(intent, promptTemplates.get("GENERAL_CHAT"));

        StringBuilder pb = new StringBuilder();

        pb.append("# MANDATORY POLICIES\n");
        pb.append("- RESPONSE LANGUAGE: MUST match the user's input language.\n");
        ((List<String>) common.get("language_rules")).forEach(rule -> pb.append("- ").append(rule).append("\n"));
        ((List<String>) common.get("safety_rules")).forEach(rule -> pb.append("- ").append(rule).append("\n"));
        ((List<String>) common.get("fallback_rules")).forEach(rule -> pb.append("- ").append(rule).append("\n"));

        pb.append("\n## YOUR IDENTITY\n").append(intentConfig.get("persona")).append("\n\n");

        pb.append("## OPERATIONAL GUIDELINES\n");
        if (intentConfig.containsKey("instructions")) {
            ((List<String>) intentConfig.get("instructions")).forEach(i -> pb.append("- ").append(i).append("\n"));
        }
        if (intentConfig.containsKey("negative_constraints")) {
            pb.append("### NEVER DO THESE:\n");
            ((List<String>) intentConfig.get("negative_constraints")).forEach(nc -> pb.append("- ").append(nc).append("\n"));
        }

        if (intentConfig.containsKey("cot_steps")) {
            pb.append("\n## THINKING PROCESS\n");
            ((List<String>) intentConfig.get("cot_steps")).forEach(step -> pb.append(step).append("\n"));
        }

        if (intentConfig.containsKey("examples")) {
            pb.append("\n## REFERENCE EXAMPLES\n");
            List<Map<String, String>> examples = (List<Map<String, String>>) intentConfig.get("examples");
            examples.forEach(ex -> pb.append("Q: ").append(ex.get("q")).append("\nA: ").append(ex.get("a")).append("\n\n"));
        }

        pb.append("## [REFERENCE KNOWLEDGE]\n").append(retrievedContext).append("\n\n");

        List<ChatListDTO> history = this.list(chatToken).stream().limit(5).collect(Collectors.toList());
        pb.append("## [CONVERSATION HISTORY]\n");
        for (int i = history.size() - 1; i >= 0; i--) {
            ChatListDTO h = history.get(i);
            // 🚨 필터링이 영어 문맥을 부수지 않도록, 대화 기록에는 마스킹을 제외하여 원본을 유지합니다.
            pb.append("User: ").append(h.getQuestion()).append("\n");
            pb.append("Assistant: ").append(h.getAnswer()).append("\n\n");
        }

        pb.append("\nFinal Step: Apply self-correction to ensure the response follows all constraints.");
        String finalSystemPrompt = pb.toString();

        log.info("Final System Prompt for ChatToken {}: \n{}", chatToken, finalSystemPrompt);
        log.info("User Question after processing: {}", processedQuestion);

        // 4. AI 호출 및 결과 저장 (DB 커넥션을 물고 있지 않으므로 안전함)
        String answer = chatClientBuilder.build()
                .prompt(finalSystemPrompt)
                .user(processedQuestion)
                .call()
                .content();

        // 5. DB 저장 (명시적으로 처리)
        assistantChatRepository.save(AssistantChat.builder()
                .prompt(finalSystemPrompt).assistant(assistant).question(question).answer(answer).build());

        assistant.updateActivity();
        assistantRepository.save(assistant); // @Transactional이 없으므로 직접 save 호출

        redisTemplate.delete("chat_history:" + chatToken);

        // 프론트엔드가 요구한 궁극의 JSON 포맷 리턴
        return ChatDTO.builder()
                .answer(answer)
                .data(responseDataList)
                .build();
    }

    /**
     * [프론트엔드 제공용]
     * RagItem의 전체 필드 중 데이터의 유형(Product, Notice, QnA)에 따라
     * 꼭 필요한 알짜배기 필드들만 골라서 맞춤형 JSON(Map)으로 변환합니다.
     */
    private Map<String, Object> extractDataForFrontend(RagItem item) {
        // 순서를 보장하기 위해 LinkedHashMap 사용
        Map<String, Object> map = new LinkedHashMap<>();

        // 1. 상품 (Product) 데이터인 경우
        if (item.getProductsName() != null && item.getTitle() == null) {
            map.put("type", "PRODUCT"); // 프론트엔드 식별용 타입
            map.put("Link", item.getLink());
            map.put("Products_Name", item.getProductsName());
            map.put("Description", item.getDescription());
            map.put("Price", item.getPrice());
            map.put("Sale_Price", item.getSalePrice());
            map.put("View_Count", item.getViewCount());
            map.put("Review", item.getReviewCount());
        }
        // 2. 문의 내역 (QnA) 데이터인 경우
        else if (item.getTitle() != null && item.getAnswer() != null) {
            map.put("type", "QNA");
            map.put("Link", item.getLink());
            if (item.getProductsName() != null) {
                map.put("Products_Name", item.getProductsName());
            }
            map.put("Title", item.getTitle());
            map.put("Question", item.getQuestion() != null ? item.getQuestion() : item.getText());
            map.put("Answer", item.getAnswer());
            map.put("Created_at", item.getCreatedAt());
        }
        // 3. 공지사항 (Notice) 데이터인 경우
        else if (item.getTitle() != null && item.getContent() != null) {
            map.put("type", "NOTICE");
            map.put("Link", item.getLink());
            map.put("Title", item.getTitle());
            map.put("Content", item.getContent());
            map.put("Published_at", item.getPublishedAt() != null ? item.getPublishedAt() : item.getCreatedAt());
        }
        // 4. 그 외 기본 텍스트 데이터인 경우
        else {
            map.put("type", "GENERAL");
            map.put("Text", item.getText());
        }

        return map;
    }

    /**
     * [LLM 제공용]
     * 파이썬에서 온 JSON 데이터를 LLM이 읽기 쉽게 포맷팅
     */
    private String formatRagItemForLLM(RagItem item, int index) {
        StringBuilder sb = new StringBuilder();
        sb.append("[참고자료 ").append(index).append("]\n");

        if (item.getLink() != null) {
            sb.append("- 바로가기 링크: ").append(item.getLink()).append("\n");
        }

        if (item.getProductsName() != null && item.getTitle() == null) {
            sb.append("- 데이터 종류: 상품 정보\n");
            sb.append("- 상품명: ").append(item.getProductsName()).append("\n");
            if (item.getCategoryName() != null) sb.append("- 카테고리/IP: ").append(item.getCategoryName()).append(" / ").append(item.getIpName()).append("\n");
            if (item.getDescription() != null) sb.append("- 설명: ").append(item.getDescription()).append("\n");

            if (item.getPrice() != null) {
                sb.append("- 가격: ").append(item.getPrice()).append("원");
                if (item.getSalePrice() != null && !item.getSalePrice().toString().isEmpty()) {
                    sb.append(" (현재 할인가: ").append(item.getSalePrice()).append("원)");
                }
                sb.append("\n");
            }

            if (item.getOptions() != null && !item.getOptions().isEmpty()) {
                sb.append("- 선택 가능 옵션:\n");
                for (RagItem.RagOption opt : item.getOptions()) {
                    sb.append("  * ").append(opt.getOptionName()).append(" (+").append(opt.getOptionPrice()).append("원)\n");
                }
            }
        } else if (item.getTitle() != null && item.getAnswer() != null) {
            sb.append("- 데이터 종류: 쇼핑몰 문의내역(QnA)\n");
            sb.append("- 관련 상품: ").append(item.getProductsName() != null ? item.getProductsName() : "일반 문의").append("\n");
            sb.append("- 문의 제목: ").append(item.getTitle()).append("\n");
            String questionText = item.getQuestion() != null ? item.getQuestion() : item.getText();
            sb.append("- 고객 질문 원문: ").append(questionText).append("\n");
            String answer = item.getAnswer();
            sb.append("- 관리자 답변: ").append((answer == null || answer.trim().isEmpty()) ? "아직 답변이 등록되지 않았습니다." : answer).append("\n");
        } else if (item.getTitle() != null && item.getContent() != null) {
            sb.append("- 데이터 종류: 쇼핑몰 공지사항\n");
            sb.append("- 공지 제목: ").append(item.getTitle()).append("\n");
            sb.append("- 작성 일자: ").append(item.getPublishedAt() != null ? item.getPublishedAt() : item.getCreatedAt()).append("\n");
            sb.append("- 공지 내용: ").append(item.getContent()).append("\n");
        } else {
            sb.append("- 내용: ").append(item.getText()).append("\n");
        }

        return sb.toString();
    }
}