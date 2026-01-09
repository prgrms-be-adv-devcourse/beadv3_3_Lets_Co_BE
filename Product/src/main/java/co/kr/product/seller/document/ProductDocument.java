package co.kr.product.seller.document;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(indexName = "products") // 인덱스 이름 (소문자 필수)
@Setting(settingPath = "static/elastic/product-setting.json") // 분리된 settings 파일 사용 권장
@Mapping(mappingPath = "static/elastic/product-mapping.json") // 분리된 mappings 파일 사용 권장
public class ProductDocument {

    @Id
    @Field(type = FieldType.Keyword)
    private String id; // ES의 _id는 문자열이 표준 (productsIdx.toString())

    @Field(name = "products_idx", type = FieldType.Long)
    private Long productsIdx; // 정렬 및 필터링용

    @Field(name = "seller_idx", type = FieldType.Long)
    private Long sellerIdx; // 판매자별 상품 모아보기 (Terms Query)

    @Field(name = "code", type = FieldType.Keyword)
    private String code; // 정확한 코드 매칭 (Term Query)

    // ★ 검색의 핵심: 이름은 검색(Text)도 되고, 정렬(Keyword)도 되어야 함 (Multi-field)
    @MultiField(
            mainField = @Field(name = "name", type = FieldType.Text, analyzer = "korean_analyzer"),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword)
            }
    )
    private String name;

    // 설명은 검색 전용 (Text). 형태소 분석 적용
    @Field(name = "description", type = FieldType.Text, analyzer = "korean_analyzer")
    private String description;

    // ES에서 BigDecimal은 지원하지 않음. Double로 변환하여 저장.
    @Field(name = "price", type = FieldType.Double)
    private Double price;

    @Field(name = "sale_price", type = FieldType.Double)
    private Double salePrice;

    @Field(name = "stock", type = FieldType.Integer)
    private Integer stock;

    @Field(name = "status", type = FieldType.Keyword)
    private String status; // SALE, STOP 등 정확한 상태 필터링

    @Field(name = "view_count", type = FieldType.Long)
    private Long viewCount;

    // 날짜 포맷 지정 (밀리초 포함)
    @Field(name = "created_at", type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime createdAt;

    @Field(name = "updated_at", type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime updatedAt;

    @Field(name = "del", type = FieldType.Boolean)
    private Boolean del;

    @Builder
    public ProductDocument(Long productsIdx, Long sellerIdx, String code, String name, String description, Double price, Double salePrice, Integer stock, String status, Long viewCount, LocalDateTime createdAt, LocalDateTime updatedAt, Boolean del) {
        this.id = String.valueOf(productsIdx);
        this.productsIdx = productsIdx;
        this.sellerIdx = sellerIdx;
        this.code = code;
        this.name = name;
        this.description = description;
        this.price = price;
        this.salePrice = salePrice;
        this.stock = stock;
        this.status = status;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.del = del;
    }
}