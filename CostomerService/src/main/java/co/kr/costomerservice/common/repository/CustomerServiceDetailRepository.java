package co.kr.costomerservice.common.repository;

import co.kr.costomerservice.common.entity.CustomerServiceDetailEntity;
import co.kr.costomerservice.common.entity.CustomerServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerServiceDetailRepository extends JpaRepository<CustomerServiceDetailEntity,Long> {


    Optional<CustomerServiceDetailEntity> findByCustomerServiceAndDelFalse(CustomerServiceEntity noticeEntity);

    List<CustomerServiceDetailEntity> findAllByCustomerServiceAndDelFalse(CustomerServiceEntity noticeEntity);

    Optional<CustomerServiceDetailEntity> findByDetailCodeAndDelFalse(String detailCode);
}
