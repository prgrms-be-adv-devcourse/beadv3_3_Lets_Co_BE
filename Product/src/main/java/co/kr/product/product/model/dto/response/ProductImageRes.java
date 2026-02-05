package co.kr.product.product.model.dto.response;

public record ProductImageRes(

    Long imageIdx,
    String url,
    Integer sortOrder,
    Boolean isThumbnail)
    {

}

