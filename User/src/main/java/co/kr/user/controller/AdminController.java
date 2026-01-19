package co.kr.user.controller;

import co.kr.user.model.DTO.admin.AdminLockedUntilReq;
import co.kr.user.model.DTO.admin.AdminRoleReq;
import co.kr.user.model.DTO.admin.AdminUserDetailDTO;
import co.kr.user.model.DTO.admin.AdminUserListDTO;
import co.kr.user.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 관리자(Admin)용 사용자 관리 컨트롤러
 * * <p>관리자 권한을 가진 사용자가 시스템 내의 다른 사용자들을 조회, 수정, 차단, 삭제하는 기능을 제공합니다.</p>
 * <p>모든 요청 헤더에는 요청을 보낸 관리자의 식별자(X-USERS-IDX)가 포함되어야 합니다.</p>
 */
@Validated // 입력값 유효성 검증 활성화
@RestController // JSON 응답을 반환하는 컨트롤러
@RequiredArgsConstructor // 생성자 주입(DI) 자동 생성
@RequestMapping("/admin/users") // 기본 경로: /admin/users
public class AdminController {

    // 관리자 기능 비즈니스 로직을 담당하는 서비스
    private final AdminService adminService;

    /**
     * 전체 사용자 목록 조회 API
     * * <p>HTTP Method: POST</p>
     * <p>Path: /admin/users/list</p>
     * * @param userIdx 요청을 보낸 관리자의 고유 식별자 (Header: X-USERS-IDX) - 권한 검증에 사용됨
     * @return 전체 사용자 목록(AdminUserListDTO 리스트)을 반환 (200 OK)
     */
    @PostMapping("/list")
    public ResponseEntity<List<AdminUserListDTO>> userList(@RequestHeader("X-USERS-IDX") Long userIdx) {
        return ResponseEntity.ok(adminService.userList(userIdx));
    }

    /**
     * 특정 사용자 상세 정보 조회 API
     * * <p>HTTP Method: POST</p>
     * <p>Path: /admin/users/{id}</p>
     * * @param userIdx 요청을 보낸 관리자의 고유 식별자 (Header)
     * @param id 조회 대상 사용자의 ID (Path Variable, 예: user123)
     * @return 해당 사용자의 상세 정보(AdminUserDetailDTO)를 반환 (200 OK)
     */
    @PostMapping("/{id}")
    public ResponseEntity<AdminUserDetailDTO> userDetail(@RequestHeader("X-USERS-IDX") Long userIdx, @PathVariable String id) {
        return ResponseEntity.ok(adminService.userDetail(userIdx, id));
    }

    /**
     * 사용자 권한(Role) 변경 API
     * * <p>특정 사용자의 권한을 변경합니다 (예: USER -> ADMIN).</p>
     * <p>HTTP Method: PUT</p>
     * <p>Path: /admin/users/{id}</p>
     * * @param userIdx 요청을 보낸 관리자의 고유 식별자 (Header)
     * @param id 대상 사용자의 ID (Path Variable)
     * @param req 변경할 권한 정보가 담긴 요청 객체 (Body)
     * @return 성공 메시지 반환 (200 OK)
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> userRole(@RequestHeader("X-USERS-IDX") Long userIdx, @PathVariable String id, @RequestBody  AdminRoleReq req) {
        return ResponseEntity.ok(adminService.userRole(userIdx, id, req.getRole()));
    }

    /**
     * 사용자 일시 차단(Block) 설정 API
     * * <p>특정 사용자를 지정된 날짜까지 로그인하지 못하도록 차단합니다.</p>
     * <p>HTTP Method: PUT</p>
     * <p>Path: /admin/users/{id}/block</p>
     * * @param userIdx 요청을 보낸 관리자의 고유 식별자 (Header)
     * @param id 대상 사용자의 ID (Path Variable)
     * @param adminLockedUntilReq 차단 해제 날짜(LocalDateTime)가 담긴 요청 객체 (Body)
     * @return 성공 메시지 반환 (200 OK)
     */
    @PutMapping("/{id}/block")
    public ResponseEntity<String> userBlock(@RequestHeader("X-USERS-IDX") Long userIdx, @PathVariable String id, @RequestBody AdminLockedUntilReq adminLockedUntilReq) {
        return  ResponseEntity.ok(adminService.userBlock(userIdx, id, adminLockedUntilReq.getLocalDateTime()));
    }

    /**
     * 사용자 삭제(강제 탈퇴) API
     * * <p>특정 사용자의 계정을 삭제합니다.</p>
     * <p>HTTP Method: DELETE</p>
     * <p>Path: /admin/users/{id}</p>
     * * @param userIdx 요청을 보낸 관리자의 고유 식별자 (Header)
     * @param id 삭제 대상 사용자의 ID (Path Variable)
     * @return 성공 메시지 반환 (200 OK)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> userDelete(@RequestHeader("X-USERS-IDX") Long userIdx, @PathVariable String id) {
        return ResponseEntity.ok(adminService.userDelete(userIdx, id));
    }
}