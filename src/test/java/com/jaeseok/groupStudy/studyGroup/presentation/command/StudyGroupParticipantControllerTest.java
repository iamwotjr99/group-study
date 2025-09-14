package com.jaeseok.groupStudy.studyGroup.presentation.command;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jaeseok.groupStudy.auth.application.MemberDetailsService;
import com.jaeseok.groupStudy.auth.domain.UserPrincipal;
import com.jaeseok.groupStudy.auth.infrastructure.jwt.JwtTokenProvider;
import com.jaeseok.groupStudy.config.SecurityConfig;
import com.jaeseok.groupStudy.studyGroup.application.command.StudyGroupParticipantService;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.ApplyStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.CancelStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.LeaveStudyGroupCommand;
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
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(StudyGroupParticipantController.class)
@Import(SecurityConfig.class)
@DisplayName("StudyGroup Participant Controller 테스트")
class StudyGroupParticipantControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    StudyGroupParticipantService studyGroupParticipantService;

    @MockitoBean
    MemberDetailsService memberDetailsService;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    UserPrincipal testUserPrincipal;

    String urlPrefix = "/api/study-groups/{studyGroupId}";

    @BeforeEach
    void setUp() {
        setTestUserPrincipal();
    }

    @Test
    @DisplayName("스터디 그룹에 가입 신청을 할 수 있다. - 성공")
    void givenStudyGroupId_whenApply_thenReturnCreated() throws Exception {
        // given
        Long studyGroupId = 10L;

        given(memberDetailsService.loadUserByUsername("test@test.com")).willReturn(testUserPrincipal);
        willDoNothing().given(studyGroupParticipantService).applyForStudyGroup(any(
                ApplyStudyGroupCommand.class));

        // when
        ResultActions actions = mockMvc.perform(
                post(urlPrefix + "/applicants/apply", studyGroupId)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        actions
                .andExpect(status().isCreated())
                .andDo(print());

        verify(studyGroupParticipantService, times(1)).applyForStudyGroup(any(
                ApplyStudyGroupCommand.class));
    }

    @Test
    @DisplayName("스터디 그룹 가입 신청을 취소할 수 있다. - 성공")
    void cancel() throws Exception {
        // given
        Long studyGroupId = 10L;

        given(memberDetailsService.loadUserByUsername("test@test.com")).willReturn(testUserPrincipal);
        willDoNothing().given(studyGroupParticipantService).cancelApplication(any(
                CancelStudyGroupCommand.class));

        // when
        ResultActions actions = mockMvc.perform(
                delete(urlPrefix + "/applicants/cancel", studyGroupId)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        actions
                .andExpect(status().isOk())
                .andDo(print());

        verify(studyGroupParticipantService, times(1)).cancelApplication(any(
                CancelStudyGroupCommand.class));
    }

    @Test
    @DisplayName("가입된 스터디 그룹에서 퇴장할 수 있다. - 성공")
    void leave() throws Exception {
        // given
        Long studyGroupId = 10L;

        given(memberDetailsService.loadUserByUsername("test@test.com")).willReturn(testUserPrincipal);
        willDoNothing().given(studyGroupParticipantService).leaveStudyGroup(any(
                LeaveStudyGroupCommand.class));

        // when
        ResultActions actions = mockMvc.perform(
                delete(urlPrefix + "/participants/leave", studyGroupId)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        actions
                .andExpect(status().isOk())
                .andDo(print());

        verify(studyGroupParticipantService, times(1)).leaveStudyGroup(any(
                LeaveStudyGroupCommand.class));
    }

    private void setTestUserPrincipal() {
        testUserPrincipal = new UserPrincipal(1L, "test@test.com", "password1234");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUserPrincipal, null, testUserPrincipal.getAuthorities())
        );
    }
}