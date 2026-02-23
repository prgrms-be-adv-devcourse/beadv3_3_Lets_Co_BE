package co.kr.user.dao;

import co.kr.user.model.entity.File;
import co.kr.user.model.vo.PublicDel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Long> {
    // Integer del 대신 PublicDel del을 사용하도록 변경
    Optional<File> findByRefTableAndRefIndexAndDel(String refTable, Long refIndex, PublicDel del);
}