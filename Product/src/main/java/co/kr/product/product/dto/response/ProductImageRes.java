package co.kr.product.product.dto.response;

public record ProductImageRes(

    Long imageIdx,
    String url,
    Integer sortOrder,
    Boolean isThumbnail)
    {

}

