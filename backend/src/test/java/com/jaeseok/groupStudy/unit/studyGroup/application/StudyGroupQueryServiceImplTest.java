package com.jaeseok.groupStudy.unit.studyGroup.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.jaeseok.groupStudy.studyGroup.application.query.StudyGroupQueryServiceImpl;
import com.jaeseok.groupStudy.studyGroup.domain.GroupState;
import com.jaeseok.groupStudy.studyGroup.domain.RecruitingPolicy;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.query.StudyGroupQueryRepository;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.query.dto.StudyGroupDetailDto;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.query.dto.StudyGroupSummaryDto;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class StudyGroupQueryServiceImplTest {

    @InjectMocks
    StudyGroupQueryServiceImpl studyGroupQueryService;

    @Mock
    StudyGroupQueryRepository studyGroupQueryRepository;

    @Test
    @DisplayName("스터디 그룹 상세 조회를 성공하면 스터디 그룹 상세 정보를 리턴한다.")
    void givenStudyGroupId_whenGetStudyGroupDetail_thenReturnStudyGroupDetailDto() {
        // given
        Long studyGroupId = 1L;
        Long hostId = 2L;
        StudyGroupDetailDto mockDto = new StudyGroupDetailDto(studyGroupId, hostId, "테스트 스터디",
                3, 5, LocalDateTime.now().plusDays(1), RecruitingPolicy.APPROVAL,
                GroupState.RECRUITING);

        given(studyGroupQueryRepository.findStudyGroupDetailById(studyGroupId))
                .willReturn(Optional.of(mockDto));

        // when
        StudyGroupDetailDto resultDto = studyGroupQueryService.getStudyGroupDetail(
                studyGroupId);

        // then
        assertThat(resultDto).isEqualTo(mockDto);
        verify(studyGroupQueryRepository, times(1)).findStudyGroupDetailById(studyGroupId);
        verifyNoMoreInteractions(studyGroupQueryRepository);
    }

    @Test
    @DisplayName("스터디 그룹 상세 조회를 실패하면 예외를 던진다.(존재하지 않는 스터디 그룹)")
    void givenNotExistStudyGroupId_whenGetStudyGroupDetail_thenThrowException() {
        // given
        Long notExistStudyGroupId = 404L;

        given(studyGroupQueryRepository.findStudyGroupDetailById(notExistStudyGroupId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> studyGroupQueryService.getStudyGroupDetail(notExistStudyGroupId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("존재하지 않는 스터디 그룹입니다.");

        verify(studyGroupQueryRepository, times(1)).findStudyGroupDetailById(notExistStudyGroupId);
        verifyNoMoreInteractions(studyGroupQueryRepository);

    }

    @Test
    @DisplayName("스터디 그룹 목록 조회를 하면 요청된 페이징 만큼 페이징해서 목록을 리턴한다.")
    void givenStateAndPageable_whenGetStudyGroupSummaries_thenReturnPageOfStudyGroupSummaries() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        List<StudyGroupSummaryDto> mockDtos = createMockStudyGroupSummaries(25);
        for (int i = 1; i <= 25; i++) {
            StudyGroupSummaryDto dto = new StudyGroupSummaryDto((long) i, (long) i + 1, "테스트 " + i, 3,
                    5, LocalDateTime.now().plusDays(i), RecruitingPolicy.APPROVAL,
                    GroupState.RECRUITING);
            mockDtos.add(dto);
        }
        PageImpl<StudyGroupSummaryDto> mockPage = new PageImpl<>(mockDtos, pageable, 25);

        given(studyGroupQueryRepository.findStudyGroupSummaries(any(), any(Pageable.class)))
                .willReturn(mockPage);

        // when
        Page<StudyGroupSummaryDto> studyGroupSummaries = studyGroupQueryService.getStudyGroupSummaries(
                GroupState.RECRUITING, pageable);

        // then
        assertThat(studyGroupSummaries.getTotalElements()).isEqualTo(25);
        assertThat(studyGroupSummaries.getTotalPages()).isEqualTo(3);
        assertThat(studyGroupSummaries.getContent()).isEqualTo(mockDtos);

        verify(studyGroupQueryRepository, times(1)).findStudyGroupSummaries(any(), any(Pageable.class));
        verifyNoMoreInteractions(studyGroupQueryRepository);
    }

    private List<StudyGroupSummaryDto> createMockStudyGroupSummaries(int count) {
        List<StudyGroupSummaryDto> dtos = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            StudyGroupSummaryDto dto = new StudyGroupSummaryDto((long) i, (long) i + 1,"테스트 " + i, 3,
                    5, LocalDateTime.now().plusDays(i), RecruitingPolicy.APPROVAL,
                    GroupState.RECRUITING);
            dtos.add(dto);
        }

        return dtos;
    }
}