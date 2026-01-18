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
import co.kr.user.util.AESUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService implements AdminServiceImpl{
    private final UserRepository userRepository;
    private final AuthService authService;
    private final UserInformationRepository userInformationRepository;
    private final UsersLoginRepository usersLoginRepository;

    private final AESUtil aesUtil;

    @Override
    public List<AdminUserListDTO> userList(Long userIdx) {
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


        List<Users> usersList = userRepository.findAllByDelOrderByCreatedAtDesc(0);
        List<UsersInformation> usersInformationList = userInformationRepository.findAllByDel(0);


        Map<Long, UsersInformation> userInfoMap = usersInformationList.stream()
                .collect(Collectors.toMap(UsersInformation::getUsersIdx, Function.identity()));

        return usersList.stream()
                .map(user -> {
                    AdminUserListDTO dto = new AdminUserListDTO();
                    dto.setID(user.getID());
                    dto.setRole(user.getRole());
                    dto.setLockedUntil(user.getLockedUntil());
                    dto.setCreatedAt(user.getCreatedAt());
                    dto.setUpdatedAt(user.getUpdatedAt());

                    UsersInformation info = userInfoMap.get(user.getUsersIdx());

                    if (info != null) {
//                        dto.setName(aesUtil.decrypt(info.getName()));
//                        dto.setPhoneNumber(aesUtil.decrypt(info.getPhoneNumber()));
//                        dto.setBirth(aesUtil.decrypt(info.getBirth()));
                        dto.setName(info.getName());
                        dto.setPhoneNumber(info.getPhoneNumber());
                        dto.setBirth(info.getBirth());
                    }

                    return dto;

                })
                .collect(Collectors.toList());
    }

    @Override
    public AdminUserDetailDTO userDetail(Long userIdx, String id) {
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


        Users usersDetail = userRepository.findByIDAndDel(id, 0)
                .orElseThrow(() -> new IllegalArgumentException(("존재하지 않는 아이디입니다.")));
        UsersInformation usersInformationDetail = userInformationRepository.findByUsersIdxAndDel(usersDetail.getUsersIdx(), 0)
                .orElseThrow(() -> new IllegalArgumentException("유저 상세 정보를 찾을 수 없습니다."));

        AdminUserDetailDTO dto = new AdminUserDetailDTO();
        dto.setID(usersDetail.getID());
        dto.setLockedUntil(usersDetail.getLockedUntil());
        dto.setRole(usersDetail.getRole());
        dto.setBalance(usersDetail.getBalance());
        dto.setAgreeTermsAt(usersDetail.getAgreeTermsAt());
        dto.setAgreePrivacyAt(usersDetail.getAgreePrivacyAt());
        dto.setAgreeMarketingAt(usersDetail.getAgreeMarketingAt());
//        dto.setName(aesUtil.decrypt(usersInformationDetail.getName()));
//        dto.setPhoneNumber(aesUtil.decrypt(usersInformationDetail.getPhoneNumber()));
//        dto.setBirth(aesUtil.decrypt(usersInformationDetail.getBirth()));
        dto.setName(usersInformationDetail.getName());
        dto.setPhoneNumber(usersInformationDetail.getPhoneNumber());
        dto.setBirth(usersInformationDetail.getBirth());
        dto.setCreatedAt(usersDetail.getCreatedAt());
        dto.setUpdatedAt(usersDetail.getUpdatedAt());

        return dto;
    }

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

        Users userData = userRepository.findByIDAndDel(id, 0)
                .orElseThrow(() -> new IllegalArgumentException(("존재하지 않는 아이디입니다.")));

        userData.setRole(changeRole);

        return userData.getID() + "의 권한이 " + changeRole + "으로 변경되었습니다.";
    }

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

        UsersRole role = authService.getRole(users.getUsersIdx());

        if (role != UsersRole.ADMIN) {
            throw new IllegalStateException("권한이 없습니다.");
        }

        if (lockedUntil.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("정지 해제 일시는 현재 시간보다 이후여야 합니다.");
        }

        Users userData = userRepository.findByIDAndDel(id, 0)
                .orElseThrow(() -> new IllegalArgumentException(("존재하지 않는 아이디입니다.")));

        userData.AdminLockUser(lockedUntil);

        UsersLogin usersLogin = usersLoginRepository.findFirstByUsersIdxOrderByLoginIdxDesc((userData.getUsersIdx()));

        if (usersLogin != null) {
            if (usersLogin.getRevokedAt() == null && usersLogin.getRevokeReason() == null) {
                usersLogin.lockToken(LocalDateTime.now(), "LOCKED");
            }
        }

        return userData.getID() + "의 계정이 " + lockedUntil + "까지 정지되었습니다.";
    }

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

        userData.AdminUserDel();

        UsersLogin usersLogin = usersLoginRepository.findFirstByUsersIdxOrderByLoginIdxDesc((userData.getUsersIdx()));

        if (usersLogin != null) {
            if (usersLogin.getRevokedAt() == null && usersLogin.getRevokeReason() == null) {
                usersLogin.lockToken(LocalDateTime.now(), "LOCKED");
            }
        }

        return userData.getID() + "의 계정이 삭제되었습니다.";
    }
}