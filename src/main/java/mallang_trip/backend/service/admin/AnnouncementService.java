package mallang_trip.backend.service.admin;

import static mallang_trip.backend.controller.io.BaseResponseStatus.Not_Found;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.constant.AnnouncementType;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.dto.admin.AnnouncementBriefResponse;
import mallang_trip.backend.domain.dto.admin.AnnouncementDetailsResponse;
import mallang_trip.backend.domain.dto.admin.AnnouncementIdResponse;
import mallang_trip.backend.domain.dto.admin.AnnouncementRequest;
import mallang_trip.backend.domain.entity.admin.Announcement;
import mallang_trip.backend.repository.admin.AnnouncementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AnnouncementService {

	private final AnnouncementRepository announcementRepository;

	/**
	 * (관리자)작성
	 */
	public AnnouncementIdResponse create(AnnouncementRequest request) {
		Announcement announcement = announcementRepository.save(Announcement.builder()
			.title(request.getTitle())
			.content(request.getContent())
			.images(request.getImages())
			.type(request.getType())
			.build());
		return AnnouncementIdResponse.builder().announcementId(announcement.getId()).build();
	}

	/**
	 * (관리자)삭제
	 */
	public void delete(Long announcementId) {
		announcementRepository.deleteById(announcementId);
	}

	/**
	 * 목록 조회
	 */
	public Page<AnnouncementBriefResponse> get(AnnouncementType type, Pageable pageable) {
		Page<Announcement> announcements = announcementRepository.findByTypeOrderByCreatedAtDesc(
			type, pageable);
		List<AnnouncementBriefResponse> responses = announcements.stream()
			.map(AnnouncementBriefResponse::of)
			.collect(Collectors.toList());
		return new PageImpl<>(responses, pageable, announcements.getTotalElements());
	}

	/**
	 * 상세 조회
	 */
	public AnnouncementDetailsResponse view(Long announcementId) {
		Announcement announcement = announcementRepository.findById(announcementId)
			.orElseThrow(() -> new BaseException(Not_Found));
		return AnnouncementDetailsResponse.of(announcement);
	}
}
