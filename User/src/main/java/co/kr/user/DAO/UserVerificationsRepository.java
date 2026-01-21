package co.kr.user.DAO;

import co.kr.user.model.entity.UsersVerifications;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 사용자 인증 정보(UsersVerifications) 엔티티의 데이터베이스 접근을 담당하는 리포지토리 인터페이스입니다.
 * 인증 코드 발송 내역 저장, 코드 검증을 위한 조회, 최신 인증 요청 확인 등의 기능을 제공합니다.
 */
public interface UserVerificationsRepository extends JpaRepository<UsersVerifications, Long> {
    /**
     * 인증 코드(Code)를 기준으로 가장 최근에 생성된 인증 내역을 조회하는 메서드입니다.
     * 사용자가 입력한 인증 코드가 유효한지(DB에 존재하는지) 검증할 때 사용됩니다.
     * 혹시 모를 중복 코드 생성 가능성에 대비하여, 생성일 기준 내림차순(최신순)으로 정렬 후 첫 번째 항목을 가져옵니다.
     *
     * @param Code 사용자가 입력한 인증 코드
     * @param del
     * @return 해당 코드를 가진 최신 인증 내역 (Optional)
     */
    Optional<UsersVerifications> findTopByCodeAndDelOrderByCreatedAtDesc(String Code, int del);

    /**
     * 특정 사용자(UsersIdx)의 가장 최근 인증 요청 내역을 조회하는 메서드입니다.
     * '생성일' 기준 내림차순으로 정렬하여 가장 마지막에 요청된 건을 가져옵니다.
     * 비밀번호 찾기나 회원 탈퇴 등의 과정에서, 해당 유저가 요청한 인증 건이 맞는지 확인하거나 인증 상태를 체크할 때 사용됩니다.
     *
     * @param userIdx 사용자 고유 식별자
     * @param del
     * @return 해당 사용자의 최신 인증 내역 (Optional)
     */
    Optional<UsersVerifications> findTopByUsersIdxAndDelOrderByCreatedAtDesc(Long userIdx, int del);
}