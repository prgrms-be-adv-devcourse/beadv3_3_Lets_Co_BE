package co.kr.costomerservice.common.repository;


import co.kr.costomerservice.common.model.entity.CustomerServiceEntity;
import co.kr.costomerservice.common.model.vo.CustomerServiceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

public interface CustomerServiceRepository extends JpaRepository<CustomerServiceEntity,Long> {

    Page<CustomerServiceEntity> findAllByTypeAndDelFalse(CustomerServiceType type, Pageable pageable);

    Optional<CustomerServiceEntity> findByCodeAndDelFalse(String code);

    Page<CustomerServiceEntity> findAllByTypeAndIsPrivateFalseAndDelFalse(CustomerServiceType type, Pageable pageable);


    Page<CustomerServiceEntity> findAllByTypeAndUsersIdxAndDelFalse(CustomerServiceType customerServiceType, Long userId, Pageable pageable);

    Page<CustomerServiceEntity> findAllByTypeAndProductsIdxAndIsPrivateFalseAndDelFalse(CustomerServiceType type,Long productsIdx ,Pageable pageable);

}
