package co.kr.user.service;

import co.kr.user.DAO.UserInformationRepository;
import co.kr.user.DAO.UserRepository;
import co.kr.user.DAO.UsersLoginRepository;
import co.kr.user.model.DTO.admin.AdminUserDetailDTO;
import co.kr.user.model.DTO.admin.AdminUserListDTO;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.UsersInformation;
import co.kr.user.model.entity.UsersLogin;
import co.kr.user.model.vo.UsersRole;
import co.kr.user.util.AesUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 관리자(Admin) 전용 기능을 처리하는 서비스 클래스입니다.
 * 전체 회원 목록 조회, 회원 상세 정보 확인, 권한 수정(Role), 계정 정지(Block) 및 강제 탈퇴(Delete) 기능을 수행합니다.
 * 모든 메서드는 실행 전 요청자가 관리자 권한을 가지고 있는지 검증합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService implements AdminServiceImpl{
    private final UserRepository userRepository;
    private final AuthService authService; // 권한 확인용 서비스
    private final UserInformationRepository userInformationRepository;
    private final UsersLoginRepository usersLoginRepository;

    private final AesUtil aesUtil; // 개인정보 복호화 유틸리티

    /**
     * 전체 회원 목록을 조회하는 메서드입니다.
     * 탈퇴하지 않은(Del=0) 모든 회원을 가입일 역순으로 조회하여 반환합니다.
     * 목록 조회 성능을 위해 상세 정보는 Map으로 매핑하여 병합합니다.
     *
     * @param userIdx 요청을 보낸 관리자의 식별자
     * @return AdminUserListDTO 리스트 (회원 요약 정보)
     */
    @Override
    public List<AdminUserListDTO> userList(Long userIdx) {
        // 관리자 계정 조회 및 상태 검증
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        // 권한 검증 (ADMIN 여부 확인)
        UsersRole role = authService.getRole(users.getUsersIdx());

        if (role != UsersRole.ADMIN) {
            throw new IllegalStateException("권한이 없습니다.");
        }

        // 전체 회원 및 상세 정보 조회
        List<Users> usersList = userRepository.findAllByDelOrderByCreatedAtDesc(0);
        List<UsersInformation> usersInformationList = userInformationRepository.findAllByDel(0);

        // 상세 정보를 UserIdx를 키로 하는 Map으로 변환 (O(N) 접근을 위해)
        Map<Long, UsersInformation> userInfoMap = usersInformationList.stream()
                .collect(Collectors.toMap(UsersInformation::getUsersIdx, Function.identity()));

        // DTO 변환 및 반환
        return usersList.stream()
                .map(user -> {
                    AdminUserListDTO dto = new AdminUserListDTO();
                    dto.setID(user.getID());
                    dto.setRole(user.getRole());
                    dto.setLockedUntil(user.getLockedUntil());
                    dto.setCreatedAt(user.getCreatedAt());
                    dto.setUpdatedAt(user.getUpdatedAt());

                    // 상세 정보 매핑 및 복호화
                    UsersInformation info = userInfoMap.get(user.getUsersIdx());
                    if (info != null) {
                        dto.setName(aesUtil.decrypt(info.getName()));
                        dto.setPhoneNumber(aesUtil.decrypt(info.getPhoneNumber()));
                        dto.setBirth(aesUtil.decrypt(info.getBirth()));
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * 특정 회원의 상세 정보를 조회하는 메서드입니다.
     * 약관 동의 내역, 잔액, 개인정보(복호화 포함) 등 모든 정보를 관리자에게 제공합니다.
     *
     * @param userIdx 요청을 보낸 관리자의 식별자
     * @param id 조회 대상 회원의 아이디(이메일)
     * @return AdminUserDetailDTO (회원 상세 정보)
     */
    @Override
    public AdminUserDetailDTO userDetail(Long userIdx, String id) {
        // 관리자 검증
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        UsersRole role = authService.getRole(users.getUsersIdx());

        if (role != UsersRole.ADMIN) {
            throw new IllegalStateException("권한이 없습니다.");
        }

        // 대상 회원 조회
        Users usersDetail = userRepository.findByIDAndDel(id, 0)
                .orElseThrow(() -> new IllegalArgumentException(("존재하지 않는 아이디입니다.")));
        UsersInformation usersInformationDetail = userInformationRepository.findByUsersIdxAndDel(usersDetail.getUsersIdx(), 0)
                .orElseThrow(() -> new IllegalArgumentException("유저 상세 정보를 찾을 수 없습니다."));

        // DTO 생성 및 데이터 주입 (개인정보 복호화)
        AdminUserDetailDTO dto = new AdminUserDetailDTO();
        dto.setID(usersDetail.getID());
        dto.setLockedUntil(usersDetail.getLockedUntil());
        dto.setRole(usersDetail.getRole());
        dto.setBalance(usersDetail.getBalance());
        dto.setAgreeTermsAt(usersDetail.getAgreeTermsAt());
        dto.setAgreePrivacyAt(usersDetail.getAgreePrivacyAt());
        dto.setAgreeMarketingAt(usersDetail.getAgreeMarketingAt());
        dto.setName(aesUtil.decrypt(usersInformationDetail.getName()));
        dto.setPhoneNumber(aesUtil.decrypt(usersInformationDetail.getPhoneNumber()));
        dto.setBirth(aesUtil.decrypt(usersInformationDetail.getBirth()));
        dto.setCreatedAt(usersDetail.getCreatedAt());
        dto.setUpdatedAt(usersDetail.getUpdatedAt());

        return dto;
    }

    /**
     * 회원의 권한(Role)을 변경하는 메서드입니다.
     * 예: 일반 유저(USER) -> 판매자(SELLER), 또는 관리자(ADMIN)로 승격 등.
     *
     * @param userIdx 요청을 보낸 관리자의 식별자
     * @param id 권한을 변경할 대상 회원의 아이디
     * @param changeRole 변경할 권한 값
     * @return 처리 결과 메시지
     */
    @Override
    @Transactional
    public String userRole(Long userIdx, String id, UsersRole changeRole) {
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        UsersRole role = authService.getRole(users.getUsersIdx());

        if (role != UsersRole.ADMIN) {
            throw new IllegalStateException("권한이 없습니다.");
        }

        // 대상 회원 조회 및 권한 변경
        Users userData = userRepository.findByIDAndDel(id, 0)
                .orElseThrow(() -> new IllegalArgumentException(("존재하지 않는 아이디입니다.")));

        userData.setRole(changeRole);

        return userData.getID() + "의 권한이 " + changeRole + "으로 변경되었습니다.";
    }

    /**
     * 회원을 특정 시간까지 정지(Block)시키는 메서드입니다.
     * 로그인 제한 시간을 설정하고, 현재 유효한 리프레시 토큰을 강제로 폐기(잠금)합니다.
     *
     * @param userIdx 요청을 보낸 관리자의 식별자
     * @param id 정지할 대상 회원의 아이디
     * @param lockedUntil 정지가 해제되는 일시
     * @return 처리 결과 메시지
     */
    @Override
    @Transactional
    public String userBlock(Long userIdx, String id, LocalDateTime lockedUntil) {
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        log.info("1");
        UsersRole role = authService.getRole(users.getUsersIdx());

        if (role != UsersRole.ADMIN) {
            throw new IllegalStateException("권한이 없습니다.");
        }
        log.info("2");

        // 정지 날짜 유효성 검사
        if (lockedUntil.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("정지 해제 일시는 현재 시간보다 이후여야 합니다.");
        }
        log.info("3");

        Users userData = userRepository.findByIDAndDel(id, 0)
                .orElseThrow(() -> new IllegalArgumentException(("존재하지 않는 아이디입니다.")));

        log.info("4");
        // 계정 잠금 처리
        userData.AdminLockUser(lockedUntil);

        log.info("5");
        // 토큰 강제 만료 처리 (로그아웃 효과)
        UsersLogin usersLogin = usersLoginRepository.findFirstByUsersIdxOrderByLoginIdxDesc((userData.getUsersIdx()));

        log.info("6");
        if (usersLogin != null) {

            log.info("7");
            if (usersLogin.getRevokedAt() == null && usersLogin.getRevokeReason() == null) {

                log.info("8");
                usersLogin.lockToken(LocalDateTime.now(), "LOCKED");

                log.info("9");
            }
        }

        return userData.getID() + "의 계정이 " + lockedUntil + "까지 정지되었습니다.";
    }

    /**
     * 회원을 강제 탈퇴(삭제) 처리하는 메서드입니다.
     * 계정 상태를 탈퇴(Del=1)로 변경하고, 리프레시 토큰을 폐기합니다.
     *
     * @param userIdx 요청을 보낸 관리자의 식별자
     * @param id 탈퇴시킬 대상 회원의 아이디
     * @return 처리 결과 메시지
     */
    @Override
    @Transactional
    public String userDelete(Long userIdx, String id) {
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        UsersRole role = authService.getRole(users.getUsersIdx());

        if (role != UsersRole.ADMIN) {
            throw new IllegalStateException("권한이 없습니다.");
        }

        Users userData = userRepository.findByIDAndDel(id, 0)
                .orElseThrow(() -> new IllegalArgumentException(("존재하지 않는 아이디입니다.")));

        // 강제 탈퇴 처리
        userData.AdminUserDel();

        // 토큰 폐기 처리
        UsersLogin usersLogin = usersLoginRepository.findFirstByUsersIdxOrderByLoginIdxDesc((userData.getUsersIdx()));

        if (usersLogin != null) {
            if (usersLogin.getRevokedAt() == null && usersLogin.getRevokeReason() == null) {
                usersLogin.lockToken(LocalDateTime.now(), "LOCKED");
            }
        }

        return userData.getID() + "의 계정이 삭제되었습니다.";
    }
}