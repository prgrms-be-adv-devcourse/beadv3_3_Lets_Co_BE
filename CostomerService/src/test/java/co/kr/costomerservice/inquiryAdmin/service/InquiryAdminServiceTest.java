package co.kr.costomerservice.inquiryAdmin.service;

import co.kr.costomerservice.common.entity.CustomerServiceDetailEntity;
import co.kr.costomerservice.common.entity.CustomerServiceEntity;
import co.kr.costomerservice.common.repository.CustomerServiceDetailRepository;
import co.kr.costomerservice.common.repository.CustomerServiceRepository;
import co.kr.costomerservice.common.vo.CustomerServiceCategory;
import co.kr.costomerservice.common.vo.CustomerServiceStatus;
import co.kr.costomerservice.common.vo.CustomerServiceType;
import co.kr.costomerservice.inquiryAdmin.dto.request.InquiryUpsertRequest;
import co.kr.costomerservice.inquiryAdmin.dto.response.InquiryDetailResponse;
import co.kr.costomerservice.inquiryAdmin.dto.response.InquiryListResponse;
import co.kr.costomerservice.inquiryAdmin.service.impl.InquiryAdminServiceImpl;
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
class InquiryAdminServiceTest {

    @InjectMocks InquiryAdminServiceImpl inquiryAdminService;

    @Mock CustomerServiceRepository customerServiceRepository;
    @Mock CustomerServiceDetailRepository customerServiceDetailRepository;

    // 공통 상수
    final Long USER_IDX = 1L;
    final String INQUIRY_CODE = "INQ-001";

    @Test
    @DisplayName("전체 문의 목록 조회 (공개글) - 성공")
    void getInquiryList_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        CustomerServiceEntity inquiry = createInquiry(USER_IDX, false); // 공개글
        Page<CustomerServiceEntity> pageResult = new PageImpl<>(List.of(inquiry));

        given(customerServiceRepository.findAllByTypeAndIsPrivateFalseAndDelFalse(CustomerServiceType.QNA_ADMIN, pageable))
                .willReturn(pageResult);

        // When
        InquiryListResponse response = inquiryAdminService.getInquiryList(pageable);

        // Then
        assertThat(response.resultCode()).isEqualTo("success");
        assertThat(response.list()).hasSize(1);
        assertThat(response.list().get(0).isPrivate()).isFalse();

        // 확인용
        System.out.println(response);
    }

    @Test
    @DisplayName("문의 등록 - 성공")
    void addInquiry_Success() {
        // Given
        InquiryUpsertRequest request = new InquiryUpsertRequest(
                null, CustomerServiceCategory.ACCOUNT, "계정 문의", "내용", true
        );

        given(customerServiceRepository.save(any(CustomerServiceEntity.class)))
                .willAnswer(i -> i.getArgument(0)); // 저장된 객체 그대로 반환
        given(customerServiceDetailRepository.save(any(CustomerServiceDetailEntity.class)))
                .willAnswer(i -> i.getArgument(0));

        // When
        InquiryDetailResponse response = inquiryAdminService.addInquiry(USER_IDX, request);

        // Then
        assertThat(response.resultCode()).isEqualTo("success");
        assertThat(response.info().title()).isEqualTo("계정 문의");

        verify(customerServiceRepository).save(any(CustomerServiceEntity.class));
        verify(customerServiceDetailRepository).save(any(CustomerServiceDetailEntity.class));

        // 확인용
        System.out.println(response);
    }

    @Test
    @DisplayName("문의 상세 조회 - 본인 글 성공")
    void getInquiryDetail_Success_Owner() {
        // Given
        CustomerServiceEntity inquiry = createInquiry(USER_IDX, true); // 비밀글
        List<CustomerServiceDetailEntity> details = List.of(createDetail(inquiry, "any-code"));

        given(customerServiceRepository.findByCodeAndDelFalse(INQUIRY_CODE)).willReturn(Optional.of(inquiry));
        given(customerServiceDetailRepository.findAllByCustomerServiceAndDelFalse(inquiry)).willReturn(details);

        // When
        InquiryDetailResponse response = inquiryAdminService.getInquiryDetail(USER_IDX, INQUIRY_CODE);

        // Then
        assertThat(response.isOwner()).isTrue();
        assertThat(response.info().title()).isEqualTo(inquiry.getTitle());

        // 확인용
        System.out.println(response);
    }

    @Test
    @DisplayName("문의 상세 조회 - 타인 글이지만 공개글 성공")
    void getInquiryDetail_Success_Public() {
        // Given
        Long otherUserIdx = 2L;
        CustomerServiceEntity inquiry = createInquiry(otherUserIdx, false); // 공개글
        List<CustomerServiceDetailEntity> details = List.of(createDetail(inquiry, "any-code"));

        given(customerServiceRepository.findByCodeAndDelFalse(INQUIRY_CODE)).willReturn(Optional.of(inquiry));
        given(customerServiceDetailRepository.findAllByCustomerServiceAndDelFalse(inquiry)).willReturn(details);

        // When
        InquiryDetailResponse response = inquiryAdminService.getInquiryDetail(USER_IDX, INQUIRY_CODE);

        // Then
        assertThat(response.isOwner()).isFalse(); // 내 글 아님
        assertThat(response.info().title()).isEqualTo(inquiry.getTitle());

        // 확인용
        System.out.println(response);
    }

    @Test
    @DisplayName("문의 상세 조회 실패 - 타인 비밀글")
    void getInquiryDetail_Fail_Private() {
        // Given
        Long otherUserIdx = 2L;
        CustomerServiceEntity inquiry = createInquiry(otherUserIdx, true); // 비밀글

        given(customerServiceRepository.findByCodeAndDelFalse(INQUIRY_CODE)).willReturn(Optional.of(inquiry));

        // When & Then
        assertThatThrownBy(() -> inquiryAdminService.getInquiryDetail(USER_IDX, INQUIRY_CODE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 문의는 비밀 글 입니다.");
    }

    @Test
    @DisplayName("문의 상세 조회 실패 - 문의 타입 아님")
    void getInquiryDetail_Fail_InvalidType() {
        // Given
        CustomerServiceEntity productQna = createInquiry(USER_IDX, false);
        ReflectionTestUtils.setField(productQna, "type", CustomerServiceType.QNA_PRODUCT); // 타입 변조

        given(customerServiceRepository.findByCodeAndDelFalse(INQUIRY_CODE)).willReturn(Optional.of(productQna));

        // When & Then
        assertThatThrownBy(() -> inquiryAdminService.getInquiryDetail(USER_IDX, INQUIRY_CODE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 게시글은 문의가 아닙니다.");
    }

    @Test
    @DisplayName("문의 수정 - 성공")
    void updateInquiry_Success() {

        // Given
        CustomerServiceEntity inquiry = createInquiry(USER_IDX, false);
        CustomerServiceDetailEntity detail = createDetail(inquiry, INQUIRY_CODE);

        InquiryUpsertRequest request = new InquiryUpsertRequest(
                INQUIRY_CODE, CustomerServiceCategory.ETC, "수정제목", "수정내용", true
        );

        given(customerServiceRepository.findByCodeAndDelFalse(INQUIRY_CODE)).willReturn(Optional.of(inquiry));
        given(customerServiceDetailRepository.findAllByCustomerServiceAndDelFalse(inquiry)).willReturn(List.of(detail));

        // When
        InquiryDetailResponse response = inquiryAdminService.updateInquiry(USER_IDX, INQUIRY_CODE, request);

        // Then
        assertThat(inquiry.getTitle()).isEqualTo("수정제목"); // Dirty Checking
        assertThat(detail.getContent()).isEqualTo("수정내용");
        assertThat(inquiry.getIsPrivate()).isTrue();

        // 확인용
        System.out.println(response);
    }

    @Test
    @DisplayName("문의 수정 실패 - 작성자 아님")
    void updateInquiry_Fail_NotOwner() {
        // Given
        Long otherUserIdx = 2L;
        CustomerServiceEntity inquiry = createInquiry(otherUserIdx, false);

        given(customerServiceRepository.findByCodeAndDelFalse(INQUIRY_CODE)).willReturn(Optional.of(inquiry));

        // When & Then
        assertThatThrownBy(() -> inquiryAdminService.updateInquiry(USER_IDX, INQUIRY_CODE, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 문의의 작성자가 아닙니다.");
    }

    @Test
    @DisplayName("문의 수정 실패 - 답변 완료 상태")
    void updateInquiry_Fail_AlreadyAnswered() {
        // Given
        CustomerServiceEntity inquiry = createInquiry(USER_IDX, false);
        inquiry.updateStatus(CustomerServiceStatus.ANSWERED);

        given(customerServiceRepository.findByCodeAndDelFalse(INQUIRY_CODE)).willReturn(Optional.of(inquiry));

        // When & Then
        assertThatThrownBy(() -> inquiryAdminService.updateInquiry(USER_IDX, INQUIRY_CODE, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("더 이상 수정이 불가능 합니다.");
    }

    @Test
    @DisplayName("문의 삭제 - 성공")
    void deleteInquiry_Success() {
        // Given
        CustomerServiceEntity inquiry = createInquiry(USER_IDX, false);
        CustomerServiceDetailEntity detail = createDetail(inquiry, "any-code");

        given(customerServiceRepository.findByCodeAndDelFalse(INQUIRY_CODE)).willReturn(Optional.of(inquiry));
        given(customerServiceDetailRepository.findAllByCustomerServiceAndDelFalse(inquiry)).willReturn(List.of(detail));

        // When
        inquiryAdminService.deleteInquiry(USER_IDX, INQUIRY_CODE);

        // Then
        assertThat(inquiry.getDel()).isTrue();
        assertThat(detail.getDel()).isTrue();
    }

    @Test
    @DisplayName("문의 삭제 실패 - 작성자 아님")
    void deleteInquiry_Fail_NotOwner() {
        // Given
        Long otherUserIdx = 2L;
        CustomerServiceEntity inquiry = createInquiry(otherUserIdx, false);

        given(customerServiceRepository.findByCodeAndDelFalse(INQUIRY_CODE)).willReturn(Optional.of(inquiry));

        // When & Then
        assertThatThrownBy(() -> inquiryAdminService.deleteInquiry(USER_IDX, INQUIRY_CODE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 문의의 작성자가 아닙니다.");
    }

    @Test
    @DisplayName("내 문의 목록 조회 - 성공")
    void getMyInquiryList_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        CustomerServiceEntity inquiry = createInquiry(USER_IDX, true);
        Page<CustomerServiceEntity> pageResult = new PageImpl<>(List.of(inquiry));

        given(customerServiceRepository.findAllByTypeAndUsersIdxAndDelFalse(
                CustomerServiceType.QNA_ADMIN, USER_IDX, pageable
        )).willReturn(pageResult);

        // When
        InquiryListResponse response = inquiryAdminService.getMyInquiryList(USER_IDX, pageable);

        // Then
        assertThat(response.resultCode()).isEqualTo("success");
        assertThat(response.list()).hasSize(1);
        assertThat(response.list().get(0).title()).isEqualTo(inquiry.getTitle());

        // 확인용
        System.out.println(response);
    }

    /**
     * Helper Methods
     */
    private CustomerServiceEntity createInquiry(Long userIdx, boolean isPrivate) {
        CustomerServiceEntity entity = CustomerServiceEntity.builder()
                .code(INQUIRY_CODE)
                .type(CustomerServiceType.QNA_ADMIN)
                .category(CustomerServiceCategory.SYSTEM)
                .status(CustomerServiceStatus.WAITING)
                .title("테스트 문의")
                .isPrivate(isPrivate)
                .usersIdx(userIdx)
                .isPinned(false)
                .build();
        ReflectionTestUtils.setField(entity, "idx", 1L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(entity, "del", false);
        return entity;
    }

    private CustomerServiceDetailEntity createDetail(CustomerServiceEntity parent, String detailCode) {
        CustomerServiceDetailEntity detail = CustomerServiceDetailEntity.builder()
                .detailCode(detailCode)
                .usersIdx(parent.getUsersIdx())
                .customerService(parent)
                .content("상세 내용")
                .build();
        ReflectionTestUtils.setField(detail, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(detail, "del", false);
        return detail;
    }
}