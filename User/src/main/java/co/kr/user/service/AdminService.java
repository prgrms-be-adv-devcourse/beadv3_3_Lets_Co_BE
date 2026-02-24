package co.kr.user.service;

import co.kr.user.model.dto.admin.AdminItemsPerPageReq;
import co.kr.user.model.dto.admin.AdminUserDetailDTO;
import co.kr.user.model.dto.admin.AdminUserListDTO;
import co.kr.user.model.vo.UsersRole;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자(Admin) 기능을 처리하기 위한 서비스 인터페이스입니다.
 * 회원 목록 조회, 상세 조회, 권한 변경, 계정 정지, 강제 탈퇴 등의 관리자 전용 기능을 정의합니다.
 */
public interface AdminService {

    /**
     * 전체 회원 목록을 페이징하여 조회합니다.
     * @param userIdx 요청한 관리자의 식별자 (권한 검증용)
     * @param page 조회할 페이지 번호
     * @param adminItemsPerPageReq 페이지당 항목 수 및 정렬 기준이 담긴 요청 객체
     * @return 조회된 회원 목록 DTO 리스트
     */
    List<AdminUserListDTO> userList(Long userIdx, int page, AdminItemsPerPageReq adminItemsPerPageReq);

    /**
     * 특정 회원의 상세 정보를 조회합니다.
     * @param userIdx 요청한 관리자의 식별자 (권한 검증용)
     * @param id 조회할 대상 회원의 아이디
     * @return 회원의 상세 정보가 담긴 DTO (주소, 카드 정보 포함)
     */
    AdminUserDetailDTO userDetail(Long userIdx, String id);

    /**
     * 특정 회원의 권한(Role)을 변경합니다. (예: 일반 회원 -> 판매자, 혹은 관리자 등)
     * @param userIdx 요청한 관리자의 식별자 (권한 검증용)
     * @param id 대상 회원의 아이디
     * @param changeRole 변경할 새로운 권한(Role) Enum
     * @return 처리 결과 메시지
     */
    String userRole(Long userIdx, String id, UsersRole changeRole);

    /**
     * 특정 회원을 지정된 시간까지 계정 정지(Block) 처리합니다.
     * @param userIdx 요청한 관리자의 식별자 (권한 검증용)
     * @param id 대상 회원의 아이디
     * @param lockedUntil 계정 잠금이 해제될 일시
     * @return 처리 결과 메시지 ("~까지 정지되었습니다.")
     */
    String userBlock(Long userIdx, String id, LocalDateTime lockedUntil);

    /**
     * 특정 회원을 강제 탈퇴 처리합니다.
     * @param userIdx 요청한 관리자의 식별자 (권한 검증용)
     * @param id 대상 회원의 아이디
     * @return 처리 결과 메시지
     */
    String userDelete(Long userIdx, String id);
}