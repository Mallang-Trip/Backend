package mallang_trip.backend.domain.party.service;

import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.admin.service.SuspensionService;
import mallang_trip.backend.domain.driver.constant.DriverStatus;
import mallang_trip.backend.domain.driver.entity.Driver;
import mallang_trip.backend.domain.driver.repository.DriverRepository;
import mallang_trip.backend.domain.party.dto.PartyRegionDriversResponse;
import mallang_trip.backend.domain.party.dto.PartyRegionRequest;
import mallang_trip.backend.domain.party.dto.PartyRegionResponse;
import mallang_trip.backend.domain.party.entity.PartyRegion;
import mallang_trip.backend.domain.party.repository.PartyRegionRepository;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.user.repository.UserRepository;
import mallang_trip.backend.global.io.BaseException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static mallang_trip.backend.domain.party.exception.PartyExceptionStatus.*;
import static mallang_trip.backend.domain.user.constant.Role.ROLE_DRIVER;

@Service
@RequiredArgsConstructor
@Transactional
public class PartyRegionService {

    private final PartyRegionRepository partyRegionRepository;

    private final DriverRepository driverRepository;

    private final UserRepository userRepository;

    private final SuspensionService suspensionService;

    /**
     * (관리자) 가고 싶은 지역 추가
     */
    public void addRegion(PartyRegionRequest request) {
        if (partyRegionRepository.existsByRegion(request.getRegion())) {
            throw new BaseException(REGION_ALREADY_EXISTS);
        }

        partyRegionRepository.save(PartyRegion.builder()
            .region(request.getRegion())
            .regionImg(request.getRegionImg())
            .build());
    }

    /**
     * (관리자) 가고 싶은 지역 삭제
     */
    public void deleteRegion(Long region_id) {
        PartyRegion partyRegion = partyRegionRepository.findById(region_id).
            orElseThrow(() -> new BaseException(REGION_NOT_FOUND));

        if (!partyRegion.isZero()) {
            throw new BaseException(REGION_NOT_EMPTY);
        }

        partyRegionRepository.delete(partyRegion);
    }

    /**
     * (관리자) 가고 싶은 지역 수정
     */
    public void updateRegion(Long region_id, PartyRegionRequest request) {
        PartyRegion partyRegion = partyRegionRepository.findById(region_id).
            orElseThrow(() -> new BaseException(REGION_NOT_FOUND));

        // 지역명 변경 시, 활동중인 드라이버 확인
        if(!partyRegion.getRegion().equals(request.getRegion()) && !partyRegion.isZero()){
            throw new BaseException(REGION_NOT_EMPTY);
        }

        partyRegion.modify(request.getRegion(), request.getRegionImg());
    }

    /**
     * 가고 싶은 지역 조회
     */
    public List<PartyRegionResponse> getRegions(String region) {
        List<PartyRegion> partyRegions;
        if (region == null) {
            partyRegions = partyRegionRepository.findAllByOrderByRegionAsc();
        } else {
            PartyRegion partyRegion = partyRegionRepository.findByRegion(region).
                orElseThrow(() -> new BaseException(REGION_NOT_FOUND));
            partyRegions = List.of(partyRegion);
        }
        return partyRegions.stream().map(PartyRegionResponse::of).collect(Collectors.toList());
    }

    /**
     * (관리자) 가고 싶은 지역 드라이버 목록 페이지
     */
    public List<PartyRegionDriversResponse> getDrivers(Long region_id, String driverNicknameOrId) {
        PartyRegion partyRegion = partyRegionRepository.findById(region_id).
            orElseThrow(() -> new BaseException(REGION_NOT_FOUND));

        List<Driver> driver;
        if (driverNicknameOrId == null) {
            driver = driverRepository.findAllByRegionAndStatus(partyRegion.getRegion(),
                DriverStatus.ACCEPTED);
        } else {
            List<User> users = userRepository.findByNicknameContainingIgnoreCaseOrLoginIdContainingIgnoreCase(
                driverNicknameOrId, driverNicknameOrId);
            driver = users.stream()
                .filter(user -> user.getRole().equals(ROLE_DRIVER))
                .map(user -> driverRepository.findByUser(user).get())
                .collect(Collectors.toList());
        }

        return driver.stream().map(d -> {
            Integer duration = suspensionService.getSuspensionDuration(d.getUser());
            return PartyRegionDriversResponse.of(d, duration);
        }).collect(Collectors.toList());
    }
}
