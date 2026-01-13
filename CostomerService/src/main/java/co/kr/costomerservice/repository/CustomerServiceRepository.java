package co.kr.costomerservice.repository;


import co.kr.costomerservice.entity.CustomerServiceEntity;
import co.kr.costomerservice.vo.CustomerServiceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

public interface CustomerServiceRepository extends JpaRepository<CustomerServiceEntity,Long> {

    Page<CustomerServiceEntity> findAllByTypeAndDelFalse(CustomerServiceType type, Pageable pageable);

    Optional<CustomerServiceEntity> findByCodeAndDelFalse(String code);
}
