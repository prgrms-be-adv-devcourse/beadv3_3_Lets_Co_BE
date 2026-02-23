package co.kr.assistant.model.dto.rag;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RagReq {
    @JsonProperty("q")
    private String q;

    @JsonProperty("top_k") // Python의 SearchRequest 필드명과 정확히 일치
    private Integer topK;
}