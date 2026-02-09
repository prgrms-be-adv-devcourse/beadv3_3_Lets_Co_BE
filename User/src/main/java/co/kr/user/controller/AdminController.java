package co.kr.user.controller;

import co.kr.user.model.dto.admin.AdminLockedUntilReq;
import co.kr.user.model.dto.admin.AdminRoleReq;
import co.kr.user.model.dto.admin.AdminUserDetailDTO;
import co.kr.user.model.dto.admin.AdminUserListDTO;
import co.kr.user.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 관리자(Admin)용 사용자 관리 컨트롤러 클래스입니다.
 * 관리자 권한을 가진 사용자가 시스템 내의 다른 사용자들을 조회, 수정, 차단, 삭제하는 기능을 제공합니다.
 * 모든 요청 헤더에는 요청을 보낸 관리자의 식별자(X-USERS-IDX)가 포함되어야 하며, 서비스 계층에서 권한 검증이 수행됩니다.
 */
@Validated // 입력값 유효성 검증(Validation) 기능을 활성화합니다.
@RestController // RESTful API 컨트롤러임을 명시하며, 응답 데이터를 JSON 형식으로 반환합니다.
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성하여 의존성 주입(DI)을 받습니다.
@RequestMapping("/admin/users") // 이 클래스 내의 모든 API 엔드포인트는 "/admin/users" 경로로 시작합니다.
public class AdminController {

    // 관리자 기능 비즈니스 로직을 담당하는 서비스 객체입니다.
    private final AdminService adminService;

    /**
     * 전체 사용자 목록 조회 API입니다.
     * 시스템에 등록된 모든 사용자(탈퇴 회원 제외 등 조건에 따라)의 목록을 조회합니다.
     *
     * @param userIdx 요청을 보낸 관리자의 고유 식별자입니다. (Header: X-USERS-IDX) - 권한 검증에 사용됩니다.
     * @return 전체 사용자 목록(AdminUserListDTO 리스트)을 포함한 ResponseEntity 객체 (HTTP 200 OK)
     */
    @PostMapping("/list")
    public ResponseEntity<List<AdminUserListDTO>> userList(@RequestHeader("X-USERS-IDX") Long userIdx) {
        // AdminService를 호출하여 전체 사용자 목록을 반환합니다.
        return ResponseEntity.ok(adminService.userList(userIdx));
    }

    /**
     * 특정 사용자 상세 정보 조회 API입니다.
     * 특정 사용자의 상세한 정보(개인정보, 활동 내역 등)를 조회합니다.
     *
     * @param userIdx 요청을 보낸 관리자의 고유 식별자입니다. (Header: X-USERS-IDX)
     * @param id 조회 대상 사용자의 ID (URL Path Variable, 예: user123)
     * @return 해당 사용자의 상세 정보(AdminUserDetailDTO)를 포함한 ResponseEntity 객체 (HTTP 200 OK)
     */
    @PostMapping("/{id}")
    public ResponseEntity<AdminUserDetailDTO> userDetail(@RequestHeader("X-USERS-IDX") Long userIdx, @PathVariable String id) {
        // AdminService를 호출하여 특정 사용자의 상세 정보를 반환합니다.
        return ResponseEntity.ok(adminService.userDetail(userIdx, id));
    }

    /**
     * 사용자 권한(Role) 변경 API입니다.
     * 특정 사용자의 시스템 권한을 변경합니다 (예: 일반 사용자 -> 관리자, 판매자 등).
     *
     * @param userIdx 요청을 보낸 관리자의 고유 식별자입니다. (Header: X-USERS-IDX)
     * @param id 권한을 변경할 대상 사용자의 ID (URL Path Variable)
     * @param req 변경할 권한 정보가 담긴 요청 객체 (HTTP Body)
     * @return 권한 변경 성공 메시지를 포함한 ResponseEntity 객체 (HTTP 200 OK)
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> userRole(@RequestHeader("X-USERS-IDX") Long userIdx, @PathVariable String id, @RequestBody  AdminRoleReq req) {
        // AdminService를 호출하여 사용자 권한 변경 로직을 수행하고 결과 메시지를 반환합니다.
        return ResponseEntity.ok(adminService.userRole(userIdx, id, req.getRole()));
    }

    /**
     * 사용자 일시 차단(Block) 설정 API입니다.
     * 특정 사용자를 지정된 날짜와 시간까지 로그인하지 못하도록 차단(정지)합니다.
     *
     * @param userIdx 요청을 보낸 관리자의 고유 식별자입니다. (Header: X-USERS-IDX)
     * @param id 차단할 대상 사용자의 ID (URL Path Variable)
     * @param adminLockedUntilReq 차단 해제 날짜(LocalDateTime)가 담긴 요청 객체 (HTTP Body)
     * @return 차단 설정 성공 메시지를 포함한 ResponseEntity 객체 (HTTP 200 OK)
     */
    @PutMapping("/{id}/block")
    public ResponseEntity<String> userBlock(@RequestHeader("X-USERS-IDX") Long userIdx, @PathVariable String id, @RequestBody AdminLockedUntilReq adminLockedUntilReq) {
        // AdminService를 호출하여 사용자 차단 로직을 수행하고 결과 메시지를 반환합니다.
        return  ResponseEntity.ok(adminService.userBlock(userIdx, id, adminLockedUntilReq.getLocalDateTime()));
    }

    /**
     * 사용자 삭제(강제 탈퇴) API입니다.
     * 관리자가 특정 사용자의 계정을 강제로 삭제(탈퇴 처리)합니다.
     *
     * @param userIdx 요청을 보낸 관리자의 고유 식별자입니다. (Header: X-USERS-IDX)
     * @param id 삭제할 대상 사용자의 ID (URL Path Variable)
     * @return 삭제 처리 성공 메시지를 포함한 ResponseEntity 객체 (HTTP 200 OK)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> userDelete(@RequestHeader("X-USERS-IDX") Long userIdx, @PathVariable String id) {
        // AdminService를 호출하여 사용자 삭제 로직을 수행하고 결과 메시지를 반환합니다.
        return ResponseEntity.ok(adminService.userDelete(userIdx, id));
    }
}