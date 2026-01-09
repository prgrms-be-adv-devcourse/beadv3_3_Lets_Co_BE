package co.kr.product.seller.service.impl;


import co.kr.product.seller.entity.OptionEntity;
import co.kr.product.seller.entity.ProductEntity;
import co.kr.product.seller.model.dto.*;
import co.kr.product.seller.repository.OptionRepository;
import co.kr.product.seller.repository.ProductRepository;
import co.kr.product.seller.service.ProductManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static co.kr.product.seller.mapper.ProductMapper.toProductDetail;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductManagerServiceImpl implements ProductManagerService {

    private final ProductRepository productRepository;
    private final OptionRepository optionRepository;

    @Override
    @Transactional
    public ProductListReponse getLists(Pageable pageable, ProductListRequest request) {



        //elastic 접속


        ProductListReponse result = null;

        return result;
    }

    @Override
    @Transactional
    public ProductDetailResponse addProduct(String accountCode, UpsertProductRequest request){

        // 1. 유저 idx  수집
        Long sellerIdx = 1L;
        // 1.1 본인 확인

        // 2. code 생성
        String code = "temp";

        // 3. 상품 저장
        ProductEntity item = ProductEntity.builder()
                .sellerIdx(sellerIdx)
                .code(code)
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .salePrice(request.salePrice())
                .stock(request.stock())
                .build();

        ProductEntity savedItem = productRepository.save(item);


        // 4. Option 저장
        List<OptionEntity> options = request.options().stream()
                .map(requestOpt ->  OptionEntity.builder()
                                .product(savedItem)
                                .code("temp") //대충 코드 생성
                                .name(requestOpt.name())
                                .sortOrder(requestOpt.sortOrder())
                                .price(requestOpt.price())
                                .salePrice(requestOpt.salePrice())
                                .stock(requestOpt.stock())
                                .build()

                        ).toList();


        List<OptionEntity> savedOpt = optionRepository.saveAll(options);

        // 반환 데이터 생성
        ProductDetailResponse result = toProductDetail(
                "success",
                        savedItem,
                        savedOpt );
        return result;
    }


    @Override
    @Transactional
    public ProductDetailResponse getProductDetail(String accountCode, String code){
        // !!!!!!!!!!본인 확인 필요!!!!!!!!!


        ProductEntity product =  productRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 상품입니다."));
        List<OptionEntity> options = optionRepository.findByProduct(product);

        return toProductDetail(
                "success",
                product,
                options
        );
    }

    @Override
    @Transactional
    public ProductDetailResponse updateProduct(
            String accountCode,
            String code,
            UpsertProductRequest request){
        // !!!!!!!!!!본인 확인 필요!!!!!!!!!

        // Entity 가져오기
        ProductEntity product = productRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 상품입니다."));// 예외처리 필요

        List<OptionEntity> options = optionRepository.findByProduct(product);


        Map<String, OptionEntity> optionMap = options.stream()
                .collect(Collectors.toMap(OptionEntity::getCode, Function.identity()));

        // 빈 리스트 생성
        List<OptionEntity> newOptions = new ArrayList<>();

        // 상품 update 진행
        product.update(request.name(),request.description(),request.price(),
                request.salePrice(),request.stock());


        // 옵션 update 진행
        for(ProductOptionsRequest dto : request.options()){
            // 추가 된 옵션
            if(dto.code() == null){
                newOptions.add( OptionEntity.builder()
                                .product(product)
                                .code("temp") //대충 코드 생성
                                .name(dto.name())
                                .sortOrder(dto.sortOrder())
                                .price(dto.price())
                                .salePrice(dto.salePrice())
                                .stock(dto.stock())
                                .build()

                        );



            }
            // 수정 된 옵션
            else{
                OptionEntity entity = optionMap.get(dto.code());
                if (entity != null){
                    entity.update(dto.name(), dto.sortOrder(), dto.price(), dto.salePrice(),dto.stock(),dto.status());

                    optionMap.remove(dto.code());
                }

            }
        }
        // 제거 된 옵션(요청에 없음)
        for (OptionEntity remain : optionMap.values()){
            remain.delete();
        }

        // 위에서 찾은 추가 옵션 저장
        if(!newOptions.isEmpty()){
            optionRepository.saveAll(newOptions);
        }


        return toProductDetail(
                "success",
                product,
                options
        );
    }


    @Override
    @Transactional
    public void deleteProduct(String accountCode, String code){

        // 존재 확인
        ProductEntity product = productRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 상품입니다."));
        List<OptionEntity> options = optionRepository.findByProduct(product);

        if(product.getDel())
        {
            log.info("이미 삭제된 상품입니다 productCode:" + code);
        }
        else{
            product.delete();
        }
        // 옵션 삭제
        try{
            options.forEach(OptionEntity::delete);
        } catch (Exception e) {
            throw new IllegalArgumentException("옵션 삭제 실패.");
        }

    }
}
