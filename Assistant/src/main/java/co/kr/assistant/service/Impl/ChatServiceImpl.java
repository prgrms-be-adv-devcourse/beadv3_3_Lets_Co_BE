package co.kr.assistant.service.Impl;

import co.kr.assistant.client.RagServiceClient;
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
    @Transactional
    public String ask(String chatToken, String question) {
        // 1. 세션 확인 및 질문 정제
        Assistant assistant = assistantRepository.findByAssistantCodeAndDel(chatToken, 0)
                .orElseThrow(() -> new IllegalArgumentException("세션이 만료되었습니다."));

        if (profanityFilter.getCleanLength(question) <= 1) {
            throw new IllegalArgumentException("질문 내용이 적절하지 않습니다.");
        }

        String processedQuestion = privacyUtil.maskAll(profanityFilter.filter(question));

        // 2. RAG 서버 호출 및 검색 결과 필터링
        RagRes ragRes = ragServiceClient.searchKnowledge(processedQuestion, 5);
        String intent = (ragRes != null) ? ragRes.getIntent() : "GENERAL_CHAT";

        // 유사도 점수가 임계값(0.1) 이상인 데이터만 추출하여 환각 현상 방지
        String retrievedContext = (ragRes != null && ragRes.getResults() != null)
                ? ragRes.getResults().stream()
                .filter(item -> item.getScore() >= MIN_SCORE_THRESHOLD)
                .map(RagItem::getText)
                .collect(Collectors.joining("\n\n"))
                : "";

        if (retrievedContext.isEmpty()) {
            retrievedContext = "관련된 직접적인 지식을 찾지 못했습니다. 일반적인 가이드라인에 따라 답변하십시오.";
        }

        // 3. 고도화된 시스템 프롬프트 조립
        Map<String, Object> common = (Map<String, Object>) promptTemplates.get("COMMON");
        Map<String, Object> intentConfig = (Map<String, Object>) promptTemplates.getOrDefault(intent, promptTemplates.get("GENERAL_CHAT"));

        StringBuilder pb = new StringBuilder();

        // [MANDATORY] 언어 및 안전 정책
        pb.append("# MANDATORY POLICIES\n");
        pb.append("- RESPONSE LANGUAGE: MUST match the user's input language.\n");
        ((List<String>) common.get("language_rules")).forEach(rule -> pb.append("- ").append(rule).append("\n"));
        ((List<String>) common.get("safety_rules")).forEach(rule -> pb.append("- ").append(rule).append("\n"));
        ((List<String>) common.get("fallback_rules")).forEach(rule -> pb.append("- ").append(rule).append("\n"));

        // [PERSONA] 역할 정의
        pb.append("\n## YOUR IDENTITY\n").append(intentConfig.get("persona")).append("\n\n");

        // [INSTRUCTIONS] 상세 지침 및 제약 사항
        pb.append("## OPERATIONAL GUIDELINES\n");
        if (intentConfig.containsKey("instructions")) {
            ((List<String>) intentConfig.get("instructions")).forEach(i -> pb.append("- ").append(i).append("\n"));
        }
        if (intentConfig.containsKey("negative_constraints")) {
            pb.append("### NEVER DO THESE:\n");
            ((List<String>) intentConfig.get("negative_constraints")).forEach(nc -> pb.append("- ").append(nc).append("\n"));
        }

        // [CoT] 사고 과정 주입
        if (intentConfig.containsKey("cot_steps")) {
            pb.append("\n## THINKING PROCESS\n");
            ((List<String>) intentConfig.get("cot_steps")).forEach(step -> pb.append(step).append("\n"));
        }

        // [EXAMPLES] Few-shot 예시
        if (intentConfig.containsKey("examples")) {
            pb.append("\n## REFERENCE EXAMPLES\n");
            List<Map<String, String>> examples = (List<Map<String, String>>) intentConfig.get("examples");
            examples.forEach(ex -> pb.append("Q: ").append(ex.get("q")).append("\nA: ").append(ex.get("a")).append("\n\n"));
        }

        // [KNOWLEDGE] 필터링된 지식 및 대화 내역
        pb.append("## [REFERENCE KNOWLEDGE]\n").append(retrievedContext).append("\n\n");

        List<ChatListDTO> history = this.list(chatToken).stream().limit(5).collect(Collectors.toList());
        pb.append("## [CONVERSATION HISTORY]\n");
        for (int i = history.size() - 1; i >= 0; i--) {
            ChatListDTO h = history.get(i);
            pb.append("User: ").append(privacyUtil.maskAll(profanityFilter.filter(h.getQuestion()))).append("\n");
            pb.append("Assistant: ").append(privacyUtil.maskAll(profanityFilter.filter(h.getAnswer()))).append("\n\n");
        }

        pb.append("\nFinal Step: Apply self-correction to ensure the response follows all constraints.");
        String finalSystemPrompt = pb.toString();

        log.info("Final System Prompt for ChatToken {}: \n{}", chatToken, finalSystemPrompt);
        log.info("User Question after processing: {}", processedQuestion);

        // 4. AI 호출 및 결과 저장
        String answer = chatClientBuilder.build()
                .prompt(finalSystemPrompt)
                .user(processedQuestion)
                .call()
                .content();

        assistantChatRepository.save(AssistantChat.builder()
                .prompt(finalSystemPrompt).assistant(assistant).question(question).answer(answer).build());

        assistant.updateActivity();
        redisTemplate.delete("chat_history:" + chatToken);

        return answer;
    }
}