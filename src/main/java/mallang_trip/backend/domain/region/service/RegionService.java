package mallang_trip.backend.domain.region.service;

import static mallang_trip.backend.domain.region.exception.RegionException.*;

import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.admin.service.SuspensionService;
import mallang_trip.backend.domain.driver.repository.DriverRepository;
import mallang_trip.backend.domain.region.dto.RegionDriverResponse;
import mallang_trip.backend.domain.region.dto.RegionRequest;
import mallang_trip.backend.domain.region.dto.RegionResponse;
import mallang_trip.backend.domain.region.entity.Region;
import mallang_trip.backend.domain.region.repository.RegionRepository;
import mallang_trip.backend.global.io.BaseException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class RegionService {

	private final RegionRepository regionRepository;
	private final DriverRepository driverRepository;
	private final SuspensionService suspensionService;

	/**
	 * 가고 싶은 지역 추가
	 * @param request 지역 정보를 담은 RegionRequest DTO
	 */
	public void create(RegionRequest request) {
		if (regionRepository.existsByName(request.getName())) {
			throw new BaseException(REGION_ALREADY_EXISTS);
		}

		regionRepository.save(Region.builder()
			.name(request.getName())
			.image(request.getImage())
			.province(request.getProvince())
			.build());
	}

	/**
	 * 가고 싶은 지역 삭제
	 * @param regionId 삭제할 지역 ID 값
	 */
	public void delete(Long regionId) {
		Region region = regionRepository.findById(regionId)
			.orElseThrow(() -> new BaseException(REGION_NOT_FOUND));

		// 활동중인 드라이버 유무 확인
		if (driverRepository.existsByRegion(region.getName())) {
			throw new BaseException(REGION_NOT_EMPTY);
		}
		regionRepository.delete(region);
	}

	/**
	 * 가고 싶은 지역 수정
	 * @param regionId 수정할 지역 ID 값
	 * @param request 수정 정보를 담은 RegionRequest DTO
	 */
	public void modify(Long regionId, RegionRequest request) {
		Region region = regionRepository.findById(regionId)
			.orElseThrow(() -> new BaseException(REGION_NOT_FOUND));

		// 지역명 변경 시, 활동중인 드라이버 유무 확인
		if (!region.getName().equals(request.getName())
			&& driverRepository.existsByRegion(region.getName())) {
			throw new BaseException(REGION_NOT_EMPTY);
		}

		region.modify(request.getName(), request.getImage(), request.getProvince());
	}

	/**
	 * 가고 싶은 지역 검색
	 * @param keyword 검색 키워드 (null -> 전체 검색)
	 * @return 가고 싶은 지역 정보를 담은 RegionResponse DTO 배열
	 */
	public List<RegionResponse> search(String keyword) {
		if (keyword == null) {
			return regionRepository.findAllByOrderByNameAsc().stream()
				.map(RegionResponse::of)
				.collect(Collectors.toList());
		}

		return regionRepository.findByNameContaining(keyword).stream()
			.map(RegionResponse::of)
			.collect(Collectors.toList());
	}

	/**
	 * 지역 별 드라이버 목록 조회
	 * @param regionId 조회할 지역 ID 값
	 * @return 드라이버 정보를 담은 RegionDriverResponse DTO 배열
	 */
	public List<RegionDriverResponse> getDrivers(Long regionId){
		Region region = regionRepository.findById(regionId)
			.orElseThrow(() -> new BaseException(REGION_NOT_FOUND));

		return driverRepository.findByRegionContaining(region.getName())
			.stream()
			.map(driver -> {
				Integer duration = suspensionService.getSuspensionDuration(driver.getUser());
				return RegionDriverResponse.of(driver, duration);
			})
			.collect(Collectors.toList());
	}
}
