package co.kr.user.controller;

import co.kr.user.model.dto.admin.*;
import co.kr.user.service.AdminService;
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
    public ResponseEntity<List<AdminUserListDTO>> userList(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                           @PathVariable int page,
                                                           @RequestBody AdminItemsPerPageReq adminPageReq) {
        return ResponseEntity.ok(adminService.userList(userIdx, page, adminPageReq));
    }

    @PostMapping("/{id}")
    public ResponseEntity<AdminUserDetailDTO> userDetail(@RequestHeader("X-USERS-IDX") Long userIdx, @PathVariable String id) {
        return ResponseEntity.ok(adminService.userDetail(userIdx, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> userRole(@RequestHeader("X-USERS-IDX") Long userIdx, @PathVariable String id, @RequestBody  AdminRoleReq req) {
        return ResponseEntity.ok(adminService.userRole(userIdx, id, req.getRole()));
    }

    @PutMapping("/{id}/block")
    public ResponseEntity<String> userBlock(@RequestHeader("X-USERS-IDX") Long userIdx, @PathVariable String id, @RequestBody AdminLockedUntilReq adminLockedUntilReq) {
        return  ResponseEntity.ok(adminService.userBlock(userIdx, id, adminLockedUntilReq.getLocalDateTime()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> userDelete(@RequestHeader("X-USERS-IDX") Long userIdx, @PathVariable String id) {
        return ResponseEntity.ok(adminService.userDelete(userIdx, id));
    }
}