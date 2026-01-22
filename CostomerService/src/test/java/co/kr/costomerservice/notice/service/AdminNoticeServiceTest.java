package co.kr.costomerservice.notice.service;

import co.kr.costomerservice.client.AuthServiceClient;
import co.kr.costomerservice.common.dto.response.ResultResponse;
import co.kr.costomerservice.common.entity.CustomerServiceDetailEntity;
import co.kr.costomerservice.common.entity.CustomerServiceEntity;
import co.kr.costomerservice.common.repository.CustomerServiceDetailRepository;
import co.kr.costomerservice.common.repository.CustomerServiceRepository;
import co.kr.costomerservice.common.vo.CustomerServiceCategory;
import co.kr.costomerservice.common.vo.CustomerServiceStatus;
import co.kr.costomerservice.common.vo.CustomerServiceType;
import co.kr.costomerservice.notice.model.dto.request.NoticeUpsertRequest;
import co.kr.costomerservice.notice.model.dto.response.AdminNoticeDetailResponse;
import co.kr.costomerservice.notice.model.dto.response.NoticeListResponse;
import co.kr.costomerservice.notice.service.impl.AdminNoticeServiceImpl;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminNoticeServiceTest {

    @InjectMocks AdminNoticeServiceImpl adminNoticeService;

    @Mock CustomerServiceRepository customerServiceRepository;
    @Mock CustomerServiceDetailRepository customerServiceDetailRepository;
    @Mock AuthServiceClient authServiceClient;

    final Long ADMIN_IDX = 99L;
    final String NOTICE_CODE = "NOTICE-001";

    @Test
    @DisplayName("공지 등록 - 성공")
    void addNotice_Success() {

        // Given
        NoticeUpsertRequest request = createRequest();

        given(authServiceClient.getUserRole(ADMIN_IDX).getBody()).willReturn("ADMIN");
        given(customerServiceRepository.save(any(CustomerServiceEntity.class)))
                .willAnswer(i -> i.getArgument(0));
        given(customerServiceDetailRepository.save(any(CustomerServiceDetailEntity.class)))
                .willAnswer(i -> i.getArgument(0));

        // When
        AdminNoticeDetailResponse response = adminNoticeService.addNotice(ADMIN_IDX, request);

        // Then
        assertThat(response.resultCode()).isEqualTo("success");
        assertThat(response.title()).isEqualTo(request.title());

        verify(customerServiceRepository).save(any(CustomerServiceEntity.class));
        verify(customerServiceDetailRepository).save(any(CustomerServiceDetailEntity.class));

        // 확인용
        System.out.println(response);
    }

    @Test
    @DisplayName("공지 등록 실패 - 권한 없음")
    void addNotice_Fail_Unauthorized() {

        // Given
        Long userIdx = 1L;
        given(authServiceClient.getUserRole(userIdx).getBody()).willReturn("USER");

        // When & Then
        assertThatThrownBy(() -> adminNoticeService.addNotice(userIdx, createRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("판매자 권한이 없습니다");
    }

    @Test
    @DisplayName("공지 목록 조회 - 성공")
    void getNoticeList_Success() {

        // Given
        Pageable pageable = PageRequest.of(0, 10);
        CustomerServiceEntity notice = createNoticeEntity(NOTICE_CODE, CustomerServiceType.NOTICE);
        Page<CustomerServiceEntity> pageResult = new PageImpl<>(List.of(notice));

        given(authServiceClient.getUserRole(ADMIN_IDX).getBody()).willReturn("ADMIN");
        given(customerServiceRepository.findAllByTypeAndDelFalse(CustomerServiceType.NOTICE, pageable))
                .willReturn(pageResult);

        // When
        NoticeListResponse response = adminNoticeService.getNoticeList(ADMIN_IDX, pageable);

        // Then
        assertThat(response.resultCode()).isEqualTo("success");
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).title()).isEqualTo(notice.getTitle());

        // 확인용
        System.out.println(response);
    }

    @Test
    @DisplayName("공지 상세 조회 - 성공")
    void getNoticeDetail_Success() {
        // Given
        CustomerServiceEntity notice = createNoticeEntity(NOTICE_CODE, CustomerServiceType.NOTICE);
        CustomerServiceDetailEntity detail = createDetailEntity(notice);

        given(authServiceClient.getUserRole(ADMIN_IDX).getBody()).willReturn("ADMIN");
        given(customerServiceRepository.findByCodeAndDelFalse(NOTICE_CODE)).willReturn(Optional.of(notice));
        given(customerServiceDetailRepository.findByCustomerServiceAndDelFalse(notice)).willReturn(Optional.of(detail));

        // When
        AdminNoticeDetailResponse response = adminNoticeService.getNoticeDetail(ADMIN_IDX, NOTICE_CODE);

        // Then
        assertThat(response.resultCode()).isEqualTo("success");
        assertThat(response.csCode()).isEqualTo(NOTICE_CODE);
        assertThat(response.content()).isEqualTo(detail.getContent());

        // 확인용
        System.out.println(response);
    }

    @Test
    @DisplayName("공지 상세 조회 실패 - 공지 타입 아님")
    void getNoticeDetail_Fail_InvalidType() {

        // Given
        CustomerServiceEntity qna = createNoticeEntity(NOTICE_CODE, CustomerServiceType.QNA_PRODUCT);

        given(authServiceClient.getUserRole(ADMIN_IDX).getBody()).willReturn("ADMIN");
        given(customerServiceRepository.findByCodeAndDelFalse(NOTICE_CODE)).willReturn(Optional.of(qna));

        // When & Then
        assertThatThrownBy(() -> adminNoticeService.getNoticeDetail(ADMIN_IDX, NOTICE_CODE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 게시글은 공지사항이 아닙니다.");
    }

    @Test
    @DisplayName("공지 수정 - 성공")
    void updateNotice_Success() {
        // Given
        CustomerServiceEntity notice = createNoticeEntity(NOTICE_CODE, CustomerServiceType.NOTICE);
        CustomerServiceDetailEntity detail = createDetailEntity(notice);

        NoticeUpsertRequest request = new NoticeUpsertRequest(
                CustomerServiceCategory.PAYMENT, CustomerServiceStatus.HIDDEN,
                "수정제목", "수정내용", true, true, LocalDateTime.now()
        );

        given(authServiceClient.getUserRole(ADMIN_IDX).getBody()).willReturn("ADMIN");
        given(customerServiceRepository.findByCodeAndDelFalse(NOTICE_CODE)).willReturn(Optional.of(notice));
        given(customerServiceDetailRepository.findByCustomerServiceAndDelFalse(notice)).willReturn(Optional.of(detail));

        // When
        AdminNoticeDetailResponse response = adminNoticeService.updateNotice(ADMIN_IDX, NOTICE_CODE, request);

        // Then
        assertThat(notice.getTitle()).isEqualTo("수정제목");
        assertThat(detail.getContent()).isEqualTo("수정내용");
        assertThat(notice.getIsPinned()).isTrue();

        // 확인용
        System.out.println(response);
    }

    @Test
    @DisplayName("공지 삭제 - 성공")
    void deleteNotice_Success() {
        // Given
        CustomerServiceEntity notice = createNoticeEntity(NOTICE_CODE, CustomerServiceType.NOTICE);
        CustomerServiceDetailEntity detail = createDetailEntity(notice);

        given(authServiceClient.getUserRole(ADMIN_IDX).getBody()).willReturn("ADMIN");
        given(customerServiceRepository.findByCodeAndDelFalse(NOTICE_CODE)).willReturn(Optional.of(notice));
        given(customerServiceDetailRepository.findByCustomerServiceAndDelFalse(notice)).willReturn(Optional.of(detail));

        // When
        ResultResponse response = adminNoticeService.deleteNotice(ADMIN_IDX, NOTICE_CODE);

        // Then
        assertThat(response.resultCode()).isEqualTo("success");
        assertThat(notice.getDel()).isTrue();
        assertThat(detail.getDel()).isTrue();

        // 확인용
        System.out.println(response);
    }

    /**
     * Helper Methods
     */
    private NoticeUpsertRequest createRequest() {
        return new NoticeUpsertRequest(
                CustomerServiceCategory.ACCOUNT,
                CustomerServiceStatus.PUBLISHED,
                "공지 제목",
                "공지 내용",
                false,
                true,
                LocalDateTime.now()
        );
    }

    private CustomerServiceEntity createNoticeEntity(String code, CustomerServiceType type) {
        CustomerServiceEntity entity = CustomerServiceEntity.builder()
                .code(code)
                .type(type)
                .category(CustomerServiceCategory.ACCOUNT)
                .status(CustomerServiceStatus.PUBLISHED)
                .title("공지 제목")
                .isPrivate(false)
                .isPinned(false)
                .usersIdx(ADMIN_IDX)
                .build();
        ReflectionTestUtils.setField(entity, "idx", 1L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(entity, "del", false);
        return entity;
    }

    private CustomerServiceDetailEntity createDetailEntity(CustomerServiceEntity parent) {
        CustomerServiceDetailEntity detail = CustomerServiceDetailEntity.builder()
                .detailCode("DETAIL-CODE")
                .usersIdx(parent.getUsersIdx())
                .customerService(parent)
                .content("공지 내용")
                .build();
        ReflectionTestUtils.setField(detail, "del", false);
        return detail;
    }
}