package mallang_trip.backend.domain.admin.service;


import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.admin.dto.ObjectionBriefResponse;
import mallang_trip.backend.domain.admin.dto.ObjectionDetailsResponse;
import mallang_trip.backend.domain.admin.dto.ObjectionRequest;
import mallang_trip.backend.domain.admin.entity.Objection;
import mallang_trip.backend.domain.admin.repository.ObjectionRepository;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.user.repository.UserRepository;
import mallang_trip.backend.domain.user.service.CurrentUserService;
import mallang_trip.backend.global.io.BaseException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static mallang_trip.backend.domain.admin.constant.ObjectionStatus.COMPLETE;
import static mallang_trip.backend.domain.admin.constant.ObjectionStatus.WAITING;
import static mallang_trip.backend.domain.user.exception.UserExceptionStatus.CANNOT_FOUND_USER;
import static mallang_trip.backend.global.io.BaseResponseStatus.Not_Found;

@Service
@RequiredArgsConstructor
@Transactional
public class ObjectionService {

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;

    private final ObjectionRepository objectionRepository;

    /** 이의제기 목록 조회
     *
     */
    public List<ObjectionBriefResponse> getObjections() {
        List<Objection> objections = Stream.concat(
                objectionRepository.findByStatusOrderByCreatedAtDesc(WAITING).stream(),
                objectionRepository.findByStatusOrderByCreatedAtDesc(COMPLETE).stream())
                .collect(Collectors.toList());
        return objections.stream()
                .map(ObjectionBriefResponse::of)
                .collect(Collectors.toList());
    }

    /** 이의제기 상세 조회
     *
     * @param objectionId
     */
    public ObjectionDetailsResponse viewObjection(Long objectionId) {
        Objection objection = objectionRepository.findById(objectionId)
                .orElseThrow(() -> new BaseException(Not_Found));

        return ObjectionDetailsResponse.of(objection);
    }

    /** 이의제기 처리하기
     *
     * @param objectionId
     */
    public void complete(Long objectionId) {
        Objection objection = objectionRepository.findById(objectionId)
                .orElseThrow(() -> new BaseException(Not_Found));
        objection.setStatus(COMPLETE);
    }

    /** 고객 이의제기하기
     *
     * @param request
     */
    public void create(ObjectionRequest request) {
        objectionRepository.save(Objection.builder()
                .objector(currentUserService.getCurrentUser())
                .content(request.getContent())
//                        .report(reportService.getReport(request.getReportId()))
                .build());
    }


}
