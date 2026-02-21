package co.kr.assistant.service.Impl;

import co.kr.assistant.model.dto.ChatListDTO;
import co.kr.assistant.model.entity.Assistant;
import co.kr.assistant.model.entity.AssistantChat;
import co.kr.assistant.dao.AssistantChatRepository;
import co.kr.assistant.dao.AssistantRepository;
import co.kr.assistant.service.ChatService;
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
    private final ObjectMapper objectMapper; // JSON 변환용
    private final ChatClient.Builder chatClientBuilder;

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
                log.error("Redis 데이터 파싱 실패, DB에서 직접 조회합니다.", e);
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

        // 3. 조회된 데이터를 Redis에 저장 (JSON 문자열 형태)
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
        // 1. 세션 확인
        Assistant assistant = assistantRepository.findByAssistantCodeAndDel(chatToken, 0)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 채팅 세션입니다."));

        // 2. 이전 대화 내역 가져오기 (최대 5개)
        // list() 메서드를 호출함으로써 Redis 우선 조회 로직을 그대로 활용함
        List<ChatListDTO> history = this.list(chatToken).stream()
                .limit(5)
                .collect(Collectors.toList());

        // 3. AI에게 전달할 시스템 프롬프트 및 컨텍스트 구성
        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("당신은 유능한 어시스턴트입니다. 다음의 이전 대화 내역을 참고하여 답변해주세요.\n\n");

        // 내역이 최신순(Desc)이므로, 과거->현재 순서로 변경하여 AI에게 전달
        for (int i = history.size() - 1; i >= 0; i--) {
            ChatListDTO h = history.get(i);
            contextBuilder.append("User: ").append(h.getQuestion()).append("\n");
            contextBuilder.append("Assistant: ").append(h.getAnswer()).append("\n\n");
        }

        contextBuilder.append("한글로만 답해줘.");
        String systemPrompt = contextBuilder.toString();

        // 4. AI 응답 생성 (ChatClient 추상화로 Ollama/OpenAI 공용 사용)
        String answer = chatClientBuilder.build()
                .prompt(systemPrompt)
                .user(question)
                .call()
                .content();

        // 5. MariaDB에 대화 내역 저장 (RAG 및 머신러닝 데이터 수집용)
        AssistantChat chat = AssistantChat.builder()
                .prompt(systemPrompt)
                .assistant(assistant)
                .question(question)
                .answer(answer)
                .build();
        assistantChatRepository.save(chat);

        // 6. 유저 활동 시간 업데이트
        assistant.updateActivity();

        // 7. 새로운 대화가 추가되었으므로 Redis 캐시 삭제 (다음 list 호출 시 최 최신화 유도)
        redisTemplate.delete("chat_history:" + chatToken);

        return answer;
    }
}