package co.kr.costomerservice.inquiryAdmin.service;

import co.kr.costomerservice.client.AuthServiceClient;
import co.kr.costomerservice.common.dto.response.ResultResponse;
import co.kr.costomerservice.common.entity.CustomerServiceDetailEntity;
import co.kr.costomerservice.common.entity.CustomerServiceEntity;
import co.kr.costomerservice.common.repository.CustomerServiceDetailRepository;
import co.kr.costomerservice.common.repository.CustomerServiceRepository;
import co.kr.costomerservice.common.vo.CustomerServiceCategory;
import co.kr.costomerservice.common.vo.CustomerServiceStatus;
import co.kr.costomerservice.common.vo.CustomerServiceType;
import co.kr.costomerservice.inquiryAdmin.dto.request.InquiryAnswerDeleteRequest;
import co.kr.costomerservice.inquiryAdmin.dto.request.InquiryAnswerUpsertRequest;
import co.kr.costomerservice.inquiryAdmin.dto.request.InquiryUpsertRequest;
import co.kr.costomerservice.inquiryAdmin.dto.response.InquiryDetailResponse;
import co.kr.costomerservice.inquiryAdmin.dto.response.InquiryListResponse;
import co.kr.costomerservice.inquiryAdmin.service.impl.InquiryAdminManagementServiceImpl;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InquiryAdminManagementServiceTest {

    @InjectMocks
    InquiryAdminManagementServiceImpl managementService;

    @Mock CustomerServiceRepository customerServiceRepository;
    @Mock CustomerServiceDetailRepository customerServiceDetailRepository;
    @Mock AuthServiceClient authServiceClient;

    final Long ADMIN_IDX = 99L;
    final Long USER_IDX = 1L;
    final String INQUIRY_CODE = "INQ-001";
    final String DETAIL_CODE = "DETAIL-001";

    @Test
    @DisplayName("관리자 문의 목록 조회 - 성공")
    void getInquiryList_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        CustomerServiceEntity inquiry = createInquiryEntity(INQUIRY_CODE, CustomerServiceType.QNA_ADMIN);
        Page<CustomerServiceEntity> pageResult = new PageImpl<>(List.of(inquiry));

        given(authServiceClient.getUserRole(ADMIN_IDX).getBody()).willReturn("ADMIN");
        given(customerServiceRepository.findAllByTypeAndDelFalse(CustomerServiceType.QNA_ADMIN, pageable))
                .willReturn(pageResult);

        // When
        InquiryListResponse response = managementService.getInquiryList(pageable, ADMIN_IDX);

        // Then
        assertThat(response.resultCode()).isEqualTo("success");
        assertThat(response.list()).hasSize(1);
        assertThat(response.list().get(0).code()).isEqualTo(INQUIRY_CODE);

        // 확인용
        System.out.println(response);
    }

    @Test
    @DisplayName("관리자 문의 답변 등록 - 성공")
    void addInquiryAnswer_Success() {
        // Given
        CustomerServiceEntity inquiry = createInquiryEntity(INQUIRY_CODE, CustomerServiceType.QNA_ADMIN);

        CustomerServiceDetailEntity questionDetail = createDetailEntity(inquiry, DETAIL_CODE);
        ReflectionTestUtils.setField(questionDetail, "detailIdx", 10L);

        List<CustomerServiceDetailEntity> detailList = new ArrayList<>();
        detailList.add(questionDetail);

        InquiryAnswerUpsertRequest request = new InquiryAnswerUpsertRequest(DETAIL_CODE, "답변입니다.");

        given(authServiceClient.getUserRole(ADMIN_IDX).getBody()).willReturn("ADMIN");
        given(customerServiceRepository.findByCodeAndDelFalse(INQUIRY_CODE)).willReturn(Optional.of(inquiry));
        given(customerServiceDetailRepository.findAllByCustomerServiceAndDelFalse(inquiry)).willReturn(detailList);

        // When
        InquiryDetailResponse response = managementService.addInquiryAnswer(ADMIN_IDX, INQUIRY_CODE, request);

        // Then
        verify(customerServiceDetailRepository).save(any(CustomerServiceDetailEntity.class));
        assertThat(inquiry.getStatus()).isEqualTo(CustomerServiceStatus.ANSWERED);
        assertThat(detailList).hasSize(2); // 원글 + 답변

        // 확인용
        System.out.println(response);
    }

    @Test
    @DisplayName("관리자 문의 답변 삭제 - 성공")
    void deleteInquiryAnswer_Success() {
        // Given
        InquiryAnswerDeleteRequest request = new InquiryAnswerDeleteRequest(DETAIL_CODE);
        CustomerServiceDetailEntity detail = CustomerServiceDetailEntity.builder()
                .detailCode(DETAIL_CODE)
                .content("삭제될 답변")
                .build();
        ReflectionTestUtils.setField(detail, "del", false); // 초기상태

        given(authServiceClient.getUserRole(ADMIN_IDX).getBody()).willReturn("ADMIN");
        given(customerServiceDetailRepository.findByDetailCodeAndDelFalse(DETAIL_CODE))
                .willReturn(Optional.of(detail));

        // When
        ResultResponse response = managementService.deleteInquiryAnswer(INQUIRY_CODE, request, ADMIN_IDX);

        // Then
        assertThat(response.resultCode()).isEqualTo("success");
        assertThat(detail.getDel()).isTrue(); // Soft Delete 확인

        // 확인용
        System.out.println(response);
    }

    @Test
    @DisplayName("문의 내용 수정 (관리자) - 성공")
    void updateInquiry_Success() {
        // Given
        // *중요* 구현 코드 로직상 inquiryCode와 detailCode가 같아야 해당 Detail을 찾음
        // .filter(detail -> inquiryCode.equals(detail.getDetailCode()))
        CustomerServiceEntity inquiry = createInquiryEntity(INQUIRY_CODE, CustomerServiceType.QNA_ADMIN);
        CustomerServiceDetailEntity detail = createDetailEntity(inquiry, INQUIRY_CODE); // 코드를 일치시킴

        InquiryUpsertRequest request = new InquiryUpsertRequest(
                INQUIRY_CODE,
                CustomerServiceCategory.ETC,
                "수정제목",
                "수정내용",
                true
        );

        given(authServiceClient.getUserRole(ADMIN_IDX).getBody()).willReturn("ADMIN");
        given(customerServiceRepository.findByCodeAndDelFalse(INQUIRY_CODE)).willReturn(Optional.of(inquiry));
        given(customerServiceDetailRepository.findAllByCustomerServiceAndDelFalse(inquiry)).willReturn(List.of(detail));

        // When
        InquiryDetailResponse response = managementService.updateInquiry(INQUIRY_CODE, request, ADMIN_IDX);

        // Then
        assertThat(inquiry.getTitle()).isEqualTo("수정제목");
        assertThat(detail.getContent()).isEqualTo("수정내용");
        assertThat(inquiry.getCategory()).isEqualTo(CustomerServiceCategory.ETC);

        // 확인용
        System.out.println(response);
    }

    @Test
    @DisplayName("문의 내용 삭제 (관리자) - 성공")
    void deleteInquiry_Success() {
        // Given
        CustomerServiceEntity inquiry = createInquiryEntity(INQUIRY_CODE, CustomerServiceType.QNA_ADMIN);
        CustomerServiceDetailEntity detail1 = createDetailEntity(inquiry, "D1");
        CustomerServiceDetailEntity detail2 = createDetailEntity(inquiry, "D2");
        List<CustomerServiceDetailEntity> detailList = List.of(detail1, detail2);

        given(authServiceClient.getUserRole(ADMIN_IDX).getBody()).willReturn("ADMIN");
        given(customerServiceRepository.findByCodeAndDelFalse(INQUIRY_CODE)).willReturn(Optional.of(inquiry));
        given(customerServiceDetailRepository.findAllByCustomerServiceAndDelFalse(inquiry)).willReturn(detailList);

        // When
        ResultResponse response = managementService.deleteInquiry(INQUIRY_CODE, ADMIN_IDX);

        // Then
        assertThat(inquiry.getDel()).isTrue();
        assertThat(detail1.getDel()).isTrue();
        assertThat(detail2.getDel()).isTrue();

        // 확인용
        System.out.println(response);
    }

    @Test
    @DisplayName("실패 케이스 - 관리자 권한 없음")
    void fail_Unauthorized() {
        // Given
        Long fakeAdminIdx = 1L;
        given(authServiceClient.getUserRole(fakeAdminIdx).getBody()).willReturn("USER");

        // When & Then
        assertThatThrownBy(() -> managementService.deleteInquiry("code", fakeAdminIdx))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("관리자 권한이 없습니다.");
    }

    @Test
    @DisplayName("실패 케이스 - 문의 타입 불일치 (QNA_ADMIN 아님)")
    void fail_InvalidType() {
        // Given
        // QNA_PRODUCT 타입의 글을 관리자 문의 기능으로 수정하려고 할 때
        CustomerServiceEntity productQna = createInquiryEntity(INQUIRY_CODE, CustomerServiceType.QNA_PRODUCT);

        given(authServiceClient.getUserRole(ADMIN_IDX).getBody()).willReturn("ADMIN");
        given(customerServiceRepository.findByCodeAndDelFalse(INQUIRY_CODE))
                .willReturn(Optional.of(productQna));

        // When & Then
        assertThatThrownBy(() -> managementService.updateInquiry(INQUIRY_CODE, null, ADMIN_IDX))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 게시글은 문의가 아닙니다.");
    }

    /**
     * Helper Methods
     */
    private CustomerServiceEntity createInquiryEntity(String code, CustomerServiceType type) {
        CustomerServiceEntity entity = CustomerServiceEntity.builder()
                .code(code)
                .type(type)
                .category(CustomerServiceCategory.SYSTEM)
                .status(CustomerServiceStatus.WAITING)
                .title("원본제목")
                .isPrivate(false)
                .usersIdx(USER_IDX)
                .isPinned(false)
                .build();
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(entity, "del", false);
        return entity;
    }

    private CustomerServiceDetailEntity createDetailEntity(CustomerServiceEntity parent, String detailCode) {
        CustomerServiceDetailEntity entity = CustomerServiceDetailEntity.builder()
                .detailCode(detailCode)
                .usersIdx(parent.getUsersIdx())
                .customerService(parent)
                .content("원본내용")
                .build();
        ReflectionTestUtils.setField(entity, "del", false);
        return entity;
    }
}