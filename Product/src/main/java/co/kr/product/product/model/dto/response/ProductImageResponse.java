package co.kr.product.product.model.dto.response;

public record ProductImageResponse(

    Long imageIdx,
    String url,
    Integer sortOrder,
    Boolean isThumbnail)
    {

}

