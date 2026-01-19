package co.kr.user.service;

import co.kr.user.model.DTO.admin.AdminUserDetailDTO;
import co.kr.user.model.DTO.admin.AdminUserListDTO;
import co.kr.user.model.vo.UsersRole;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자(Admin) 전용 회원 관리 비즈니스 로직을 정의하는 인터페이스입니다.
 * 전체 회원 조회, 상세 정보 확인, 권한 변경, 계정 차단 및 삭제 기능을 명세합니다.
 * 구현체: AdminService
 */
public interface AdminServiceImpl {

    /**
     * 전체 회원 목록 조회 메서드 정의입니다.
     * 시스템에 등록된 모든 회원의 요약 정보를 조회합니다.
     *
     * @param userIdx 관리자(요청자)의 고유 식별자 (권한 검증용)
     * @return 전체 회원 목록 (AdminUserListDTO 리스트)
     */
    List<AdminUserListDTO> userList(Long userIdx);

    /**
     * 회원 상세 정보 조회 메서드 정의입니다.
     * 특정 회원의 상세한 개인정보 및 활동 내역을 조회합니다.
     *
     * @param userIdx 관리자(요청자)의 고유 식별자
     * @param id 조회할 대상 회원의 아이디(이메일)
     * @return 회원 상세 정보 객체 (AdminUserDetailDTO)
     */
    AdminUserDetailDTO userDetail(Long userIdx, String id);

    /**
     * 회원 권한 변경 메서드 정의입니다.
     * 특정 회원의 권한(Role)을 수정합니다. (예: USER -> ADMIN)
     *
     * @param userIdx 관리자(요청자)의 고유 식별자
     * @param id 권한을 변경할 대상 회원의 아이디
     * @param changeRole 변경할 권한 값
     * @return 권한 변경 결과 메시지
     */
    String userRole(Long userIdx, String id, UsersRole changeRole);

    /**
     * 회원 일시 차단(Block) 메서드 정의입니다.
     * 특정 회원을 지정된 시간까지 로그인하지 못하도록 차단합니다.
     *
     * @param userIdx 관리자(요청자)의 고유 식별자
     * @param id 차단할 대상 회원의 아이디
     * @param lockedUntil 차단 해제 일시
     * @return 차단 설정 결과 메시지
     */
    String userBlock(Long userIdx, String id, LocalDateTime lockedUntil);

    /**
     * 회원 강제 탈퇴(삭제) 메서드 정의입니다.
     * 특정 회원의 계정을 강제로 삭제(탈퇴 처리)합니다.
     *
     * @param userIdx 관리자(요청자)의 고유 식별자
     * @param id 삭제할 대상 회원의 아이디
     * @return 삭제 처리 결과 메시지
     */
    String userDelete(Long userIdx, String id);
}