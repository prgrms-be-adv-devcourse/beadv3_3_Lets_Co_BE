package co.kr.user.DAO;

import co.kr.user.model.entity.Users_Verifications;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * [인증 정보 리포지토리]
 * 'Users_Verifications' 테이블에 대한 CRUD 작업을 담당합니다.
 * 이메일 발송 후 저장된 인증 코드를 다시 꺼내와서, 사용자가 입력한 코드와 비교할 때 사용됩니다.
 */
public interface UserVerificationsRepository extends JpaRepository<Users_Verifications, Long> {

    /**
     * [인증 코드 조회 메서드]
     * 사용자가 입력한 인증 코드(Code)를 기반으로 DB에서 데이터를 찾습니다.
     * * * 메서드 이름 분석 (Spring Data JPA 규칙):
     * 1. find: 조회 기능을 수행합니다.
     * 2. Top: 여러 개의 결과가 있다면 그 중 맨 위의 1개만 가져옵니다. (Limit 1)
     * 3. ByCode: WHERE Code = ? 조건절을 생성합니다.
     * 4. OrderByCreatedAtDesc: 생성일(CreatedAt) 기준 내림차순(DESC) 정렬합니다.
     * * * 해석:
     * "해당 코드를 가진 데이터 중, 가장 최근에 생성된 1건을 찾아라."
     * (이유: 랜덤 코드라 하더라도 만에 하나 중복될 수 있으므로, 가장 최신의 유효한 코드를 가져오기 위함입니다.)
     * * @param Code 사용자가 입력한 인증 코드 문자열
     * @return Optional<Users_Verifications> (결과가 없을 수도 있으므로 null 안전한 Optional 래퍼 사용)
     */
    Optional<Users_Verifications> findTopByCodeOrderByCreatedAtDesc(String Code);

}