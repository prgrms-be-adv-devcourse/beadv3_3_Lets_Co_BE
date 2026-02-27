package co.kr.assistant.model.dto.chat;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AskReq {

    @NotBlank(message = "질문 내용을 입력해주세요.")
    @Size(min = 1, message = "질문은 최소 1글자 이상이어야 합니다.")
    @JsonProperty("question")
    private String question;

}