package mallang_trip.backend.domain.admin.service;

import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.admin.dto.UserInfoForAdminResponse;
import mallang_trip.backend.domain.admin.dto.GrantAdminRoleRequest;
import mallang_trip.backend.domain.driver.entity.Driver;
import mallang_trip.backend.domain.driver.repository.DriverRepository;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.user.repository.UserRepository;
import mallang_trip.backend.global.io.BaseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static mallang_trip.backend.domain.driver.exception.DriverExceptionStatus.CANNOT_FOUND_DRIVER;
import static mallang_trip.backend.domain.user.constant.Role.*;
import static mallang_trip.backend.domain.user.exception.UserExceptionStatus.CANNOT_FOUND_USER;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAdminService {

    private final UserRepository userRepository;
    private final SuspensionService suspensionService;

    private final DriverRepository driverRepository;

    /**
     * (관리자) 회원 정보 목록 조회
     */
    public List<UserInfoForAdminResponse> getUserList(String nicknameOrId){
        if(nicknameOrId == null){
            return userRepository.findAll().stream()
                    .map(user-> {
                        if(user.getRole().equals(ROLE_DRIVER)){
                            Driver driver = driverRepository.findByUser(user)
                                    .orElseThrow(() -> new BaseException(CANNOT_FOUND_DRIVER));
                            return UserInfoForAdminResponse.of(user,0,driver.getRegion());
                        }
                        return UserInfoForAdminResponse.of(user,suspensionService.getSuspensionDuration(user));
                    })
                    .collect(Collectors.toList());
        }
        return userRepository.findByNicknameContainingIgnoreCaseOrLoginIdContainingIgnoreCase(nicknameOrId,nicknameOrId).stream()
                .map(user-> UserInfoForAdminResponse.of(user,suspensionService.getSuspensionDuration(user)))
                .collect(Collectors.toList());
    }

    /**
     * (관리자) 관리자 권한 회원 목록 조회
     *
     */
    public List<UserInfoForAdminResponse> getAdminList(){
        return userRepository.findByRole(ROLE_ADMIN).stream()
                .map(user-> UserInfoForAdminResponse.of(user,0))
                .collect(Collectors.toList());
    }


    /**
     * (관리자) 회원 관리자 권한 부여
     *
     */
    public void grantAdminRole(GrantAdminRoleRequest request) {
        // List<Integer> userIds
        List<Long> userIds = request.getUserIds();
        for (Long userId : userIds) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BaseException(CANNOT_FOUND_USER));
            if (!user.getRole().equals(ROLE_ADMIN)) {
                user.setRole(ROLE_ADMIN);
            }
        }
    }

    /**
     * (관리자) 회원 관리자 권한 해제
     *
     */
    public void revokeAdminRole(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(CANNOT_FOUND_USER));
        if(user.getRole().equals(ROLE_ADMIN)){
            user.setRole(ROLE_USER);
        }
    }
}
