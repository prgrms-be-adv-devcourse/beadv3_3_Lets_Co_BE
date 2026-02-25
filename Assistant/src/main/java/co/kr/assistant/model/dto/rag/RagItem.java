package co.kr.assistant.model.dto.rag; // 패키지명 유지

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RagItem {

    private Double score;

    @JsonProperty("text")
    private String text; // 파이썬의 기본 텍스트 필드 (컴파일 에러 방지용)

    @JsonProperty("Link")
    private String link;

    // ==========================================
    // 🛍️ 상품(Product) 전용 필드
    // ==========================================
    @JsonProperty("Products_Name")
    private String productsName;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("Category_Name")
    private String categoryName;

    @JsonProperty("IP_Name")
    private String ipName;

    @JsonProperty("Price")
    private Double price;

    @JsonProperty("Sale_Price")
    private Object salePrice; // 할인가가 없을 경우 빈 문자열("") 대비 Object 처리

    @JsonProperty("View_Count")
    private Integer viewCount;

    @JsonProperty("Order_Count")
    private Integer orderCount;

    @JsonProperty("Review")
    private Integer reviewCount;

    @JsonProperty("Option")
    private List<RagOption> options;

    // ==========================================
    // 📢 게시판(Notice / QnA) 전용 필드
    // ==========================================
    @JsonProperty("Title")
    private String title;

    @JsonProperty("Category")
    private String category;

    @JsonProperty("Content")
    private String content;

    @JsonProperty("Question")
    private String question;

    @JsonProperty("Answer")
    private String answer;

    @JsonProperty("Status")
    private String status;

    @JsonProperty("Is_Pinned")
    private Integer isPinned;

    @JsonProperty("Published_at")
    private String publishedAt;

    @JsonProperty("Created_at")
    private String createdAt;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RagOption {
        @JsonProperty("Option_Name")
        private String optionName;

        @JsonProperty("Option_Price")
        private Double optionPrice;

        @JsonProperty("Option_Sale_Price")
        private Object optionSalePrice;
    }
}