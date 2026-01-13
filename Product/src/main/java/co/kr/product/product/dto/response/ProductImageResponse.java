package co.kr.product.product.dto.response;

public record ProductImageResponse(

    Long imageIdx,
    String url,
    Integer sortOrder,
    Boolean isThumbnail)
    {

}

