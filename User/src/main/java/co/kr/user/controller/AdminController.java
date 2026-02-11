package co.kr.user.controller;

import co.kr.user.model.dto.admin.*;
import co.kr.user.service.AdminService;
import co.kr.user.util.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/list/{page}")
    public ResponseEntity<BaseResponse<List<AdminUserListDTO>>> userList(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                                         @PathVariable int page,
                                                                         @RequestBody AdminItemsPerPageReq adminPageReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", adminService.userList(userIdx, page, adminPageReq)));
    }

    @PostMapping("/{id}")
    public ResponseEntity<BaseResponse<AdminUserDetailDTO>> userDetail(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                                       @PathVariable String id) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", adminService.userDetail(userIdx, id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<String>> userRole(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                         @PathVariable String id,
                                                         @RequestBody AdminRoleReq req) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", adminService.userRole(userIdx, id, req.getRole())));
    }

    @PutMapping("/{id}/block")
    public ResponseEntity<BaseResponse<String>> userBlock(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                          @PathVariable String id,
                                                          @RequestBody AdminLockedUntilReq adminLockedUntilReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", adminService.userBlock(userIdx, id, adminLockedUntilReq.getLocalDateTime())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<String>> userDelete(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                           @PathVariable String id) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", adminService.userDelete(userIdx, id)));
    }
}