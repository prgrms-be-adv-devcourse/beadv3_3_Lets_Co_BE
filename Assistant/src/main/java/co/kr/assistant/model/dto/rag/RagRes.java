package co.kr.assistant.model.dto.rag;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RagRes {
    // Python 서버의 batch_processor가 반환하는 "intent" 필드와 매핑
    @JsonProperty("intent")
    private String intent;

    // Python 서버의 batch_processor가 반환하는 "results" 필드와 매핑
    @JsonProperty("results")
    private List<RagItem> results;
}