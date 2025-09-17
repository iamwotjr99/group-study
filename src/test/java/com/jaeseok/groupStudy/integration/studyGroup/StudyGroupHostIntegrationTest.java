package com.jaeseok.groupStudy.integration.studyGroup;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaeseok.groupStudy.auth.presentation.dto.LoginRequest;
import com.jaeseok.groupStudy.auth.presentation.dto.LoginResponse;
import com.jaeseok.groupStudy.integration.IntegrationTestSupport;
import com.jaeseok.groupStudy.member.domain.Member;
import com.jaeseok.groupStudy.member.domain.MemberRepository;
import com.jaeseok.groupStudy.studyGroup.domain.RecruitingPolicy;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroup;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroupCommandRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StudyGroupHostIntegrationTest extends IntegrationTestSupport {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    StudyGroupCommandRepository commandRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    EntityManager entityManager;

    String TEST_USER_EMAIL = "test@test.com";
    String TEST_USER_PASSWORD = "password1234";
    String TEST_USER_NICKNAME = "TestUser";

    String TEST_STUDY_GROUP_TITLE = "테스트 스터디 그룹 001";
    Integer TEST_STUDY_GROUP_CAPACITY = 5;
    LocalDateTime TEST_STUDY_GROUP_DEADLINE = LocalDateTime.now().plusDays(1);
    RecruitingPolicy TEST_STUDY_GROUP_POLICY = RecruitingPolicy.APPROVAL;

    Long TEST_HOST_ID;
    Long TEST_STUDY_GROUP_ID;

    String urlPrefix = "/api/study-groups/{studyGroupId}";

    @BeforeEach
    void setUp() {
        Member member = Member.createMember(TEST_USER_NICKNAME, TEST_USER_EMAIL,
                passwordEncoder.encode(TEST_USER_PASSWORD));
        Member savedMember = memberRepository.save(member);
        TEST_HOST_ID = savedMember.getId();

        StudyGroup studyGroup = StudyGroup.createWithHost(savedMember.getId(), TEST_STUDY_GROUP_TITLE,
                TEST_STUDY_GROUP_CAPACITY, TEST_STUDY_GROUP_DEADLINE, TEST_STUDY_GROUP_POLICY);
        StudyGroup savedStudyGroup = commandRepository.save(studyGroup);
        TEST_STUDY_GROUP_ID = savedStudyGroup.getId();
    }

    @Test
    @DisplayName("방장은 참여 신청자를 승인할 수 있다.")
    void givenHostIdAndStudyGroupIdAndApplicantId_whenApproveApplicant_thenReturnOK() throws Exception {
        // given
        Member member = saveMember("신청자", "applicant@test.com", "password1234");
        apply(TEST_STUDY_GROUP_ID, member.getId());

        String accessToken = login(TEST_USER_EMAIL, TEST_USER_PASSWORD);

        Long studyGroupId = TEST_STUDY_GROUP_ID;
        Long applicantId = member.getId();

        // when
        ResultActions actions = mockMvc.perform(
                post(urlPrefix + "/applicants/{applicantId}/approve", studyGroupId, applicantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
        );

        // then
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("승인 성공"));
    }

    @Test
    @DisplayName("방장이 아닌 멤버가 다른 신청자를 승인하려고 하면 403에러를 응답한다.")
    void givenNotHostAndStudyGroupIdAndApplicantId_whenApproveApplicant_thenReturnForbidden() throws Exception {
        // given
        Long studyGroupId = TEST_STUDY_GROUP_ID;

        Member member = saveMember("스터디그룹멤버", "other@test.com", "password1234");
        addParticipant(studyGroupId, member.getId());

        Member applicant = saveMember("신청자", "applicant@test.com", "password1234");
        apply(TEST_STUDY_GROUP_ID, applicant.getId());
        Long applicantId = applicant.getId();

        String accessToken = login("other@test.com", "password1234");

        // when
        ResultActions actions = mockMvc.perform(
                post(urlPrefix + "/applicants/{applicantId}/approve", studyGroupId, applicantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
        );

        // then
        actions
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("이미 승인된 멤버를 승인하려고 하면 400 에러를 응답한다.")
    void givenAlreadyApprovedApplicantIdAndStudyGroupIdAndHostId_whenApproveApplicant_thenReturnBadRequest() throws Exception {
        // given
        Long studyGroupId = TEST_STUDY_GROUP_ID;

        Member member = saveMember("스터디그룹멤버", "other@test.com", "password1234");
        addParticipant(studyGroupId, member.getId());
        Long applicantId = member.getId();

        String accessToken = login(TEST_USER_EMAIL, TEST_USER_PASSWORD);

        // when
        ResultActions actions = mockMvc.perform(
                post(urlPrefix + "/applicants/{applicantId}/approve", studyGroupId, applicantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
        );

        // then
        actions
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("존재하지 않는 유저를 승인하려고 하면 404 에러를 응답한다.")
    void givenNotExistingApplicantIdAndStudyGroupIdAndHostId_whenApproveApplicant_thenReturnNotFound() throws Exception {
        // given
        Long studyGroupId = TEST_STUDY_GROUP_ID;
        Long NotExistingApplicantId = 999L;

        String accessToken = login(TEST_USER_EMAIL, TEST_USER_PASSWORD);

        // when
        ResultActions actions = mockMvc.perform(
                post(urlPrefix + "/applicants/{applicantId}/approve", studyGroupId, NotExistingApplicantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
        );

        // then
        actions
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("스터디 최대 인원이 꽉 찼는데, 새로운 멤버를 승인하려고 하면 400 에러를 응답한다.")
    void givenStudyGroupIsFullAndApplicantIdAndStudyGroupIdAndHostId_whenApproveApplicant_thenReturnBadRequest() throws Exception {
        // given
        Long studyGroupId = TEST_STUDY_GROUP_ID;

        for (int i = 1; i <= TEST_STUDY_GROUP_CAPACITY - 1; i++) {
            Member member = saveMember("테스트 유저 " + i, "test" + i + "@test.com", "password1234");
            addParticipant(studyGroupId, member.getId());
        }

        Member applicant = saveMember("테스트 유저 6", "test6@test.com", "password1234");
        apply(studyGroupId, applicant.getId());
        Long applicantId = applicant.getId();

        String accessToken = login(TEST_USER_EMAIL, TEST_USER_PASSWORD);

        // when
        ResultActions actions = mockMvc.perform(
                post(urlPrefix + "/applicants/{applicantId}/approve", studyGroupId, applicantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
        );

        // then
        actions
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("방장은 참여 신청자를 거절할 수 있다.")
    void givenHostIdAndStudyGroupIdAndApplicantId_whenRejectApplicant_thenReturnOK() throws Exception {
        // given
        Member member = saveMember("신청자", "applicant@test.com", "password1234");
        apply(TEST_STUDY_GROUP_ID, member.getId());

        String accessToken = login(TEST_USER_EMAIL, TEST_USER_PASSWORD);
        Long studyGroupId = TEST_STUDY_GROUP_ID;
        Long applicantId = member.getId();

        // when
        ResultActions actions = mockMvc.perform(
                delete(urlPrefix + "/applicants/{applicantId}/reject", studyGroupId, applicantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
        );

        // then
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("거절 성공"));
    }

    @Test
    @DisplayName("방장이 아닌 멤버가 다른 신청자를 거절하려고 하면 403에러를 응답한다.")
    void givenNotHostAndStudyGroupIdAndApplicantId_whenRejectApplicant_thenReturnForbidden() throws Exception {
        // given
        Long studyGroupId = TEST_STUDY_GROUP_ID;

        Member member = saveMember("스터디그룹멤버", "other@test.com", "password1234");
        addParticipant(studyGroupId, member.getId());

        Member applicant = saveMember("신청자", "applicant@test.com", "password1234");
        apply(TEST_STUDY_GROUP_ID, applicant.getId());
        Long applicantId = applicant.getId();

        String accessToken = login("other@test.com", "password1234");

        // when
        ResultActions actions = mockMvc.perform(
                delete(urlPrefix + "/applicants/{applicantId}/reject", studyGroupId, applicantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
        );

        // then
        actions
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("존재하지 않는 유저를 거절하려고 하면 404 에러를 응답한다.")
    void givenNotExistingApplicantIdAndStudyGroupIdAndHostId_whenRejectApplicant_thenReturnNotFound() throws Exception {
        // given
        Long studyGroupId = TEST_STUDY_GROUP_ID;
        Long NotExistingApplicantId = 999L;

        String accessToken = login(TEST_USER_EMAIL, TEST_USER_PASSWORD);

        // when
        ResultActions actions = mockMvc.perform(
                delete(urlPrefix + "/applicants/{applicantId}/reject", studyGroupId, NotExistingApplicantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
        );

        // then
        actions
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("방장은 승인된 참여자를 강퇴할 수 있다.")
    void givenHostIdAndStudyGroupIdAndParticipantId_whenKickParticipant_thenReturnOK() throws Exception {
        // given
        Member member = saveMember("신청자", "applicant@test.com", "password1234");
        addParticipant(TEST_STUDY_GROUP_ID, member.getId());

        String accessToken = login(TEST_USER_EMAIL, TEST_USER_PASSWORD);
        Long studyGroupId = TEST_STUDY_GROUP_ID;
        Long participantId = member.getId();

        // when
        ResultActions actions = mockMvc.perform(
                delete(urlPrefix + "/participants/{participantId}/kick", studyGroupId, participantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
        );

        // then
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("강퇴 성공"));
    }

    @Test
    @DisplayName("방장이 아닌 멤버가 다른 참여자를 강퇴하려고 하면 403에러를 응답한다.")
    void givenNotHostAndStudyGroupIdAndParticipantId_whenKickParticipant_thenReturnForbidden() throws Exception {
        // given
        Long studyGroupId = TEST_STUDY_GROUP_ID;

        Member member = saveMember("스터디그룹멤버", "other@test.com", "password1234");
        addParticipant(studyGroupId, member.getId());

        Member otherParticipant = saveMember("신청자", "participant@test.com", "password1234");
        addParticipant(studyGroupId, otherParticipant.getId());
        Long participantId = otherParticipant.getId();

        String accessToken = login("other@test.com", "password1234");

        // when
        ResultActions actions = mockMvc.perform(
                delete(urlPrefix + "/participants/{participantId}/kick", studyGroupId, participantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
        );

        // then
        actions
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("참여자가 아닌 유저를 강퇴하려고 하면 400 에러를 응답한다.")
    void givenHostIdAndStudyGroupIdAndNotParticipantId_whenKickParticipant_thenReturnBadRequest() throws Exception {
        // given
        Long studyGroupId = TEST_STUDY_GROUP_ID;

        Member applicant = saveMember("신청자", "applicant@test.com", "password1234");
        apply(TEST_STUDY_GROUP_ID, applicant.getId());
        Long notParticipantId = applicant.getId();

        String accessToken = login(TEST_USER_EMAIL, TEST_USER_PASSWORD);

        // when
        ResultActions actions = mockMvc.perform(
                delete(urlPrefix + "/participants/{participantId}/kick", studyGroupId, notParticipantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
        );

        // then
        actions
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("존재하지 않는 유저를 강퇴하려고 하면 404 에러를 응답한다.")
    void givenNotExistingParticipantIdAndStudyGroupIdAndHostId_whenKickParticipant_thenReturnNotFound() throws Exception {
        // given
        Long studyGroupId = TEST_STUDY_GROUP_ID;
        Long NotExistingApplicantId = 999L;

        String accessToken = login(TEST_USER_EMAIL, TEST_USER_PASSWORD);

        // when
        ResultActions actions = mockMvc.perform(
                delete(urlPrefix + "/participants/{participantId}/kick", studyGroupId, NotExistingApplicantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
        );

        // then
        actions
                .andExpect(status().isNotFound());
    }

    private String login(String email, String password) throws Exception {
        LoginRequest request = new LoginRequest(email, password);

        MvcResult mvcResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        LoginResponse jsonResponse = objectMapper.readValue(responseBody, LoginResponse.class);

        return jsonResponse.token();
    }

    private Member saveMember(String nickname, String email, String password) {
        Member member = Member.createMember(nickname, email, passwordEncoder.encode(password));
        return memberRepository.save(member);
    }

    private void apply(Long studyGroupId, Long memberId) {
        StudyGroup studyGroup = commandRepository.findById(studyGroupId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 스터디 그룹"));
        studyGroup.apply(memberId);
        commandRepository.update(studyGroup);
        entityManager.flush();
        entityManager.clear();
    }

    private void addParticipant(Long studyGroupId, Long memberId) {
        StudyGroup studyGroup = commandRepository.findById(studyGroupId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 스터디 그룹"));
        studyGroup.apply(memberId);
        studyGroup.approveParticipant(TEST_HOST_ID, memberId);
        commandRepository.update(studyGroup);
        entityManager.flush();
        entityManager.clear();
    }
}
