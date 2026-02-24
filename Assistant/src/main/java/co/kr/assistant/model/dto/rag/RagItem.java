package co.kr.assistant.model.dto.rag;

import lombok.Data;

@Data
public class RagItem {
    private Double score;
    private String text;
}