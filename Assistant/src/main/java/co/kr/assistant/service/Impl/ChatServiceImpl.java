package co.kr.assistant.service.Impl;

import co.kr.assistant.model.dto.ChatListDTO;
import co.kr.assistant.model.entity.Assistant;
import co.kr.assistant.model.entity.AssistantChat;
import co.kr.assistant.dao.AssistantChatRepository;
import co.kr.assistant.dao.AssistantRepository;
import co.kr.assistant.service.ChatService;
import co.kr.assistant.util.TokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final AssistantRepository assistantRepository;
    private final AssistantChatRepository assistantChatRepository;

    private final RedisTemplate<String, Object> redisTemplate; // Redis 활용

    @Override
    @Transactional
    public String start(String refreshToken, String ip, String ua) {
        String chatToken = UUID.randomUUID().toString();
        Long usersIdx = null;

        // 1. refreshToken이 있으면 TokenUtil로 유저 식별자 추출
        if (refreshToken != null) {
            usersIdx = TokenUtil.getUserIdFromToken(refreshToken);
        }

        // usersIdx가 null인 상태로 Assistant 객체가 생성 및 저장됩니다.
        Assistant assistant = Assistant.builder()
                .assistantCode(chatToken)
                .usersIdx(usersIdx)
                .ipAddress(ip)
                .userAgent(ua)
                .build();

        assistantRepository.save(assistant);

        // 3. 보안 검증용 Redis 캐싱 (1시간)
        redisTemplate.opsForValue().set("session:" + chatToken, ip + "|" + ua, 1, TimeUnit.HOURS);

        return chatToken;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatListDTO> list(String chatToken) {
        // 1. 토큰으로 Assistant 정보 조회
        Assistant assistant = assistantRepository.findByAssistantCodeAndDel(chatToken, 0)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 채팅 세션입니다."));

        // 2. 대화 내역 조회 및 DTO 변환
        return assistantChatRepository.findByAssistant_AssistantIdxAndDelOrderByCreatedAtDesc(assistant.getAssistantIdx(), 0)
                .stream()
                .map(chat -> ChatListDTO.builder()
                        .question(chat.getQuestion())
                        .answer(chat.getAnswer())
                        .time(chat.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 대화 내용을 DB와 Redis에 저장하는 로직 (예시)
     */
    @Transactional
    public void saveChatMessage(String chatToken, String question, String answer) {
        Assistant assistant = assistantRepository.findByAssistantCodeAndDel(chatToken, 0)
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));

        // MariaDB 저장
        assistantChatRepository.save(AssistantChat.builder()
                .assistant(assistant)
                .question(question)
                .answer(answer)
                .build());

        // 마지막 활동 시간 업데이트
        assistant.updateActivity();

        // (선택) Redis 캐시 갱신 로직 추가 가능
    }
}