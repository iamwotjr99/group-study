package com.jaeseok.groupStudy.unit.studyGroup.presentation.command;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaeseok.groupStudy.auth.application.MemberDetailsService;
import com.jaeseok.groupStudy.auth.domain.UserPrincipal;
import com.jaeseok.groupStudy.auth.infrastructure.jwt.JwtTokenProvider;
import com.jaeseok.groupStudy.config.SecurityConfig;
import com.jaeseok.groupStudy.studyGroup.application.command.StudyGroupHostService;
import com.jaeseok.groupStudy.studyGroup.application.command.StudyGroupLifecycleService;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.CloseStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.CreateStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.CreateStudyGroupInfo;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.StartStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.domain.RecruitingPolicy;
import com.jaeseok.groupStudy.studyGroup.presentation.command.StudyGroupLifecycleController;
import com.jaeseok.groupStudy.studyGroup.presentation.command.dto.CreateStudyGroupRequest;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(StudyGroupLifecycleController.class)
@Import(SecurityConfig.class)
@DisplayName("StudyGroup Lifecycle Controller 테스트")
class StudyGroupLifecycleControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    MemberDetailsService memberDetailsService;

    @MockitoBean
    StudyGroupLifecycleService studyGroupLifecycleService;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    UserPrincipal testUserPrincipal;

    String urlPrefix = "/api/study-groups";

    @BeforeEach
    void setUp() {
        setTestUserPrincipal();
    }

    @Test
    @DisplayName("스터디 그룹을 생성한다. - 성공")
    void givenCreateStudyGroupRequest_whenCreateStudyGroup_thenReturnCreated()
            throws Exception {
        // given
        CreateStudyGroupRequest request = new CreateStudyGroupRequest(1L,
                "스터디 그룹 1", 5, LocalDateTime.now().plusDays(5), RecruitingPolicy.APPROVAL);

        Long willCreatedStudyGroupId = 10L;
        CreateStudyGroupInfo createStudyGroupInfo = new CreateStudyGroupInfo(
                willCreatedStudyGroupId);
        given(memberDetailsService.loadUserByUsername("test@test.com")).willReturn(testUserPrincipal);
        given(studyGroupLifecycleService.createStudyGroup(any(CreateStudyGroupCommand.class))).willReturn(createStudyGroupInfo);

        // when
        ResultActions actions = mockMvc.perform(
                post("/api/study-groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );

        // then
        actions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studyGroupId").value(willCreatedStudyGroupId))
                .andDo(print());

        verify(studyGroupLifecycleService, times(1)).createStudyGroup(any(CreateStudyGroupCommand.class));
    }

    @Test
    @DisplayName("스터디 그룹을 시작한다. - 성공")
    void givenStudyGroupId_whenStartStudyGroup_thenReturnOK() throws Exception {
        // given
        Long studyGroupId = 10L;

        given(memberDetailsService.loadUserByUsername("test@test.com")).willReturn(testUserPrincipal);
        willDoNothing().given(studyGroupLifecycleService).startStudyGroup(any(StartStudyGroupCommand.class));

        // when
        ResultActions actions = mockMvc.perform(
                post(urlPrefix + "/{studyGroupId}/start", studyGroupId)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        actions
                .andExpect(status().isOk())
                .andDo(print());

        verify(studyGroupLifecycleService, times(1)).startStudyGroup(any(StartStudyGroupCommand.class));
    }

    @Test
    @DisplayName("스터디 그룹을 종료한다. - 성공")
    void closeStudyGroup() throws Exception {
        // given
        Long studyGroupId = 10L;

        given(memberDetailsService.loadUserByUsername("test@test.com")).willReturn(testUserPrincipal);
        willDoNothing().given(studyGroupLifecycleService).closeStudyGroup(any(CloseStudyGroupCommand.class));

        // when
        ResultActions actions = mockMvc.perform(
                post(urlPrefix + "/{studyGroupId}/close", studyGroupId)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        actions
                .andExpect(status().isOk())
                .andDo(print());

        verify(studyGroupLifecycleService, times(1)).closeStudyGroup(any(CloseStudyGroupCommand.class));
    }

    private void setTestUserPrincipal() {
        testUserPrincipal = new UserPrincipal(1L, "test@test.com", "password1234");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUserPrincipal, null, testUserPrincipal.getAuthorities())
        );
    }
}