package co.kr.assistant.service.impl;

import co.kr.assistant.client.RagServiceClient;
import co.kr.assistant.client.UserServiceClient;
import co.kr.assistant.model.dto.chat.ChatDTO;
import co.kr.assistant.model.dto.list.ChatListDTO;
import co.kr.assistant.model.dto.rag.RagItem;
import co.kr.assistant.model.dto.rag.RagReq;
import co.kr.assistant.model.dto.rag.RagRes;
import co.kr.assistant.model.dto.user.UserContextDTO;
import co.kr.assistant.model.entity.Assistant;
import co.kr.assistant.model.entity.AssistantChat;
import co.kr.assistant.dao.AssistantChatRepository;
import co.kr.assistant.dao.AssistantRepository;
import co.kr.assistant.model.vo.PublicDel;
import co.kr.assistant.service.ChatService;
import co.kr.assistant.util.BaseResponse;
import co.kr.assistant.util.PrivacyUtil;
import co.kr.assistant.util.ProfanityFilter;
import co.kr.assistant.util.TokenUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
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
    private final UserServiceClient userServiceClient; // 추가된 통신 클라이언트
    private final Executor aiThreadPoolTaskExecutor;

    private final Map<String, Object> promptTemplates = new HashMap<>();

    @Value("${rag.score.threshold:0.0006}")
    private double minScoreThreshold;

    @Value("${llm.server.timeout:30}")
    private int llmTimeoutSeconds;

    private static final int MAX_SEARCH_KEYWORD_LENGTH = 500;
    private static final int MAX_HISTORY_ANSWER_LENGTH = 200;

    @PostConstruct
    public void initPrompts() {
        try {
            ClassPathResource resource = new ClassPathResource("data/prompts.json");
            try (InputStream is = resource.getInputStream()) {
                Map<String, Object> loaded = objectMapper.readValue(is, new TypeReference<>() {});
                this.promptTemplates.putAll(loaded);
                log.info("프롬프트 로드 완료: {}개 섹션", promptTemplates.size());
            }
        } catch (Exception e) {
            log.error("프롬프트 파일 로드 실패", e);
        }
    }

    @Override
    @Transactional
    public String start(String accessToken, String ip, String ua) {
        String chatToken = UUID.randomUUID().toString();
        Long usersIdx = (accessToken != null) ? TokenUtil.getUserIdFromToken(accessToken) : null;
        Assistant assistant = Assistant.builder().assistantCode(chatToken).usersIdx(usersIdx).ipAddress(ip).userAgent(ua).build();
        assistantRepository.save(assistant);

        try {
            redisTemplate.opsForValue().set("session:" + chatToken, ip + "|" + ua, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("Redis 캐시 저장 실패 (세션): {}", e.getMessage());
        }
        return chatToken;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatListDTO> list(String chatToken) {
        String redisKey = "chat_history:" + chatToken;

        try {
            String cachedData = (String) redisTemplate.opsForValue().get(redisKey);
            if (cachedData != null) {
                return objectMapper.readValue(cachedData, new TypeReference<List<ChatListDTO>>() {});
            }
        } catch (Exception e) {
            log.error("Redis 캐시 조회 실패 (히스토리): {}", e.getMessage());
        }

        Assistant assistant = assistantRepository.findByAssistantCodeAndDel(chatToken, PublicDel.ACTIVE).orElseThrow(() -> new IllegalArgumentException("Invalid Session"));

        List<ChatListDTO> dbChats = assistantChatRepository.findTop5ByAssistant_AssistantIdxAndDelOrderByCreatedAtDesc(assistant.getAssistantIdx(), PublicDel.ACTIVE)
                .stream().map(chat -> ChatListDTO.builder().question(chat.getQuestion()).answer(chat.getAnswer()).time(chat.getCreatedAt()).build()).collect(Collectors.toList());

        if (!dbChats.isEmpty()) {
            try {
                redisTemplate.opsForValue().set(redisKey, objectMapper.writeValueAsString(dbChats), 1, TimeUnit.HOURS);
            } catch (Exception e) {
                log.error("Redis 캐시 저장 실패 (히스토리): {}", e.getMessage());
            }
        }
        return dbChats;
    }

    @Override
    public ChatDTO ask(String chatToken, String question) {
        String lockKey = "lock:chat:" + chatToken;
        boolean lockAcquired = false;

        try {
            try {
                Boolean isLocked = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", 40, TimeUnit.SECONDS);
                if (Boolean.FALSE.equals(isLocked)) {
                    throw new IllegalStateException("현재 답변을 생성 중입니다. 잠시만 기다려주세요.");
                }
                lockAcquired = true;
            } catch (IllegalStateException e) {
                throw e;
            } catch (Exception e) {
                log.error("Redis 장애로 분산 락을 건너뜁니다: {}", e.getMessage());
            }

            Assistant assistant = assistantRepository.findByAssistantCodeAndDel(chatToken, PublicDel.ACTIVE)
                    .orElseThrow(() -> new IllegalArgumentException("세션이 만료되었습니다."));

            if (profanityFilter.getCleanLength(question) <= 1) {
                throw new IllegalArgumentException("질문 내용이 적절하지 않습니다.");
            }

            String processedQuestion = privacyUtil.maskAll(profanityFilter.filter(question));

            AssistantChat chatEntry = AssistantChat.builder()
                    .assistant(assistant)
                    .question(processedQuestion)
                    .build();
            assistantChatRepository.save(chatEntry);

            try {
                // ===== [전략 1 적용] 유저 정보 조회 및 메타데이터 세팅 =====
                String ageGroup = null;
                String gender = null;
                String userContextString = "";

                Long usersIdx = assistant.getUsersIdx();
                if (usersIdx != null) {
                    try {
                        BaseResponse<UserContextDTO> res = userServiceClient.getUserContext(usersIdx);
                        if (res != null && res.getData() != null) {
                            UserContextDTO userContext = res.getData();
                            ageGroup = userContext.getAgeGroup();
                            gender = userContext.getGender();

                            String genderStr = "MALE".equals(gender) ? "남성" : ("FEMALE".equals(gender) ? "여성" : "고객");
                            userContextString = String.format("해당 사용자는 %s %s이며, 쇼핑몰 %s 등급의 %s입니다.",
                                    ageGroup, genderStr, userContext.getMembership(), userContext.getRole());
                        }
                    } catch (Exception e) {
                        log.warn("사용자 개인정보 조회 실패 (비로그인/기본 조건 진행): {}", e.getMessage());
                    }
                }
                // ==========================================================

                String searchKeyword = processedQuestion;
                if (searchKeyword.length() > MAX_SEARCH_KEYWORD_LENGTH) {
                    searchKeyword = searchKeyword.substring(0, MAX_SEARCH_KEYWORD_LENGTH);
                }

                // RagReq 객체를 빌더로 생성하여 메타데이터 포함
                RagReq ragReq = RagReq.builder()
                        .q(searchKeyword)
                        .topK(5)
                        .ageGroup(ageGroup) // 비로그인이면 null
                        .gender(gender)     // 비로그인이면 null
                        .build();

                RagRes ragRes = ragServiceClient.searchKnowledge(ragReq);
                String intent = (ragRes != null && ragRes.getIntent() != null) ? ragRes.getIntent() : "GENERAL_CHAT";

                List<Map<String, Object>> responseDataList = new ArrayList<>();
                String retrievedContext = "";

                if (ragRes != null && ragRes.getResults() != null) {
                    Set<String> uniqueContent = new HashSet<>();
                    List<RagItem> validItems = new ArrayList<>();

                    for (RagItem item : ragRes.getResults()) {
                        if (item.getScore() != null && item.getScore() >= minScoreThreshold) {
                            String contentKey = (item.getProductsName() != null) ? item.getProductsName() :
                                    (item.getAnswer() != null) ? item.getAnswer() : item.getText();

                            if (contentKey != null && uniqueContent.add(contentKey.trim())) {
                                validItems.add(item);
                            }
                        }
                    }

                    if (!validItems.isEmpty()) {
                        StringBuilder ctxBuilder = new StringBuilder();
                        for (int i = 0; i < validItems.size(); i++) {
                            RagItem item = validItems.get(i);
                            ctxBuilder.append(formatRagItemForLLM(item, i + 1)).append("\n");
                            responseDataList.add(extractDataForFrontend(item));
                        }
                        retrievedContext = ctxBuilder.toString();
                    }
                }

                if (retrievedContext.isEmpty()) {
                    retrievedContext = "검색된 내부 지식이 없습니다. [MANDATORY POLICIES]의 'fallback_rules'를 엄격히 준수하십시오.";
                }

                // 파라미터로 userContextString과 ageGroup 추가 전달
                String finalSystemPrompt = buildSystemPrompt(intent, retrievedContext, chatToken, userContextString, ageGroup);

                long startTime = System.currentTimeMillis();

                ChatResponse chatResponse = CompletableFuture.supplyAsync(() ->
                                chatClientBuilder.build()
                                        .prompt(finalSystemPrompt)
                                        .user(processedQuestion)
                                        .call()
                                        .chatResponse()
                        , aiThreadPoolTaskExecutor).orTimeout(llmTimeoutSeconds, TimeUnit.SECONDS).join();

                long duration = System.currentTimeMillis() - startTime;

                String answer = chatResponse.getResult().getOutput().getText();
                Usage usage = chatResponse.getMetadata().getUsage();

                log.info("AI 응답 완료 - 소요시간: {}ms, 토큰사용: [Prompt: {}, Completion: {}, Total: {}]",
                        duration, usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());

                chatEntry.updateResponse(
                        finalSystemPrompt,
                        answer,
                        (int)usage.getPromptTokens(),
                        (int)usage.getCompletionTokens(),
                        (int)usage.getTotalTokens(),
                        duration
                );
                assistantChatRepository.save(chatEntry);

                assistant.updateActivity();
                assistantRepository.save(assistant);

                try {
                    redisTemplate.delete("chat_history:" + chatToken);
                    redisTemplate.expire("session:" + chatToken, 1, TimeUnit.HOURS);
                } catch (Exception e) {
                    log.error("Redis 갱신 실패: {}", e.getMessage());
                }

                return ChatDTO.builder()
                        .answer(answer)
                        .data(responseDataList)
                        .build();

            } catch (Exception e) {
                log.error("채팅 처리 중 오류 발생: ", e);
                chatEntry.updateResponse("ERROR_LOG", "AI 모델 장애 발생", 0, 0, 0, 0L);
                assistantChatRepository.save(chatEntry);
                throw new RuntimeException("현재 요청을 처리할 수 없습니다.");
            }
        } finally {
            if (lockAcquired) {
                try {
                    redisTemplate.delete(lockKey);
                } catch (Exception e) {
                    log.error("Redis 락 해제 실패: {}", e.getMessage());
                }
            }
        }
    }

    // 메서드 시그니처 수정 및 상태별 대화형 프로파일링 적용
    private String buildSystemPrompt(String intent, String retrievedContext, String chatToken, String userContextString, String ageGroup) {
        Map<String, Object> common = (Map<String, Object>) promptTemplates.get("COMMON");
        Map<String, Object> intentConfig = (Map<String, Object>) promptTemplates.getOrDefault(intent, promptTemplates.get("GENERAL_CHAT"));

        StringBuilder pb = new StringBuilder();
        pb.append("# MANDATORY POLICIES\n");
        pb.append("- RESPONSE LANGUAGE: MUST match the user's input language.\n");
        pb.append("- NO LINKS IN ANSWER: NEVER include any URLs or clickable links in your 'answer'. Link information is already provided in the structured data.\n");
        pb.append("- PRIVACY PROTECTION: NEVER disclose raw masking tags like '<이름_숨김>'. Instead, use polite natural terms like '고객님'.\n");

        ((List<String>) common.get("language_rules")).forEach(rule -> pb.append("- ").append(rule).append("\n"));
        ((List<String>) common.get("safety_rules")).forEach(rule -> pb.append("- ").append(rule).append("\n"));
        ((List<String>) common.get("fallback_rules")).forEach(rule -> pb.append("- ").append(rule).append("\n"));

        pb.append("\n## YOUR IDENTITY\n").append(intentConfig.get("persona")).append("\n\n");

        // ===== [전략 3 적용] 유저 컨텍스트 주입 =====
        pb.append("## [USER CONTEXT & PERSONALIZATION RULE]\n");
        if (ageGroup != null) {
            pb.append(userContextString).append("\n");
            pb.append("- 당신은 위의 사용자 프로필(연령대, 성별, 등급)을 분석하여 가장 적합한 상품을 최우선으로 추천해야 합니다.\n");
        } else {
            pb.append("- 현재 사용자는 비로그인(게스트) 상태이거나 연령/성별 정보가 없습니다.\n");
            pb.append("- [중요] 상품 추천 시 사용자의 정보가 없으므로 무난하고 대중적인 베스트셀러 위주로 제안하십시오.\n");
            pb.append("- [중요] 답변의 마지막에 자연스럽게 '혹시 찾으시는 연령대나 선호하는 스타일이 있으신가요?'라고 질문하여 사용자의 니즈를 구체화하도록 유도하십시오.\n");
        }
        pb.append("\n");
        // ============================================

        pb.append("## [REFERENCE KNOWLEDGE]\n").append(retrievedContext).append("\n\n");

        List<ChatListDTO> history = this.list(chatToken);

        pb.append("## [CONVERSATION HISTORY]\n");
        for (int i = history.size() - 1; i >= 0; i--) {
            ChatListDTO h = history.get(i);
            pb.append("User: ").append(h.getQuestion()).append("\n");
            String prevAnswer = h.getAnswer();
            if (prevAnswer != null && prevAnswer.length() > MAX_HISTORY_ANSWER_LENGTH) {
                prevAnswer = prevAnswer.substring(0, MAX_HISTORY_ANSWER_LENGTH) + "...(중략)";
            }
            pb.append("Assistant: ").append(prevAnswer).append("\n\n");
        }
        return pb.toString();
    }

    private Map<String, Object> extractDataForFrontend(RagItem item) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (item.getProductsName() != null && item.getTitle() == null) {
            map.put("type", "PRODUCT");
            map.put("Link", item.getLink());
            map.put("Products_Name", item.getProductsName());
            map.put("Description", item.getDescription());
            map.put("Price", item.getPrice());
            map.put("Sale_Price", item.getSalePrice());
            map.put("View_Count", item.getViewCount());
            map.put("Review", item.getReviewCount());
        } else if (item.getTitle() != null && item.getAnswer() != null) {
            map.put("type", "QNA");
            map.put("Link", item.getLink());
            if (item.getProductsName() != null) map.put("Products_Name", item.getProductsName());
            map.put("Title", item.getTitle());
            map.put("Question", item.getQuestion() != null ? item.getQuestion() : item.getText());
            map.put("Answer", item.getAnswer());
            map.put("Created_at", item.getCreatedAt());
        } else if (item.getTitle() != null && item.getContent() != null) {
            map.put("type", "NOTICE");
            map.put("Link", item.getLink());
            map.put("Title", item.getTitle());
            map.put("Content", item.getContent());
            map.put("Published_at", item.getPublishedAt() != null ? item.getPublishedAt() : item.getCreatedAt());
        } else {
            map.put("type", "GENERAL");
            map.put("Text", item.getText());
        }
        return map;
    }

    private String formatRagItemForLLM(RagItem item, int index) {
        StringBuilder sb = new StringBuilder();
        sb.append("[참고자료 ").append(index).append("]\n");
        if (item.getProductsName() != null) sb.append("- 상품명: ").append(item.getProductsName()).append("\n");
        if (item.getText() != null) sb.append("- 내용: ").append(item.getText()).append("\n");
        if (item.getAnswer() != null) sb.append("- 답변: ").append(item.getAnswer()).append("\n");
        return sb.toString();
    }
}