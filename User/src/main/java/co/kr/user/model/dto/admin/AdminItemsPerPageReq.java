package co.kr.user.model.dto.admin;

import co.kr.user.model.vo.DBColum;
import co.kr.user.model.vo.DBSorting;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 관리자가 회원 목록 등을 조회할 때 페이지 크기와 정렬 기준을 지정하기 위한 DTO입니다.
 */
@Data
public class AdminItemsPerPageReq {
    /** 한 페이지에 표시할 항목의 수 (필수 값) */
    @NotNull(message = "페이지당 항목 수는 무조건 있어야 합니다.")
    private int itemsPerPage;
    /** 정렬 기준이 되는 컬럼 (ID, 이름, 역할 등) */
    private DBColum colum;
    /** 정렬 방식 (오름차순, 내림차순) */
    private DBSorting sorting;
}