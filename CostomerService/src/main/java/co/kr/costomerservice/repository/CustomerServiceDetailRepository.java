package co.kr.costomerservice.repository;

import co.kr.costomerservice.entity.CustomerServiceDetailEntity;
import co.kr.costomerservice.entity.CustomerServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerServiceDetailRepository extends JpaRepository<CustomerServiceDetailEntity,Long> {


    Optional<CustomerServiceDetailEntity> findByCustomerServiceAndDelFalse(CustomerServiceEntity noticeEntity);
}
