package co.kr.assistant.dao;

import co.kr.assistant.model.entity.AssistantChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssistantChatRepository extends JpaRepository<AssistantChat, Long> {
    // 특정 세션의 대화 내역을 최신순으로 조회
    List<AssistantChat> findByAssistant_AssistantIdxAndDelOrderByCreatedAtDesc(Long assistantIdx, Integer del);
}