package co.kr.user.service;

import co.kr.user.DAO.PaymentRepository;
import co.kr.user.DAO.UserRepository;
import co.kr.user.model.DTO.Payment.PaymentDateOptionReq;
import co.kr.user.model.DTO.Payment.PaymentListDTO;
import co.kr.user.model.DTO.card.CardListDTO;
import co.kr.user.model.entity.Payment;
import co.kr.user.model.entity.UserCard;
import co.kr.user.model.entity.Users;
import co.kr.user.model.vo.PaymentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 사용자의 예치금(포인트) 잔액 및 결제/충전 내역을 관리하는 서비스 클래스입니다.
 * 현재 잔액 조회, 전체 충전 내역 조회, 기간별 내역 조회 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
public class BalanceService implements BalanceServiceImpl{
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    /**
     * 사용자의 현재 보유 잔액을 조회하는 메서드입니다.
     *
     * @param userIdx 로그인한 사용자의 식별자
     * @return 현재 잔액 (BigDecimal)
     */
    @Override
    public BigDecimal balance(Long userIdx) {
        // 사용자 조회
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        // 계정 상태 검증 (탈퇴: 1, 미인증: 2)
        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        // Users 엔티티에 저장된 잔액 반환
        return users.getBalance();
    }

    /**
     * 사용자의 전체 예치금 충전(DEPOSIT) 내역을 조회하는 메서드입니다.
     * 최근 내역이 상단에 오도록 정렬하여 반환합니다.
     *
     * @param userIdx 로그인한 사용자의 식별자
     * @return 충전 내역 리스트 (PaymentListDTO)
     */
    @Override
    public List<PaymentListDTO> balanceHistory(Long userIdx) {
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        // 'DEPOSIT' 타입의 결제 내역만 조회 (충전 내역)
        List<Payment> paymentList = paymentRepository.findAllByUsersIdxAndTypeOrderByCreatedAtDesc(users.getUsersIdx(), PaymentType.DEPOSIT);

        if (paymentList.isEmpty()) {
            throw new IllegalArgumentException("예치금 결제 내역이 없습니다.");
        }

        // Entity -> DTO 변환
        return paymentList.stream()
                .map(payment -> {
                    PaymentListDTO dto = new PaymentListDTO();
                    dto.setStatus(payment.getStatus());
                    dto.setAmount(payment.getAmount());
                    dto.setCreatedAt(payment.getCreatedAt());
                    return dto;
                })
                .toList();
    }

    /**
     * 특정 기간(옵션) 동안의 예치금 충전 내역을 조회하는 메서드입니다.
     * 시작일(StartDate)과 종료일(EndDate)을 지정하여 검색할 수 있습니다.
     *
     * @param userIdx 로그인한 사용자의 식별자
     * @param paymentDateOptionReq 조회할 기간 정보 (시작일, 종료일)
     * @return 해당 기간의 충전 내역 리스트
     */
    @Override
    public List<PaymentListDTO> balanceHistoryOption(Long userIdx, PaymentDateOptionReq paymentDateOptionReq) {
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        // 시작일이 없으면 가입일(최초 생성일)로 설정
        if (paymentDateOptionReq.getStartDate() == null) {
            paymentDateOptionReq.setStartDate(users.getCreatedAt());
        }

        // 종료일이 없거나 미래인 경우 현재 시간으로 설정
        if (paymentDateOptionReq.getEndDate() == null || paymentDateOptionReq.getEndDate().isAfter(LocalDateTime.now())) {
            paymentDateOptionReq.setEndDate(LocalDateTime.now());
        }

        // 기간 및 타입(DEPOSIT) 조건으로 내역 조회
        List<Payment> paymentList = paymentRepository.findAllByUsersIdxAndTypeAndCreatedAtBetweenOrderByCreatedAtDesc(users.getUsersIdx(), PaymentType.DEPOSIT,
                paymentDateOptionReq.getStartDate(), paymentDateOptionReq.getEndDate());

        if (paymentList.isEmpty()) {
            throw new IllegalArgumentException("예치금 결제 내역이 없습니다.");
        }

        return paymentList.stream()
                .map(payment -> {
                    PaymentListDTO dto = new PaymentListDTO();
                    dto.setStatus(payment.getStatus());
                    dto.setAmount(payment.getAmount());
                    dto.setCreatedAt(payment.getCreatedAt());
                    return dto;
                })
                .toList();
    }
}