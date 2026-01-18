package co.kr.user.service;

import co.kr.user.model.DTO.admin.AdminUserDetailDTO;
import co.kr.user.model.DTO.admin.AdminUserListDTO;
import co.kr.user.model.vo.UsersRole;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminServiceImpl {
    List<AdminUserListDTO> userList(Long userIdx);

    AdminUserDetailDTO userDetail(Long userIdx, String id);

    String userRole(Long userIdx, String id, UsersRole changeRole);

    String userBlock(Long userIdx, String id, LocalDateTime lockedUntil);

    String userDelete(Long userIdx, String id);
}
