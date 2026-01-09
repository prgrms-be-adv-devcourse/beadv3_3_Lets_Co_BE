package co.kr.product.product.dto.response;

public class ProductImageResponse {

    private Long imageIdx;
    private String url;
    private Integer sortOrder;
    private Boolean isThumbnail;

    public ProductImageResponse(
            Long imageIdx, String url, Integer sortOrder, Boolean isThumbnail) {
        this.imageIdx = imageIdx;
        this.url = url;
        this.sortOrder = sortOrder;
        this.isThumbnail = isThumbnail;
    }

    public Long getImageIdx() { return imageIdx; }
    public String getUrl() { return url; }
    public Integer getSortOrder() { return sortOrder; }
    public Boolean getIsThumbnail() { return isThumbnail; }
}

