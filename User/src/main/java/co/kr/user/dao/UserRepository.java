package co.kr.user.dao;

import co.kr.user.model.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자(Users) 엔티티의 데이터베이스 접근(CRUD)을 담당하는 리포지토리 인터페이스입니다.
 * JpaRepository를 상속받아 기본적인 저장, 조회, 수정, 삭제 메서드를 자동으로 제공합니다.
 */
public interface UserRepository extends JpaRepository<Users, Long> {
    /**
     * 아이디(이메일) 중복 여부를 확인하는 메서드입니다.
     * 회원가입 시 이미 존재하는 아이디인지 검증하기 위해 사용됩니다.
     *
     * @param ID 중복 확인할 사용자 아이디(이메일)
     * @return true: 이미 존재함, false: 사용 가능함
     */
    boolean existsByID(String ID);

    /**
     * 아이디(이메일)를 기준으로 사용자 정보를 조회하는 메서드입니다.
     * 로그인, 정보 찾기 등에서 특정 사용자를 식별할 때 주로 사용됩니다.
     *
     * @param ID 조회할 사용자 아이디
     * @return 조회된 Users 객체를 감싼 Optional (존재하지 않을 경우 empty)
     */
    Optional<Users> findByID(String ID);

    /**
     * 삭제(탈퇴)되지 않은 모든 사용자를 생성일 역순(최신순)으로 조회하는 메서드입니다.
     * 관리자 페이지 등에서 전체 회원 목록을 볼 때 사용될 수 있습니다.
     *
     * @param del 삭제 상태 플래그 (0: 정상, 1: 탈퇴)
     * @return 조건에 맞는 Users 리스트
     */
    List<Users> findAllByDelOrderByCreatedAtDesc(int del);

    /**
     * 아이디(이메일)와 삭제 상태(Del)를 동시에 조건으로 하여 사용자를 조회하는 메서드입니다.
     * 예를 들어, 탈퇴하지 않은(del=0) 특정 아이디의 회원을 찾을 때 유용합니다.
     *
     * @param ID 조회할 사용자 아이디
     * @param del 삭제 상태 플래그
     * @return 조건에 맞는 Users 객체를 감싼 Optional
     */
    Optional<Users> findByIDAndDel(String ID, int del);
}