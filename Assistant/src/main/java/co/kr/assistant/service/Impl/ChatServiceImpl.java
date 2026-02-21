package co.kr.assistant.service.Impl;

import co.kr.assistant.client.RagServiceClient;
import co.kr.assistant.model.dto.list.ChatListDTO;
import co.kr.assistant.model.dto.rag.RagRes;
import co.kr.assistant.model.entity.Assistant;
import co.kr.assistant.model.entity.AssistantChat;
import co.kr.assistant.dao.AssistantChatRepository;
import co.kr.assistant.dao.AssistantRepository;
import co.kr.assistant.service.ChatService;
import co.kr.assistant.util.PrivacyUtil; // 개인정보 마스킹 유틸
import co.kr.assistant.util.ProfanityFilter; // 욕설 필터 유틸
import co.kr.assistant.util.TokenUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
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

    // 유틸리티 주입
    private final ProfanityFilter profanityFilter;
    private final PrivacyUtil privacyUtil;

    private final RagServiceClient ragServiceClient;

    @Override
    @Transactional
    public String start(String accessToken, String ip, String ua) {
        String chatToken = UUID.randomUUID().toString();
        Long usersIdx = null;

        if (accessToken != null) {
            usersIdx = TokenUtil.getUserIdFromToken(accessToken);
        }

        Assistant assistant = Assistant.builder()
                .assistantCode(chatToken)
                .usersIdx(usersIdx)
                .ipAddress(ip)
                .userAgent(ua)
                .build();

        assistantRepository.save(assistant);

        // 보안 검증용 세션 캐싱 (1시간)
        redisTemplate.opsForValue().set("session:" + chatToken, ip + "|" + ua, 1, TimeUnit.HOURS);

        return chatToken;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatListDTO> list(String chatToken) {
        String redisKey = "chat_history:" + chatToken;

        // 1. Redis에서 캐시 데이터 확인
        String cachedData = (String) redisTemplate.opsForValue().get(redisKey);

        if (cachedData != null) {
            try {
                log.info("Redis 캐시에서 채팅 리스트를 반환합니다.");
                return objectMapper.readValue(cachedData, new TypeReference<List<ChatListDTO>>() {});
            } catch (JsonProcessingException e) {
                log.error("Redis 데이터 파싱 실패", e);
            }
        }

        // 2. Redis에 없으면 MariaDB에서 조회
        Assistant assistant = assistantRepository.findByAssistantCodeAndDel(chatToken, 0)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 채팅 세션입니다."));

        List<ChatListDTO> dbChats = assistantChatRepository.findByAssistant_AssistantIdxAndDelOrderByCreatedAtDesc(assistant.getAssistantIdx(), 0)
                .stream()
                .map(chat -> ChatListDTO.builder()
                        .question(chat.getQuestion())
                        .answer(chat.getAnswer())
                        .time(chat.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        // 3. 조회된 데이터를 Redis에 저장
        if (!dbChats.isEmpty()) {
            try {
                String jsonChats = objectMapper.writeValueAsString(dbChats);
                redisTemplate.opsForValue().set(redisKey, jsonChats, 1, TimeUnit.HOURS);
            } catch (JsonProcessingException e) {
                log.error("Redis 저장용 JSON 변환 실패", e);
            }
        }

        return dbChats;
    }

    @Override
    @Transactional
    public String ask(String chatToken, String question) {
        // 1. 세션 확인 및 유효성 검사
        Assistant assistant = assistantRepository.findByAssistantCodeAndDel(chatToken, 0)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 채팅 세션입니다."));

        int cleanLength = profanityFilter.getCleanLength(question);
        if (cleanLength <= 1) {
            throw new IllegalArgumentException("질문 내용이 너무 짧거나 부적절한 표현만 포함되어 있습니다.");
        }

        // 2. 현재 질문 가공 (욕설 필터 및 개인정보 마스킹)
        String processedQuestion = privacyUtil.maskAll(profanityFilter.filter(question));

        // 3. [추가] Python RAG 서버를 통해 관련 지식(Context) 조회
        // 3060 Ti에서 BGE-M3 + Reranker 로직이 작동하여 최상의 지식을 선별합니다.
        List<RagRes> searchResults = ragServiceClient.searchKnowledge(processedQuestion, 5);
        String retrievedContext = searchResults.stream()
                .map(RagRes::getText)
                .collect(Collectors.joining("\n\n"));

        // 4. 시스템 프롬프트 구성 (지식 기반 답변 지침 추가)
        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("당신은 유능한 보안 챗봇 어시스턴트입니다. 아래 [참고 지식]을 바탕으로 질문에 답하세요.\n");
        contextBuilder.append("1. 문장에 포함된 <PROFANITY_MASK>는 부적절한 단어가 가려진 것이니 무시하고 자연스럽게 답하세요.\n");
        contextBuilder.append("2. <PRIVACY_MASK>로 가려진 부분은 답변에서 직접 언급하거나 '없다'고 말하지 말고, 문맥에 맞춰 자연스럽게 생략하거나 대체하세요.\n");
        contextBuilder.append("3. [참고 지식]에 관련 정보가 있다면 우선적으로 활용하여 정확하게 답변하세요.\n\n");

        contextBuilder.append("[참고 지식]\n");
        contextBuilder.append(retrievedContext.isEmpty() ? "관련된 내부 지식을 찾지 못했습니다." : retrievedContext).append("\n\n");

        // 5. 이전 대화 내역 가져오기 (기존 로직 유지)
        List<ChatListDTO> history = this.list(chatToken).stream().limit(5).collect(Collectors.toList());
        contextBuilder.append("[이전 대화 내역]\n");
        for (int i = history.size() - 1; i >= 0; i--) {
            ChatListDTO h = history.get(i);
            contextBuilder.append("User: ").append(privacyUtil.maskAll(profanityFilter.filter(h.getQuestion()))).append("\n");
            contextBuilder.append("Assistant: ").append(privacyUtil.maskAll(profanityFilter.filter(h.getAnswer()))).append("\n\n");
        }

        contextBuilder.append("한글로만 답해줘.");
        String systemPrompt = contextBuilder.toString();

        log.info("AI에게 전달되는 시스템 프롬프트: {}", systemPrompt);
        log.info("AI에게 전달되는 질문: {}", question);

        // 6. AI 호출 및 결과 저장
        String answer = chatClientBuilder.build()
                .prompt(systemPrompt)
                .user(processedQuestion)
                .call()
                .content();

        AssistantChat chat = AssistantChat.builder()
                .prompt(systemPrompt)
                .assistant(assistant)
                .question(question)
                .answer(answer)
                .build();
        assistantChatRepository.save(chat);

        assistant.updateActivity();
        redisTemplate.delete("chat_history:" + chatToken);

        return answer;
    }
}