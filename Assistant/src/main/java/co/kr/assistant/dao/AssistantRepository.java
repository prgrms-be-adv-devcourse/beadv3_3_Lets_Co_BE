package co.kr.assistant.dao;

import co.kr.assistant.model.entity.Assistant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AssistantRepository extends JpaRepository<Assistant, Long> {
    Optional<Assistant> findByAssistantCodeAndDel(String assistantCode, Integer del);
}