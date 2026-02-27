package co.kr.assistant.model.dto.chat;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class ChatDTO {
    // 1. LLM이 생성한 텍스트 답변
    private String answer;

    // 2. 카테고리별로 정제된 맞춤형 데이터 배열 (Product, Notice, QnA)
    private List<Map<String, Object>> data;
}