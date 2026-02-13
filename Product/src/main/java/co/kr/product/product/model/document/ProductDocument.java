package co.kr.product.product.model.document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

// Elastic 내 products-index 인덱스
@Document(indexName = "products-index", createIndex = false)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDocument {

    @Id // ES 내부 ID, 실제로 우리가 사용하지는 않음
    private String id;

    @Field(name = "products_idx", type = FieldType.Long)
    private Long productsIdx;

    @Field(name = "products_name", type = FieldType.Text, analyzer = "korean_analyzer")
    private String productsName;


    @Field(name = "products_code", type = FieldType.Keyword)
    private String productsCode;


    @Field(name = "price", type = FieldType.Float)
    private BigDecimal price;

    @Field(name = "sale_price", type = FieldType.Float)
    private BigDecimal salePrice;

    @Field(name = "seller_idx", type = FieldType.Long)
    private Long sellerIdx;

    @Field(name = "status", type = FieldType.Keyword)
    private String status;

    @Field(name = "view_count", type = FieldType.Long)
    private Long viewCount;

    @Field(name = "thumbnail_key", type = FieldType.Text)
    private String thumbnailKey;

    @MultiField(
            mainField = @Field (name = "category_names", type = FieldType.Text),
            otherFields = @InnerField(suffix = "keyword", type = FieldType.Keyword, ignoreAbove = 256))
    private List<String> categoryNames;

    // 날짜 포맷 매핑
    @Field(name = "updated_at", type = FieldType.Date,format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSSX")
    private LocalDateTime updatedAt;

    @Field(name = "del", type = FieldType.Boolean)
    private boolean del;
}