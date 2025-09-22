package com.jaeseok.groupStudy.unit.studyGroup.presentation.query;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jaeseok.groupStudy.auth.application.MemberDetailsService;
import com.jaeseok.groupStudy.auth.domain.UserPrincipal;
import com.jaeseok.groupStudy.auth.infrastructure.jwt.JwtTokenProvider;
import com.jaeseok.groupStudy.config.SecurityConfig;
import com.jaeseok.groupStudy.studyGroup.application.query.StudyGroupQueryService;
import com.jaeseok.groupStudy.studyGroup.domain.GroupState;
import com.jaeseok.groupStudy.studyGroup.domain.RecruitingPolicy;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.query.dto.StudyGroupDetailDto;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.query.dto.StudyGroupSummaryDto;
import com.jaeseok.groupStudy.studyGroup.presentation.query.StudyGroupQueryController;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(StudyGroupQueryController.class)
@Import(SecurityConfig.class)
@DisplayName("StudyGroup Query Controller 테스트")
class StudyGroupQueryControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    StudyGroupQueryService studyGroupQueryService;

    @MockitoBean
    MemberDetailsService memberDetailsService;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    UserPrincipal testUserPrincipal;

    String urlPrefix = "/api/study-groups";

    @BeforeEach
    void setUp() {
        setTestUserPrincipal();
    }

    @Test
    @DisplayName("스터디 그룹 목록을 페이징하여 조회한다. - 성공")
    void givenStateAndPageable_whenGetStudyGroups_thenReturnStudyGroupSummaries() throws Exception {
        // given
        List<StudyGroupSummaryDto> mockStudyGroupSummaries = createMockStudyGroupSummaries(25);
        PageRequest pageable = PageRequest.of(0, 10);
        Page<StudyGroupSummaryDto> mockPage = new PageImpl<>(
                mockStudyGroupSummaries, pageable, mockStudyGroupSummaries.size());

        given(studyGroupQueryService.getStudyGroupSummaries(any(), any(Pageable.class))).willReturn(mockPage);

        // when
        ResultActions actions = mockMvc.perform(get("/api/study-groups")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(mockStudyGroupSummaries.size()))
                .andExpect(jsonPath("$.content[0].title").value("테스트 1"))
                .andDo(print());

        verify(studyGroupQueryService, times(1)).getStudyGroupSummaries(any(), any(Pageable.class));
    }

    @Test
    @DisplayName("스터디 그룹 상세 정보를 조회할 수 있다. - 성공")
    void getStudyGroupDetail() throws Exception {
        // given
        Long studyGroupId = 10L;
        Long hostId = 1L;
        StudyGroupDetailDto mockDto = new StudyGroupDetailDto(studyGroupId, hostId,
                "테스트 그룹 001", 3, 5, LocalDateTime.now().plusDays(3), RecruitingPolicy.APPROVAL,
                GroupState.RECRUITING);

        given(studyGroupQueryService.getStudyGroupDetail(eq(studyGroupId))).willReturn(mockDto);

        // when
        ResultActions actions = mockMvc.perform(get(urlPrefix + "/{studyGroupId}", studyGroupId)
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studyGroupId").value(studyGroupId))
                .andExpect(jsonPath("$.title").value("테스트 그룹 001"))
                .andExpect(jsonPath("$.curMemberCount").value("3"))
                .andExpect(jsonPath("$.capacity").value("5"))
                .andDo(print());

        verify(studyGroupQueryService, times(1)).getStudyGroupDetail(eq(studyGroupId));
    }

    private void setTestUserPrincipal() {
        testUserPrincipal = new UserPrincipal(1L, "test@test.com", "password1234");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUserPrincipal, null, testUserPrincipal.getAuthorities())
        );
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