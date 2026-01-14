package co.kr.user.repository;

import co.kr.user.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * [User 데이터 접근 계층]
 * DB의 'User' 테이블에 접근하여 CRUD(생성, 조회, 수정, 삭제)를 수행
 * JpaRepository를 상속받아 기본적인 메서드(findById, save 등)를 바로 사용할 수 있음
 */
public interface UserRepository extends JpaRepository<User, Long> {
    // 기본적인 findById 등은 JpaRepository가 자동으로 제공하므로 별도 정의 불필요
}


