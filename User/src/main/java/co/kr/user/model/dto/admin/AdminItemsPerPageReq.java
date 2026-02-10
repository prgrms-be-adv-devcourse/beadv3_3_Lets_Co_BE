package co.kr.user.model.dto.admin;

import co.kr.user.model.vo.DBColum;
import co.kr.user.model.vo.DBSorting;
import co.kr.user.model.vo.UsersMembership;
import co.kr.user.model.vo.UsersRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminItemsPerPageReq {
    @NotNull(message = "페이지당 항목 수는 무조건 있어야 합니다.")
    private int itemsPerPage;
    private DBColum colum;
    private DBSorting sorting;
}
