package com.jaeseok.groupStudy.integration.studyGroup;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaeseok.groupStudy.auth.presentation.dto.LoginRequest;
import com.jaeseok.groupStudy.auth.presentation.dto.LoginResponse;
import com.jaeseok.groupStudy.integration.IntegrationTestSupport;
import com.jaeseok.groupStudy.member.domain.Member;
import com.jaeseok.groupStudy.member.domain.MemberRepository;
import com.jaeseok.groupStudy.studyGroup.domain.RecruitingPolicy;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroup;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroupCommandRepository;
import com.jaeseok.groupStudy.studyGroup.presentation.command.dto.CreateStudyGroupRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
class StudyGroupLifecycleIntegrationTest extends IntegrationTestSupport {

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
    @DisplayName("로그인 한 사용자는 유효한 정보로 스터디 그룹 생성을 요청하면 스터디 그룹을 생성할 수 있다.")
    void givenLoginUserAndValidRequest_whenCreateStudyGroup_thenReturnCreatedStatus() throws Exception {
        // given
        String accessToken = login(TEST_USER_EMAIL, TEST_USER_PASSWORD);
        CreateStudyGroupRequest request = new CreateStudyGroupRequest(
                "테스트 스터디 그룹 002", 3, LocalDateTime.now().plusDays(2), RecruitingPolicy.APPROVAL
        );

        // when
        ResultActions actions = mockMvc.perform(post("/api/study-groups")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .content(objectMapper.writeValueAsString(request))
        );

        // then
        actions
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @ParameterizedTest
    @DisplayName("스터디를 생성할 때 유효하지 않은 정보로 요쳥하면 400에러를 응답한다.")
    @MethodSource("invalidCreateStudyGroupRequests")
    void givenInvalidRequest_whenCreateStudyGroup_thenReturnBadRequest(CreateStudyGroupRequest request) throws Exception {
        // given
        String accessToken = login(TEST_USER_EMAIL, TEST_USER_PASSWORD);

        // when
        ResultActions actions = mockMvc.perform(post("/api/study-groups")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .content(objectMapper.writeValueAsString(request))
        );

        // then
        actions
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그인 한 방장은 스터디 그룹을 시작할 수 있다.")
    void givenLoginHostAndStudyGroupId_whenStartStudyGroup_thenReturnOK() throws Exception {
        // given
        String accessToken = login(TEST_USER_EMAIL, TEST_USER_PASSWORD);
        Long studyGroupId = TEST_STUDY_GROUP_ID;


        // when
        ResultActions actions = mockMvc.perform(
                post("/api/study-groups/{studyGroupId}/start", studyGroupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
        );

        // then
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("스터디를 시작했습니다."));
    }

    @Test
    @DisplayName("방장이 아닌 사람이 스터디 그룹을 시작하려 할 때 403 에러를 응답한다.")
    void givenNotHostAndStudyGroupId_whenStartStudyGroup_thenReturnForbidden() throws Exception {
        // given
        Member member = Member.createMember("another", "another@test.com", passwordEncoder.encode("password1234"));
        Member savedMember = memberRepository.save(member);
        String accessToken = login("another@test.com", "password1234");

        // 새로운 유저를 승인된 참여자로 추가
        addParticipant(TEST_STUDY_GROUP_ID, savedMember.getId());

        // when
        ResultActions actions = mockMvc.perform(
                post("/api/study-groups/{studyGroupId}/start", TEST_STUDY_GROUP_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
        );

        // then
        actions
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("존재하지 않는 스터디를 시작하려 할 때 404 에러를 응답한다.")
    void givenNotExistingStudyGroupId_whenStartStudyGroup_thenReturnNotFound() throws Exception {
        // given
        String accessToken = login(TEST_USER_EMAIL, TEST_USER_PASSWORD);
        Long notExistingStudyGroupId = 999L;

        // when
        ResultActions actions = mockMvc.perform(
                post("/api/study-groups/{studyGroupId}/start", notExistingStudyGroupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
        );

        // then
        actions
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("모집중이 아닌 스터디를 시작하려 할 때 400 에러를 응답한다.")
    void givenNotRecruitingStudyGroupId_whenStartStudyGroup_thenReturnBadRequest() throws Exception {
        // given
        String accessToken = login(TEST_USER_EMAIL, TEST_USER_PASSWORD);

        StudyGroup studyGroup = commandRepository.findById(TEST_STUDY_GROUP_ID)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 스터디그룹"));
        studyGroup.start(TEST_HOST_ID);
        commandRepository.update(studyGroup);
        entityManager.flush();
        entityManager.clear();

        // when
        ResultActions actions = mockMvc.perform(
                post("/api/study-groups/{studyGroupId}/start", TEST_STUDY_GROUP_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
        );

        // then
        actions
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그인 한 방장은 진행중인 스터디 그룹을 종료할 수 있다.")
    void givenLoginHostAndStartStudyGroup_whenCloseStudyGroup_thenReturnOK() throws Exception {
        // given
        String accessToken = login(TEST_USER_EMAIL, TEST_USER_PASSWORD);
        Long studyGroupId = TEST_STUDY_GROUP_ID;

        // 스터디 그룹 진행중으로 변경
        startStudyGroup(TEST_STUDY_GROUP_ID, TEST_HOST_ID);

        // when
        ResultActions actions = mockMvc.perform(
                post("/api/study-groups/{studyGroupId}/close", studyGroupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
        );

        // then
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("스터디를 종료했습니다."));
    }

    @Test
    @DisplayName("방장이 아닌 사람이 스터디 그룹을 종료하려 할 때 403 에러를 응답한다.")
    void givenNotHostAndStudyGroupId_whenCloseStudyGroup_thenReturnForbidden() throws Exception {
        // given
        Member member = Member.createMember("another", "another@test.com", passwordEncoder.encode("password1234"));
        Member savedMember = memberRepository.save(member);
        String accessToken = login("another@test.com", "password1234");

        Long studyGroupId = TEST_STUDY_GROUP_ID;

        // 새로운 유저를 승인된 참여자로 추가
        addParticipant(TEST_STUDY_GROUP_ID, savedMember.getId());

        // 스터디 그룹을 진행중으로 변경
        startStudyGroup(TEST_STUDY_GROUP_ID, TEST_HOST_ID);

        // when
        ResultActions actions = mockMvc.perform(
                post("/api/study-groups/{studyGroupId}/close", studyGroupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
        );

        // then
        actions
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("존재하지 않는 스터디를 종료하려 할 때 404 에러를 응답한다.")
    void givenNotExistingStudyGroupId_whenCloseStudyGroup_thenReturnNotFound() throws Exception {
        // given
        String accessToken = login(TEST_USER_EMAIL, TEST_USER_PASSWORD);
        Long notExistingStudyGroupId = 999L;

        // when
        ResultActions actions = mockMvc.perform(
                post("/api/study-groups/{studyGroupId}/close", notExistingStudyGroupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
        );

        // then
        actions
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("진행중이 아닌 스터디를 종료하려 할 때 400 에러를 응답한다.")
    void givenNotStartStudyGroupId_whenCloseStudyGroup_thenReturnBadRequest() throws Exception {}

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
        studyGroup.approveParticipant(TEST_HOST_ID, memberId);
        commandRepository.update(studyGroup);
        entityManager.flush();
        entityManager.clear();
    }

    private void startStudyGroup(Long studyGroupId, Long hostId) {
        StudyGroup studyGroup = commandRepository.findById(studyGroupId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 스터디그룹"));
        studyGroup.start(hostId);
        commandRepository.update(studyGroup);
        entityManager.flush();
        entityManager.clear();
    }

    private static Stream<Arguments> invalidCreateStudyGroupRequests() {
        return Stream.of(
                Arguments.of(new CreateStudyGroupRequest("정상 스터디 그룹 제목", 1, LocalDateTime.now().plusDays(1), RecruitingPolicy.APPROVAL)),
                Arguments.of(new CreateStudyGroupRequest("정상 스터디 그룹 제목", 3, LocalDateTime.now().minusDays(1), RecruitingPolicy.APPROVAL)),
                Arguments.of(new CreateStudyGroupRequest("", 3, LocalDateTime.now().plusDays(1), RecruitingPolicy.APPROVAL)),
                Arguments.of(new CreateStudyGroupRequest(null, 3, LocalDateTime.now().plusDays(1), RecruitingPolicy.APPROVAL)),
                Arguments.of(new CreateStudyGroupRequest("엄청 매우 매우 정말 정말 긴 스터디 그룹 제목", 3, LocalDateTime.now().plusDays(1), RecruitingPolicy.APPROVAL))
        );
    }
}
