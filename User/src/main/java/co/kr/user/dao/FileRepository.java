package co.kr.user.dao;

import co.kr.user.model.entity.File;
import co.kr.user.model.vo.PublicDel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * 파일(File) 엔티티에 대한 데이터베이스 액세스 처리를 담당하는 레포지토리 인터페이스입니다.
 * JpaRepository를 상속받아 기본적인 CRUD 기능을 제공합니다.
 */
public interface FileRepository extends JpaRepository<File, Long> {

    /**
     * 참조 테이블명, 참조 인덱스, 삭제 여부를 조건으로 단일 파일 정보를 조회합니다.
     * 기존의 Integer 타입 삭제 플래그 대신 PublicDel 열거형을 사용하여 데이터 정합성을 강화했습니다.
     *
     * @param refTable 파일을 참조하는 소속 테이블 이름 (예: 'Seller')
     * @param refIndex 해당 테이블의 고유 식별자(PK)
     * @param del 삭제 상태 (PublicDel.OK 또는 PublicDel.DELETED)
     * @return 조회된 파일 엔티티를 포함한 Optional 객체
     */
    Optional<File> findByRefTableAndRefIndexAndDel(String refTable, Long refIndex, PublicDel del);
}