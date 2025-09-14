package com.jaeseok.groupStudy.studyGroup.presentation.command;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jaeseok.groupStudy.auth.application.MemberDetailsService;
import com.jaeseok.groupStudy.auth.domain.UserPrincipal;
import com.jaeseok.groupStudy.auth.infrastructure.jwt.JwtTokenProvider;
import com.jaeseok.groupStudy.config.SecurityConfig;
import com.jaeseok.groupStudy.studyGroup.application.command.StudyGroupHostService;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.ApproveStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.KickStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.RejectStudyGroupCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(StudyGroupHostController.class)
@Import(SecurityConfig.class)
@DisplayName("StudyGroup Host Controller 테스트")
class StudyGroupHostControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    MemberDetailsService memberDetailsService;

    @MockitoBean
    StudyGroupHostService studyGroupHostService;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    UserPrincipal testUserPrincipal;

    String urlPrefix = "/api/study-groups/{studyGroupId}";

    @BeforeEach
    void setUp() {
        // User 인증 객체를 Security Context에 주입
        setTestUserPrincipal();
    }

    @Test
    @DisplayName("스터디 참여 신청을 승인한다. - 성공")
    void givenApplicantIdAndStudyGroupId_whenApproveApplication_thenReturnOK() throws Exception {
        // given
        Long studyGroupId = 10L;
        Long applicantId = 1L;

        given(memberDetailsService.loadUserByUsername("test@test.com")).willReturn(testUserPrincipal);
        willDoNothing().given(studyGroupHostService).approveApplication(any(ApproveStudyGroupCommand.class));

        // when
        ResultActions actions = mockMvc.perform(
                post(urlPrefix + "/applicants/{applicantId}/approve",
                        studyGroupId, applicantId)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        actions
                .andExpect(status().isOk())
                .andDo(print());

        verify(studyGroupHostService, times(1)).approveApplication(any(ApproveStudyGroupCommand.class));
    }

    @Test
    @DisplayName("스터디 참여 신청을 거절한다. - 성공")
    void givenApplicantIdAndStudyGroupId_whenRejectApplication_thenReturnOK() throws Exception {
        // given
        Long studyGroupId = 10L;
        Long applicantId = 1L;

        given(memberDetailsService.loadUserByUsername("test@test.com")).willReturn(testUserPrincipal);
        willDoNothing().given(studyGroupHostService).rejectApplication(any(RejectStudyGroupCommand.class));

        // when
        ResultActions actions = mockMvc.perform(
                delete(urlPrefix + "/applicants/{applicantId}/reject", studyGroupId, applicantId)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        actions
                .andExpect(status().isOk())
                .andDo(print());

        verify(studyGroupHostService, times(1)).rejectApplication(any(RejectStudyGroupCommand.class));
    }

    @Test
    @DisplayName("스터디 참여자를 강퇴한다. - 성공")
    void givenParticipantIdAndStudyGroupId_whenKickParticipant_thenReturnOK() throws Exception {
        // given
        Long studyGroupId = 10L;
        Long participantId = 1L;

        given(memberDetailsService.loadUserByUsername("test@test.com")).willReturn(testUserPrincipal);
        willDoNothing().given(studyGroupHostService).kickParticipation(any(KickStudyGroupCommand.class));

        // when
        ResultActions actions = mockMvc.perform(
                delete(urlPrefix + "/participants/{participantId}/kick", studyGroupId,
                        participantId)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        actions
                .andExpect(status().isOk())
                .andDo(print());
        verify(studyGroupHostService, times(1)).kickParticipation(any(KickStudyGroupCommand.class));
    }

    private void setTestUserPrincipal() {
        testUserPrincipal = new UserPrincipal(1L, "test@test.com", "password1234");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUserPrincipal, null, testUserPrincipal.getAuthorities())
        );
    }
}