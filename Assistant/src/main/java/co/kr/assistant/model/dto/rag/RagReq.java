package co.kr.assistant.model.dto.rag;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagReq {
    @JsonProperty("q")
    private String q;

    @JsonProperty("top_k")
    private Integer topK;

    // 추가된 메타데이터 필터 필드
    @JsonProperty("age_group")
    private String ageGroup;

    @JsonProperty("gender")
    private String gender;
}