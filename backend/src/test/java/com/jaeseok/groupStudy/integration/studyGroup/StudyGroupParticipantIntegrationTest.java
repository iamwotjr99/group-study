package com.jaeseok.groupStudy.integration.studyGroup;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
public class StudyGroupParticipantIntegrationTest extends IntegrationTestSupport {

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

    String TEST_HOST_EMAIL = "host@test.com";
    String TEST_HOST_PASSWORD = "password5678";
    String TEST_HOST_NICKNAME = "방장";

    String TEST_STUDY_GROUP_TITLE = "테스트 스터디 그룹 001";
    Integer TEST_STUDY_GROUP_CAPACITY = 5;
    LocalDateTime TEST_STUDY_GROUP_DEADLINE = LocalDateTime.now().plusDays(1);
    RecruitingPolicy TEST_STUDY_GROUP_POLICY = RecruitingPolicy.APPROVAL;

    Long TEST_APPLICANT_ID;
    Long TEST_STUDY_GROUP_ID;

    String urlPrefix = "/api/study-groups/{studyGroupId}";

    @BeforeEach
    void setUp() {
        Member host = Member.createMember(TEST_HOST_NICKNAME, TEST_HOST_EMAIL,
                passwordEncoder.encode(TEST_HOST_PASSWORD));
        Member savedHost = memberRepository.save(host);

        StudyGroup studyGroup = StudyGroup.createWithHost(savedHost.getId(), TEST_STUDY_GROUP_TITLE,
                TEST_STUDY_GROUP_CAPACITY, TEST_STUDY_GROUP_DEADLINE, TEST_STUDY_GROUP_POLICY);
        StudyGroup savedStudyGroup = commandRepository.save(studyGroup);
        TEST_STUDY_GROUP_ID = savedStudyGroup.getId();

        Member applicant = Member.createMember(TEST_USER_NICKNAME, TEST_USER_EMAIL,
                passwordEncoder.encode(TEST_USER_PASSWORD));
        Member savedParticipant = memberRepository.save(applicant);
        TEST_APPLICANT_ID = savedParticipant.getId();
    }

    @Test
    @DisplayName("유저는 스터디 그룹에 참여 신청을 할 수 있다.")
    void givenLoginUserAndStudyGroupId_whenApplyToStudyGroup_thenReturnCreated() throws Exception {
        // given
        String accessToken = login(TEST_USER_EMAIL, TEST_USER_PASSWORD);

        Long studyGroupId = TEST_STUDY_GROUP_ID;

        // when
        ResultActions actions = mockMvc.perform(post(urlPrefix + "/applicants/apply", studyGroupId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
        );

        // then
        actions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("가입 신청에 성공했습니다."));
    }

    @Test
    @DisplayName("존재하지 않는 스터디 그룹에 참여 신청을 하면 404 에러를 응답한다.")
    void givenLoginUserAndNotExistStudyGroupId_whenApplyToStudyGroup_thenReturnNotFound() throws Exception {
        // given
        String accessToken = login(TEST_USER_EMAIL, TEST_USER_PASSWORD);

        Long notExistStudyGroupId = 999L;

        // when
        ResultActions actions = mockMvc.perform(post(urlPrefix + "/applicants/apply", notExistStudyGroupId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
        );

        // then
        actions
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("이미 신청한 스터디 그룹에 신청을 하면 400 에러를 응답한다.")
    void givenLoginUserAndAlreadyAppliedStudyGroupId_whenApplyToStudyGroup_thenReturnBadRequest() throws Exception {
        // given
        Long studyGroupId = TEST_STUDY_GROUP_ID;
        apply(studyGroupId, TEST_APPLICANT_ID);

        String accessToken = login(TEST_USER_EMAIL, TEST_USER_PASSWORD);

        // when
        ResultActions actions = mockMvc.perform(post(urlPrefix + "/applicants/apply", studyGroupId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
        );

        // then
        actions
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("유저는 스터디 그룹에 참여 신청을 하고 취소 할 수 있다.")
    void givenLoginUserAndStudyGroupId_whenCancelApplyToStudyGroup_thenReturnOK() throws Exception {
        // given
        apply(TEST_STUDY_GROUP_ID, TEST_APPLICANT_ID);

        String accessToken = login(TEST_USER_EMAIL, TEST_USER_PASSWORD);

        Long studyGroupId = TEST_STUDY_GROUP_ID;

        // when
        ResultActions actions = mockMvc.perform(delete(urlPrefix + "/applicants/cancel", studyGroupId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
        );

        // then
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("가입 신청 취소에 성공했습니다."));
    }

    @Test
    @DisplayName("존재하지 않는 스터디 그룹에 신청 취소를 하면 404 에러를 응답한다.")
    void givenLoginUserAndNotExistStudyGroupId_whenCancelApplyToStudyGroup_thenReturnNotFound() throws Exception {
        // given
        String accessToken = login(TEST_USER_EMAIL, TEST_USER_PASSWORD);

        Long notExistStudyGroupId = 999L;

        // when
        ResultActions actions = mockMvc.perform(delete(urlPrefix + "/applicants/cancel", notExistStudyGroupId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
        );

        // then
        actions
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("승인 대기중이 아닌 스터디 그룹에 신청 취소를 하면 400 에러를 응답한다.")
    void givenLoginUserAndAlreadyApprovedStudyGroupId_whenCancelApplyToStudyGroup_thenReturnBadRequest() throws Exception {
        // given
        Long studyGroupId = TEST_STUDY_GROUP_ID;
        addParticipant(studyGroupId, TEST_APPLICANT_ID);

        String accessToken = login(TEST_USER_EMAIL, TEST_USER_PASSWORD);

        // when
        ResultActions actions = mockMvc.perform(delete(urlPrefix + "/applicants/cancel", studyGroupId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
        );

        // then
        actions
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("스터디 그룹 참여자는 그룹을 퇴장할 수 있다.")
    void givenLoginUserAndStudyGroupId_whenLeaveFromStudyGroup_thenReturnOK() throws Exception {
        // given
        addParticipant(TEST_STUDY_GROUP_ID, TEST_APPLICANT_ID);

        String accessToken = login(TEST_USER_EMAIL, TEST_USER_PASSWORD);

        Long studyGroupId = TEST_STUDY_GROUP_ID;

        // when
        ResultActions actions = mockMvc.perform(
                delete(urlPrefix + "/participants/leave", studyGroupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
        );

        // then
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("스터디 그룹을 퇴장했습니다."));
    }

    @Test
    @DisplayName("존재하지 않는 스터디 그룹에 퇴장 요청을 하면 404 에러를 응답한다.")
    void givenLoginUserAndNotExistStudyGroupId_whenLeaveFromStudyGroup_thenReturnNotFound() throws Exception {
        // given
        String accessToken = login(TEST_USER_EMAIL, TEST_USER_PASSWORD);

        Long notExistStudyGroupId = 999L;

        // when
        ResultActions actions = mockMvc.perform(
                delete(urlPrefix + "/participants/leave", notExistStudyGroupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
        );

        // then
        actions
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("참여중이 아닌 스터디 그룹에 퇴장 요청을 하면 400 에러를 응답한다.")
    void givenLoginUserAndAlreadyApprovedStudyGroupId_whenLeaveFromStudyGroup_thenReturnBadRequest() throws Exception {
        // given
        Long studyGroupId = TEST_STUDY_GROUP_ID;
        apply(studyGroupId, TEST_APPLICANT_ID);

        String accessToken = login(TEST_USER_EMAIL, TEST_USER_PASSWORD);

        // when
        ResultActions actions = mockMvc.perform(
                delete(urlPrefix + "/participants/leave", studyGroupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
        );

        // then
        actions
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("해당 스터디 그룹의 방장이 스터디 그룹에 퇴장 요청을 하면 400 에러를 응답한다.")
    void givenHostAndStudyGroupId_whenLeaveFromStudyGroup_thenReturnBadRequest() throws Exception {
        // given
        String accessToken = login(TEST_HOST_EMAIL, TEST_HOST_PASSWORD);

        Long studyGroupId = TEST_STUDY_GROUP_ID;

        // when
        ResultActions actions = mockMvc.perform(
                delete(urlPrefix + "/participants/leave", studyGroupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
        );

        // then
        actions
                .andExpect(status().isBadRequest());
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

    private void addParticipant(Long studyGroupId, Long memberId) {
        StudyGroup studyGroup = commandRepository.findById(studyGroupId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 스터디 그룹"));
        studyGroup.apply(memberId);
        studyGroup.approveParticipant(studyGroup.getHost().userId(), memberId);
        commandRepository.update(studyGroup);
        entityManager.flush();
        entityManager.clear();
    }

    private void apply(Long studyGroupId, Long memberId) {
        StudyGroup studyGroup = commandRepository.findById(studyGroupId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 스터디 그룹"));
        studyGroup.apply(memberId);
        commandRepository.update(studyGroup);
        entityManager.flush();
        entityManager.clear();
    }

}
