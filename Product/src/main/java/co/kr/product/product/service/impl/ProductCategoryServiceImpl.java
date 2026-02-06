package co.kr.product.product.service.impl;

import co.kr.product.product.mapper.ProductMapper;
import co.kr.product.product.model.dto.request.CategoryUpsertReq;
import co.kr.product.product.model.dto.response.CategoryFamilyRes;
import co.kr.product.product.model.dto.response.CategoryRes;
import co.kr.product.product.model.entity.ProductCategoryEntity;
import co.kr.product.product.model.vo.CategoryType;
import co.kr.product.product.repository.ProductCategoryRepository;
import co.kr.product.product.service.ProductCategoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;

    @Override
    public List<CategoryRes> getCategory(CategoryType type) {

        List<ProductCategoryEntity> categoryList = categoryRepository.findAllByTypeAndDelFalse(CategoryType.CATEGORY);

        List<CategoryRes> item = categoryList.stream()

                .map( entity -> new CategoryRes(
                        entity.getCategoryCode(),
                        entity.getCategoryName(),
                        entity.getPath(),
                        // 부모의 code를 가져오는 작업
                        categoryList.stream()
                                // 1. 리스트의 categoryIdx 와  부모 ID(parentIdx)가 같은지 비교
                                .filter(c -> c.getCategoryIdx().equals(entity.getParentIdx()))
                                // 2. 찾은 객체에서 CategoryCode만 추출
                                .map(ProductCategoryEntity::getCategoryCode)
                                // 3. 첫 번째 결과 반환 (없으면 0 반환)
                                .findFirst()
                                .orElse("0")


                ) ).toList();

        return item;
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryFamilyRes getFamilyCategory(String categoryCode, CategoryType type) {

        // 1. 기준 카테고리 검색
        ProductCategoryEntity entity = categoryRepository.findByCategoryCodeAndTypeAndDelFalse(categoryCode, CategoryType.CATEGORY)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 카테고리 입니다."));

        // 2. 부모 카테고리 검색

        // 2-1. 본인이 최상위일 경우 비어있는채로 넘김
        List<ProductCategoryEntity> sortedParents = List.of();

        // 2-2. 본인이 최상위 카테고리가 아닐경우
        if (entity.hasParent()){

            // 상위 카테고리 검색
            // path 내 상위 카테고리의 idx만 분리
            List<Long> parentsIdx = Arrays.stream(entity.getPath().split("/"))
                    .filter(str -> !str.isBlank())  // 혹시 모를 공백 등 제거
                    .map(Long::parseLong)
                    .toList();

            // 위 idx 기반 in 쿼리로 db에서 카테고리 검색
            List<ProductCategoryEntity> parents = categoryRepository.findAllByCategoryIdxInAndTypeAndDelFalse(parentsIdx, CategoryType.CATEGORY);

            Map<Long, ProductCategoryEntity> parentsMap = parents.stream()
                    .collect(Collectors.toMap(
                                    ProductCategoryEntity::getCategoryIdx,
                                    Function.identity()
                            )
                    );

            sortedParents = parentsIdx.stream()
                    .map(parentsMap::get)
                    .filter(Objects::nonNull)  // 혹시 모를 null방지
                    .toList();


        }


        // 3. 모든 자식 검색
        // List<ProductCategoryEntity> childs = categoryRepository.findAllByPathStartingWithAndTypeAndDelFalse(entity.getPath(), CategoryType.CATEGORY);
        
        // 바로 아래 단계 자식만 검색
        List<ProductCategoryEntity> childs = categoryRepository.findAllByParentIdxAndTypeAndDelFalse(entity.getCategoryIdx(), CategoryType.CATEGORY);


        return ProductMapper.toCategoryFamilyMapper(sortedParents, childs);
    }

    @Override
    @Transactional
    public CategoryRes addCategory(Long usersIdx, CategoryUpsertReq req, CategoryType type) {

        ProductCategoryEntity savedCategory;

        //임시
        String parentCode;

        // 최상위 카테고리 추가
        if (req.parentCode().isBlank() || req.parentCode().equals("0") ) {
            // entity 생성
            ProductCategoryEntity item = ProductCategoryEntity.builder()
                    .categoryName(req.categoryName())
                    .categoryCode(UUID.randomUUID().toString())
                    .type(CategoryType.CATEGORY)
                    .path("0/")
                    .build();
            // 저장
            savedCategory = categoryRepository.save(item);

            parentCode = "0";
        }

        // 하위 카테고리 추가
        else{
            // 바로 위 부모 카테고리 하나 조회
            ProductCategoryEntity parent = categoryRepository
                    .findByCategoryCodeAndTypeAndDelFalse(req.parentCode(), CategoryType.CATEGORY)
                    .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 상위 카테고리 입니다."));

            // entity 생성
            ProductCategoryEntity item = ProductCategoryEntity.builder()
                    .categoryName(req.categoryName())
                    .categoryCode(UUID.randomUUID().toString())
                    .type(CategoryType.CATEGORY)
                    .parentIdx(parent.getCategoryIdx())
                    // 부모의 path 넣어놓기
                    .path(parent.getPath())
                    .build();

            // 저장
            savedCategory = categoryRepository.save(item);

            // 임시
            parentCode = parent.getCategoryCode();
        }

        // 현재 path에 본인의 idx를 추가
        // ex) idx:8인 카테고리일시 :  "1/3/" > "1/3/8"
        savedCategory.updatePath(savedCategory.getPath() + savedCategory.getCategoryIdx() + "/");
        return new CategoryRes(
                savedCategory.getCategoryCode(),
                savedCategory.getCategoryName(),
                savedCategory.getPath(),
                parentCode
        );
    }

    @Override
    @Transactional
    public String updateCategory(Long usersIdx,String categoryCode ,CategoryUpsertReq req, CategoryType type) {

        // 이름 변경 및 부모 변경
        
        // 해당 카테고리 조회
        ProductCategoryEntity entity = categoryRepository.findByCategoryCodeAndTypeAndDelFalse(categoryCode, CategoryType.CATEGORY)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 카테고리 입니다."));

        entity.updateName(req.categoryName());

        // case 1: 이름 만 변경
        // 아무 값도 입력받지 못할 시 여기서 끝 (부모 카테고리의 변화가 없다 판단)
        if (req.parentCode() == null || req.parentCode().isBlank()){
            return "";
        }

        // case 2: parentCode를 "0"으로 받을 시.
        // 이는 하위 카테고리였던것을 최상위로 올렸을 경우를 대비.
        // "0"을 입력받으면 최상위 카테고리로 올렸다고 판단
        if (req.parentCode().equals("0")){

            String oldPath = entity.getPath();
            String newPath = "0/";

            int temp =  String.valueOf(entity.getCategoryIdx()).length() + 1;
            String oldParentPath = oldPath.substring(0, oldPath.length()-temp);

            // parentIdx 컬럼 비우고
            entity.removeParentIdx();
            // path 수정
            entity.updatePath( "0/"+entity.getCategoryIdx() + "/");

            // 자식 카테고리 패스 수정
            categoryRepository.updateChildPath(oldPath,newPath, oldParentPath);

            return "굿";
        }

        // case 3 : 카테고리 레벨 수정
        // error! a 를 수정하는데 부모 코드로 a를 받은 경우
        if(categoryCode.equals(req.parentCode())){
            throw new IllegalArgumentException("자신을 상위/하위 카테고리로 설정 할 수 없습니다.");
        }
        // 3.1 이동 할 부모 카테고리 조회
        ProductCategoryEntity parent = categoryRepository.findByCategoryCodeAndTypeAndDelFalse(req.parentCode(), CategoryType.CATEGORY)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 상위 카테고리 입니다."));

        // ex) 0/1/3/5/7/
        String oldPath = entity.getPath();

        int temp =  String.valueOf(entity.getCategoryIdx()).length() + 1;
        // ex) 0/1/3/5/
        String oldParentPath = oldPath.substring(0, oldPath.length()-temp);

        // ex) 0/2
        String newPath = parent.getPath();

        // error! a를 a 자식 카테고리 아래로 이동할 경우
        // ex) 1/3/5/9 > 5를 9 아래로 옮길 시
        // if("1/3/5/9" 가 "1/3/5/" 로 시작하면) > 예외처리
        if(newPath.startsWith(oldPath)){
            throw new IllegalArgumentException("자신의 하위 카테고리로 이동 할 수 없습니다.");
        }
        
        // 3.2 이동
        entity.updatePath(newPath + entity.getCategoryIdx() + "/");
        entity.updateParentIdx(parent.getParentIdx());

        // 3.3 기존에 하위에 있던 카테고리들 역시 모두 이동
        // 조회 후 수정하는거 보단 이게 더 좋지 않을까 판단
        categoryRepository.updateChildPath(oldPath,newPath, oldParentPath);

        return "대성공";
    }

    @Override
    @Transactional
    public String deleteCategory(Long usersIdx,String categoryCode, CategoryType type) {

        // 검색
        ProductCategoryEntity entity = categoryRepository.findByCategoryCodeAndTypeAndDelFalse(categoryCode, CategoryType.CATEGORY)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 카테고리 입니다."));

        // 삭제 (soft delete)
        // 삭제하는 카테고리의 path를 이용해 본인 포함 자식들 전부 삭제
        categoryRepository.updateChildDel(entity.getPath());

        return "삭제 성공";
    }
}
