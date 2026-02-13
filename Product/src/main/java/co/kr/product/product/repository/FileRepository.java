package co.kr.product.product.repository;

import co.kr.product.product.model.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface FileRepository extends JpaRepository<FileEntity, Long> {


    List<FileEntity> findAllByRefTableAndRefIndexAndDelFalse(String products, Long productsIdx);
}
