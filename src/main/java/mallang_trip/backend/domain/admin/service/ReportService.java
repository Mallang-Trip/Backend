package mallang_trip.backend.domain.admin.service;

import static mallang_trip.backend.domain.admin.constant.ReportStatus.COMPLETE;
import static mallang_trip.backend.domain.admin.constant.ReportStatus.WAITING;
import static mallang_trip.backend.global.io.BaseResponseStatus.Not_Found;
import static mallang_trip.backend.domain.user.exception.UserExceptionStatus.CANNOT_FOUND_USER;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.admin.dto.*;
import mallang_trip.backend.domain.admin.entity.Suspension;
import mallang_trip.backend.domain.admin.repository.SuspensionRepository;
import mallang_trip.backend.domain.user.service.CurrentUserService;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.domain.admin.entity.Report;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.admin.repository.ReportRepository;
import mallang_trip.backend.domain.user.repository.UserRepository;
import mallang_trip.backend.domain.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

	private final CurrentUserService currentUserService;
	private final UserRepository userRepository;
	private final ReportRepository reportRepository;

	private final SuspensionRepository suspensionRepository;

	/**
	 * 신고
	 */
	public void create(ReportRequest request) {
		User reportee = userRepository.findById(request.getReporteeId())
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_USER));
		reportRepository.save(Report.builder()
			.reporter(currentUserService.getCurrentUser())
			.reportee(reportee)
			.content(request.getContent())
			.type(request.getType())
			.targetId(request.getTargetId())
			.build());
	}

	/**
	 * 신고 대기 목록 조회
	 */
	public List<ReportBriefResponse> getReports() {
		return reportRepository.findByStatusOrderByCreatedAtDesc(WAITING)
			.stream()
			.map(ReportBriefResponse::of)
			.collect(Collectors.toList());
	}

	/**
	 * 대기중인 신고 상세 조회
	 */
	public ReportDetailsResponse viewReport(Long reportId) {
		Report report = reportRepository.findById(reportId)
			.orElseThrow(() -> new BaseException(Not_Found));
		return ReportDetailsResponse.of(report);
	}

	/**
	 * 신고 완료 처리
	 */
	public void complete(Long reportId) {
		Report report = reportRepository.findById(reportId)
			.orElseThrow(() -> new BaseException(Not_Found));
		report.setStatus(COMPLETE);
	}

	/**
	 * 처리 완료된 신고 목록 조회
	 */
	public List<ReportCompleteBriefResponse> getCompleteReports() {
		List<Report> reports = reportRepository.findByStatusOrderByCreatedAtDesc(COMPLETE);

		return reports.stream()
			.map(report-> {
				Suspension suspension = suspensionRepository.findByReportId(report.getId())
					.orElse(null);
				return ReportCompleteBriefResponse.of(report, suspension);
			})
			.collect(Collectors.toList()); // 순서는 보장
	}

	/**
	 * 처리 완료된 신고 상세 조회
	 */
	public ReportCompleteDetailsResponse viewCompleteReport(Long reportId) {
		Report report = reportRepository.findById(reportId)
			.orElseThrow(() -> new BaseException(Not_Found));
		Suspension suspension = suspensionRepository.findByReportId(reportId)
			.orElse(null);
		return ReportCompleteDetailsResponse.of(report, suspension);
	}

}
