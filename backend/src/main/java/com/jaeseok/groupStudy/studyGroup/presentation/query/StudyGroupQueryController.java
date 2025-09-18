package com.jaeseok.groupStudy.studyGroup.presentation.query;

import com.jaeseok.groupStudy.studyGroup.application.query.StudyGroupQueryService;
import com.jaeseok.groupStudy.studyGroup.domain.GroupState;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.query.dto.StudyGroupDetailDto;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.query.dto.StudyGroupSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/study-groups")
@RequiredArgsConstructor
public class StudyGroupQueryController {

    private final StudyGroupQueryService studyGroupQueryService;

    /**
     * 스터디 그룹 목록 조회(페이징)
     * @param state URL 쿼리 파라미터(?state=RECRUITING)를 GroupState enum으로 받는다. null이면 전체 조회
     * @param pageable ?page=0&size=10와 같은 파라미터를 Pageable객체로 받는다.
     * @return
     */
    @GetMapping
    public ResponseEntity<Page<StudyGroupSummaryDto>> getStudyGroups(
            @RequestParam(required = false) GroupState state,
            Pageable pageable
    ) {
        Page<StudyGroupSummaryDto> studyGroupSummaries = studyGroupQueryService.getStudyGroupSummaries(
                state, pageable);

        return ResponseEntity.ok(studyGroupSummaries);
    }

    /**
     * 스터디 그룹 상세 정보 조회
     * @param studyGroupId 해당 스터디 그룹의 ID를 받는다.
     * @return
     */
    @GetMapping("/{studyGroupId}")
    public ResponseEntity<StudyGroupDetailDto> getStudyGroupDetail(
            @PathVariable Long studyGroupId
    ) {
        StudyGroupDetailDto studyGroupDetail = studyGroupQueryService.getStudyGroupDetail(
                studyGroupId);
        return ResponseEntity.ok(studyGroupDetail);
    }
}
