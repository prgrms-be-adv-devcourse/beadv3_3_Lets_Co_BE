package co.kr.product.product.repository.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.kr.product.product.model.document.ProductDocument;
import co.kr.product.product.model.dto.request.ProductListReq;
import co.kr.product.product.repository.ProductEsCustomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductEsCustomRepositoryImpl implements ProductEsCustomRepository {

    private final ElasticsearchClient client;
    private static final String PRODUCT_INDEX = "products-index";

    @Override
    public Page<ProductDocument> searchProducts(List<Float> queryVector, ProductListReq request, Pageable pageable) {
        final String category =  request.category();
        final String ip = request.ip();
        final String search = request.search();

        boolean hasSearch = !(search == null || search.isBlank());

        // 0. ES 검색 요청 객체 생성
        SearchRequest.Builder searchBuilder = new SearchRequest.Builder()
                .index(PRODUCT_INDEX)
                .from((int) pageable.getOffset())
                .size(pageable.getPageSize());

        // 1. 필터 생성 > 카테고리, ip에 대한 조건 존재 시 해당 필터 적용
        List<Query> filters = new ArrayList<>();
        if (StringUtils.hasText(category)) {
            log.info("카테고리 존재 : " + category);
            filters.add(Query.of(q -> q.term(t -> t.field("category.keyword").value(category))));
        }
        if (StringUtils.hasText(ip)) {
            log.info("ip 존재 : " + ip);
            filters.add(Query.of(q -> q.term(t -> t.field("ip.keyword").value(ip))));
        }

        // 1.1 del false 인 데이터만 검색
        filters.add(Query.of(q -> q.term(t -> t.field("del").value(false))));

        // 2. 쿼리문 생성
        // 2.1 검색어가 존재 시
        if(hasSearch) {
            log.info("검색어 존재 : " + search);
            Query lexicalQuery = Query.of(q -> q.bool(b -> {
                // 각 필드마다 점수 가중치 부여하여 조회
                b.should(s -> s.match(m -> m.field("products_name").query(search).boost(2.5f)));
                b.should(s -> s.match(m -> m.field("category_names").query(search).boost(1.5f)));
                b.should(s -> s.match(m -> m.field("ip_names").query(search).boost(1.5f)));

                // 필터 존재 시 필터 적용
                if (!filters.isEmpty()) b.filter(filters);
                return b;
            }));
            searchBuilder.query(lexicalQuery);

            // 검색어에 대한 벡터값이 존재 시(검색어 임베딩 성공 시, 실패할 경우 해당 과정 생략. 기본 검색만 진행)
            if (queryVector != null && !queryVector.isEmpty()) {
                log.info("벡터값 존재");
                // knn 알고리즘 사용. 가장 가까운 50개의 값 유추 (numCandidates : 가볍게 훑어서? 500개의 후보군을 먼저 찾음 )
                searchBuilder.knn(k -> {
                    k
                            .field("product_vector")
                            .queryVector(queryVector)
                            .k(50).numCandidates(500)
                            .boost(0.5f);
                    // 필터 존재 시 필터 적용
                    if (!filters.isEmpty()) k.filter(filters);
                    return k;
                });

            }
        }
        // 2.2 검색어 없을 시
        else {
            log.info("검색어 x");
            Query filterOnlyQuery = Query.of(q -> q.bool(b -> {
                // 필터 존재 시 필터 적용
                if (!filters.isEmpty()) b.filter(filters);
                // 필터 없으면 모든 값 가져오는 쿼리 생성
                else b.must(m -> m.matchAll(ma -> ma));
                return b;
            }));
            searchBuilder.query(filterOnlyQuery);
        }

        // 3. 정렬 추가
        // 3.1 정렬을 요청 받았을 시, 지금은 가격순, 조회수순, 날짜순, 정확도순 생각 중
        // Pageable내 Sort는 springboot의 정렬, 이를 Elastic용으로 변경하는 과정
        if (pageable.getSort().isSorted()){
            log.info("정렬 존재 :" + pageable.getSort());
            List<SortOptions> sortOptionsList = new ArrayList<>();
            // 다중 정렬 고려
            for (Sort.Order order : pageable.getSort()) {
                // 정렬 대상 필드 명
                String sortField = order.getProperty();
                String finalSortField = sortField;
                // 정렬 방향
                SortOrder sortOrder = order.getDirection().isAscending() ? SortOrder.Asc : SortOrder.Desc;
                // 리스트에 저장
                sortOptionsList.add(SortOptions.of(s -> s.field(f -> f.field(finalSortField).order(sortOrder))));
            }
            searchBuilder.sort(sortOptionsList);
        }
        // 3.2  정렬 조건 x , 검색어 x > 기본: 날짜 순으로 정렬
        else if (!hasSearch) {
            searchBuilder.sort(s -> s.field(f -> f.field("updated_at").order(SortOrder.Desc)));
        }
        // 3.3 정렬 조건 x , 검색어 o > 위 쿼리로 검색 시 정확도 순으로 나오기때문에 따로 설정 x

        // 4. 검색 실행
        // 4.1 요청 생성
        SearchRequest searchRequest = searchBuilder.build();

        // 4.2 요청
        try{
            SearchResponse<ProductDocument> response = client.search(searchRequest,ProductDocument.class);

            // 데이터 추출
            List<ProductDocument> documentList = response.hits().hits().stream()
                    .map(Hit::source)          // hit 내부의 _source(ProductDocument)만 꺼냄
                    .filter(doc -> doc != null) // 방어적 코드: null 필터링
                    .toList();
            // 전체 검색 결과 수 추출
            long totalHits = response.hits().total() != null ? response.hits().total().value() : 0;

            //  Page<ProductDocument> 형태로 최종 반환
            return new PageImpl<>(documentList, pageable, totalHits);

        }catch (Exception e){
            throw new IllegalArgumentException("검색 중 문제 발생 :" + e);
        }

    }
}
