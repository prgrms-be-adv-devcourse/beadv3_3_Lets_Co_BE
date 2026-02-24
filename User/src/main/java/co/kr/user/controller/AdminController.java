package co.kr.user.controller;

import co.kr.user.model.dto.admin.*;
import co.kr.user.service.AdminService;
import co.kr.user.util.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 관리자(Admin) 기능을 제공하는 REST 컨트롤러입니다.
 * 회원 목록 조회, 상세 조회, 권한 변경, 계정 정지 및 삭제 등의 관리 기능을 수행합니다.
 * 이 컨트롤러의 모든 요청은 관리자 권한을 가진 사용자만 호출해야 합니다.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users") // 기본 경로 설정
public class AdminController {
    private final AdminService adminService;

    /**
     * 전체 회원 목록을 페이징하여 조회합니다.
     * @param userIdx 요청한 관리자의 식별자 (헤더)
     * @param page 조회할 페이지 번호 (Path Variable)
     * @param adminPageReq 페이지당 항목 수 및 정렬 옵션 (Body)
     * @return 회원 목록 DTO 리스트를 담은 응답 객체
     */
    @PostMapping("/list/{page}")
    public ResponseEntity<BaseResponse<List<AdminUserListDTO>>> userList(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                                         @PathVariable int page,
                                                                         @RequestBody AdminItemsPerPageReq adminPageReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", adminService.userList(userIdx, page, adminPageReq)));
    }

    /**
     * 특정 회원의 상세 정보를 조회합니다.
     * @param userIdx 요청한 관리자의 식별자 (헤더)
     * @param id 조회할 대상 회원의 아이디 (Path Variable)
     * @return 회원 상세 정보 DTO를 담은 응답 객체
     */
    @PostMapping("/{id}")
    public ResponseEntity<BaseResponse<AdminUserDetailDTO>> userDetail(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                                       @PathVariable String id) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", adminService.userDetail(userIdx, id)));
    }

    /**
     * 특정 회원의 권한(Role)을 변경합니다.
     * @param userIdx 요청한 관리자의 식별자 (헤더)
     * @param id 대상 회원의 아이디 (Path Variable)
     * @param req 변경할 권한 정보 (Body)
     * @return 처리 결과 메시지를 담은 응답 객체
     */
    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<String>> userRole(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                         @PathVariable String id,
                                                         @RequestBody AdminRoleReq req) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", adminService.userRole(userIdx, id, req.getRole())));
    }

    /**
     * 특정 회원을 지정된 시간까지 정지(Block) 처리합니다.
     * @param userIdx 요청한 관리자의 식별자 (헤더)
     * @param id 대상 회원의 아이디 (Path Variable)
     * @param adminLockedUntilReq 정지 해제 일시 정보 (Body)
     * @return 처리 결과 메시지를 담은 응답 객체
     */
    @PutMapping("/{id}/block")
    public ResponseEntity<BaseResponse<String>> userBlock(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                          @PathVariable String id,
                                                          @RequestBody AdminLockedUntilReq adminLockedUntilReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", adminService.userBlock(userIdx, id, adminLockedUntilReq.getLocalDateTime())));
    }

    /**
     * 특정 회원을 강제 탈퇴(삭제) 처리합니다.
     * @param userIdx 요청한 관리자의 식별자 (헤더)
     * @param id 대상 회원의 아이디 (Path Variable)
     * @return 처리 결과 메시지를 담은 응답 객체
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<String>> userDelete(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                           @PathVariable String id) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", adminService.userDelete(userIdx, id)));
    }
}