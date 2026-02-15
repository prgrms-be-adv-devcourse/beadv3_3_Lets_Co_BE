package co.kr.user.model.dto.admin;

import co.kr.user.model.vo.UsersMembership;
import co.kr.user.model.vo.UsersRole;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 관리자용 회원 목록 조회 시 각 항목의 정보를 담는 DTO입니다.
 */
@Data
public class AdminUserListDTO {
    private UsersRole role;             /** 사용자 권한 */
    private String id;                  /** 사용자 로그인 ID */
    private UsersMembership membership; /** 멤버십 등급 */
    private String name;                /** 사용자 이름 */
    private LocalDateTime lockedUntil;  /** 계정 잠금 여부 확인을 위한 일시 */
    private LocalDateTime createdAt;    /** 가입 일시 */
    private LocalDateTime updatedAt;    /** 최종 정보 수정 일시 */
}