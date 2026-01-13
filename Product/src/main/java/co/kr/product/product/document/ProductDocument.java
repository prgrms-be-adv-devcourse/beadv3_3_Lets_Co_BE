package co.kr.product.product.document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Document(indexName = "products-index", createIndex = false) // 생성한 인덱스 이름과 일치해야 함
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDocument {

    @Id // ES 내부 ID (String)
    private String id;

    @Field(name = "products_idx", type = FieldType.Long)
    private Long productsIdx;

    @Field(name = "products_name", type = FieldType.Text, analyzer = "korean_analyzer")
    private String productsName;


    @Field(name = "products_code", type = FieldType.Keyword)
    private String productsCode;


    @Field(name = "price", type = FieldType.Scaled_Float)
    private BigDecimal price;

    @Field(name = "sale_price", type = FieldType.Scaled_Float)
    private BigDecimal salePrice;

    @Field(name = "seller_idx", type = FieldType.Long)
    private Long sellerIdx;

    @Field(name = "status", type = FieldType.Keyword)
    private String status;

    @Field(name = "view_count", type = FieldType.Long)
    private Long viewCount;

    // 날짜 포맷 매핑
    @Field(name = "updated_at", type = FieldType.Date,format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSX")
    private LocalDateTime updatedAt;


}