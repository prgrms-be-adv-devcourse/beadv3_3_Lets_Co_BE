package co.kr.product.review.model.dto.response;


public class CommonResponse {

    private String resultCode;

    public CommonResponse(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultCode() {
        return resultCode;
    }
}
