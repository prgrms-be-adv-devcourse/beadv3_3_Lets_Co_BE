package co.kr.user.service;

import co.kr.user.model.dto.admin.AdminItemsPerPageReq;
import co.kr.user.model.dto.admin.AdminUserDetailDTO;
import co.kr.user.model.dto.admin.AdminUserListDTO;
import co.kr.user.model.vo.UsersRole;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminService {
    List<AdminUserListDTO> userList(Long userIdx, int page, AdminItemsPerPageReq adminItemsPerPageReq);

    AdminUserDetailDTO userDetail(Long userIdx, String id);

    String userRole(Long userIdx, String id, UsersRole changeRole);

    String userBlock(Long userIdx, String id, LocalDateTime lockedUntil);

    String userDelete(Long userIdx, String id);
}