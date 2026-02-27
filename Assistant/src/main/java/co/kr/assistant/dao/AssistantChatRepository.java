package co.kr.assistant.dao;

import co.kr.assistant.model.entity.AssistantChat;
import co.kr.assistant.model.vo.PublicDel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssistantChatRepository extends JpaRepository<AssistantChat, Long> {
    // [개선 1] DB 레벨에서 최신 5건만 조회하도록 쿼리 최적화 (성능 향상)
    List<AssistantChat> findTop5ByAssistant_AssistantIdxAndDelOrderByCreatedAtDesc(Long assistantIdx, PublicDel del);
}