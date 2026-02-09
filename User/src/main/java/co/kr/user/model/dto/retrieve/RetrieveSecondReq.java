package co.kr.user.model.dto.retrieve;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RetrieveSecondReq {

    @JsonProperty("ID")
    private String ID;
    private String authCode;
}
