package co.kr.costomerservice.notice.service;

import co.kr.costomerservice.common.entity.CustomerServiceDetailEntity;
import co.kr.costomerservice.common.entity.CustomerServiceEntity;
import co.kr.costomerservice.common.repository.CustomerServiceDetailRepository;
import co.kr.costomerservice.common.repository.CustomerServiceRepository;
import co.kr.costomerservice.common.vo.CustomerServiceCategory;
import co.kr.costomerservice.common.vo.CustomerServiceStatus;
import co.kr.costomerservice.common.vo.CustomerServiceType;
import co.kr.costomerservice.notice.model.dto.response.NoticeDetailResponse;
import co.kr.costomerservice.notice.model.dto.response.NoticeListResponse;
import co.kr.costomerservice.notice.service.impl.UserNoticeServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserNoticeServiceTest {

    @InjectMocks UserNoticeServiceImpl userNoticeService;

    @Mock CustomerServiceRepository customerServiceRepository;
    @Mock CustomerServiceDetailRepository customerServiceDetailRepository;

    final String NOTICE_CODE = "NOTICE-001";

    @Test
    @DisplayName("공지 목록 조회 - 성공")
    void getNoticeList_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        CustomerServiceEntity notice = createNoticeEntity(NOTICE_CODE, CustomerServiceType.NOTICE);
        Page<CustomerServiceEntity> pageResult = new PageImpl<>(List.of(notice));

        given(customerServiceRepository.findAllByTypeAndDelFalse(CustomerServiceType.NOTICE, pageable))
                .willReturn(pageResult);

        // When
        NoticeListResponse response = userNoticeService.getNoticeList(pageable);

        // Then
        assertThat(response.resultCode()).isEqualTo("success");
        assertThat(response.items()).hasSize(1);

        assertThat(response.items().get(0).title()).isEqualTo(notice.getTitle());
        assertThat(response.items().get(0).viewCount()).isEqualTo(10L);

        // 확인용
        System.out.println(response);
    }

    @Test
    @DisplayName("공지 상세 조회 - 성공")
    void getNoticeDetail_Success() {
        // Given
        CustomerServiceEntity notice = createNoticeEntity(NOTICE_CODE, CustomerServiceType.NOTICE);
        CustomerServiceDetailEntity detail = createDetailEntity(notice);

        given(customerServiceRepository.findByCodeAndDelFalse(NOTICE_CODE)).willReturn(Optional.of(notice));
        given(customerServiceDetailRepository.findByCustomerServiceAndDelFalse(notice)).willReturn(Optional.of(detail));

        // When
        NoticeDetailResponse response = userNoticeService.getNoticeDetail(NOTICE_CODE);

        // Then
        assertThat(response.resultCode()).isEqualTo("success");
        assertThat(response.title()).isEqualTo(notice.getTitle());
        assertThat(response.content()).isEqualTo(detail.getContent());
        assertThat(response.viewCount()).isEqualTo(10L);

        // 확인용
        System.out.println(response);
    }

    @Test
    @DisplayName("공지 상세 조회 실패 - 존재하지 않는 코드")
    void getNoticeDetail_Fail_NotFound() {
        // Given
        given(customerServiceRepository.findByCodeAndDelFalse(NOTICE_CODE)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userNoticeService.getNoticeDetail(NOTICE_CODE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재 하지 않는 공지입니다.");
    }

    @Test
    @DisplayName("공지 상세 조회 실패 - 공지 타입 아님")
    void getNoticeDetail_Fail_InvalidType() {
        // Given
        CustomerServiceEntity qna = createNoticeEntity(NOTICE_CODE, CustomerServiceType.QNA_PRODUCT); // QNA 타입

        given(customerServiceRepository.findByCodeAndDelFalse(NOTICE_CODE)).willReturn(Optional.of(qna));

        // When & Then
        assertThatThrownBy(() -> userNoticeService.getNoticeDetail(NOTICE_CODE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 게시글은 공지사항이 아닙니다.");
    }

    @Test
    @DisplayName("공지 상세 조회 실패 - 상세 내용 없음 (데이터 무결성 오류)")
    void getNoticeDetail_Fail_DetailNotFound() {
        // Given
        CustomerServiceEntity notice = createNoticeEntity(NOTICE_CODE, CustomerServiceType.NOTICE);

        given(customerServiceRepository.findByCodeAndDelFalse(NOTICE_CODE)).willReturn(Optional.of(notice));
        given(customerServiceDetailRepository.findByCustomerServiceAndDelFalse(notice)).willReturn(Optional.empty()); // 상세 없음

        // When & Then
        assertThatThrownBy(() -> userNoticeService.getNoticeDetail(NOTICE_CODE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재 하지 않는 공지입니다.");
    }

    /*
     * Helper Methods
     */
    private CustomerServiceEntity createNoticeEntity(String code, CustomerServiceType type) {

        CustomerServiceEntity entity = CustomerServiceEntity.builder()
                .code(code)
                .type(type)
                .category(CustomerServiceCategory.ORDER)
                .status(CustomerServiceStatus.PUBLISHED)
                .title("공지 제목")
                .isPrivate(false)
                .isPinned(false)
                .usersIdx(1L)
                .username("관리자")
                .build();

        ReflectionTestUtils.setField(entity, "idx", 1L);
        ReflectionTestUtils.setField(entity, "viewCount", 10L);
        ReflectionTestUtils.setField(entity, "publishedAt", LocalDateTime.now());
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.now());

        return entity;
    }

    private CustomerServiceDetailEntity createDetailEntity(CustomerServiceEntity parent) {
        CustomerServiceDetailEntity detail = CustomerServiceDetailEntity.builder()
                .customerService(parent)
                .content("공지 내용")
                .build();
        ReflectionTestUtils.setField(detail, "updatedAt", LocalDateTime.now());
        return detail;
    }
}