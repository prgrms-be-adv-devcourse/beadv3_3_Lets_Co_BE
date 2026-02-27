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
public class RagItem {
    @JsonProperty("score")
    private Double score;

    @JsonProperty("source")
    private String source;

    @JsonProperty("Link")
    private String link;

    @JsonProperty("Products_Name")
    private String productsName;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("Category_Name")
    private String categoryName;

    @JsonProperty("Price")
    private Double price;

    @JsonProperty("Sale_Price")
    private Object salePrice;

    @JsonProperty("View_Count")
    private Integer viewCount;

    @JsonProperty("Order_Count")
    private Integer orderCount;

    @JsonProperty("Review")
    private Integer reviewCount;

    // ⭐ Python RAG에서 계산된 "20대,30대/MALE" 등 주요 구매층 요약 정보
    @JsonProperty("Target_Info")
    private String targetInfo;

    // ⭐ 상품의 상세 옵션 리스트
    @JsonProperty("Options")
    private List<RagOption> options;

    @JsonProperty("Title")
    private String title;

    @JsonProperty("Category")
    private String category;

    @JsonProperty("Status")
    private String status;

    @JsonProperty("Question")
    private String question;

    @JsonProperty("Answer")
    private String answer;

    @JsonProperty("Content")
    private String content;

    @JsonProperty("Published_at")
    private String publishedAt;

    @JsonProperty("Created_at")
    private String createdAt;

    @JsonProperty("Text")
    private String text;

    /**
     * 상품 옵션 상세 정보를 담는 내부 클래스
     */
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