package co.kr.user.controller;

import co.kr.user.model.dto.balance.BalanceReq;
import co.kr.user.model.vo.UsersRole;
import co.kr.user.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // RESTful API 컨트롤러임을 명시하며, 응답 데이터를 JSON으로 반환합니다.
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성하여 의존성을 주입받습니다.
@RequestMapping("/client/users") // 이 클래스의 API 기본 경로를 "/auth"로 설정합니다.
public class ClientController {
    private final ClientService clientService;

    /**
     * 사용자 권한(Role) 조회 API
     * * <p>HTTP Method: GET</p>
     * <p>Path: /auth/role</p>
     * * @param userIdx 조회할 사용자의 고유 식별자 (Query Parameter, 예: ?userIdx=1)
     * @return 사용자의 권한 정보(UsersRole Enum 등)를 반환 (200 OK)
     */
    @GetMapping("/role")
    public ResponseEntity<UsersRole> getRole(@RequestParam @Valid Long userIdx) {
        // 서비스 계층을 호출하여 해당 userIdx를 가진 유저의 역할(ADMIN, USER 등)을 조회합니다.
        return ResponseEntity.ok(clientService.getRole(userIdx));
    }

    @PostMapping("/balance")
    public ResponseEntity<String> Balance(@RequestHeader("X-USERS-IDX") Long userIdx, BalanceReq balanceReq) {
        return ResponseEntity.ok(clientService.Balance(userIdx, balanceReq));
    }
}
