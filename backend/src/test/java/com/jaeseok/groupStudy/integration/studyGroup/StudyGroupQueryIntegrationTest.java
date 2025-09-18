package com.jaeseok.groupStudy.integration.studyGroup;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaeseok.groupStudy.auth.presentation.dto.LoginRequest;
import com.jaeseok.groupStudy.auth.presentation.dto.LoginResponse;
import com.jaeseok.groupStudy.integration.IntegrationTestSupport;
import com.jaeseok.groupStudy.member.domain.Member;
import com.jaeseok.groupStudy.member.domain.MemberRepository;
import com.jaeseok.groupStudy.studyGroup.domain.GroupState;
import com.jaeseok.groupStudy.studyGroup.domain.RecruitingPolicy;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroup;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroupCommandRepository;
import jakarta.persistence.EntityManager;
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
public class StudyGroupQueryIntegrationTest extends IntegrationTestSupport {

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

    Long TEST_STUDY_GROUP_ID;
    @BeforeEach
    void setUp() {
        Member host = Member.createMember(TEST_USER_NICKNAME, TEST_USER_EMAIL,
                passwordEncoder.encode(TEST_USER_PASSWORD));
        Member savedHost = memberRepository.save(host);

        String title = "모집중인 스터디 ";
        Integer capacity = 3;
        LocalDateTime deadline = LocalDateTime.now().plusDays(5);
        RecruitingPolicy policy = RecruitingPolicy.APPROVAL;

        StudyGroup studyGroup = StudyGroup.createWithHost(savedHost.getId(), "상세정보 스터디",
                5, deadline, RecruitingPolicy.AUTO);
        StudyGroup savedStudyGroup = commandRepository.save(studyGroup);
        TEST_STUDY_GROUP_ID = savedStudyGroup.getId();

        // Recruiting 상태 스터디 그룹
        for (int i = 1; i <= 8; i++) {
            StudyGroup studyGroup_ = StudyGroup.createWithHost(savedHost.getId(), title, capacity,
                    deadline, policy);
            commandRepository.save(studyGroup_);
        }

        // Start 상태 스터디 그룹
        for (int i = 1; i <= 8; i++) {
            StudyGroup studyGroup_ = StudyGroup.createWithHost(savedHost.getId(), title, capacity,
                    deadline, policy);
            studyGroup_.start(savedHost.getId());
            commandRepository.save(studyGroup_);
        }

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("스터디 전체 목록을 페이징 조회할 수 있다.")
    void givenStateEmptyAndPageable_whenGetStudyGroups_thenReturnStudyGroups() throws Exception {
        // given
        String accessToken = login(TEST_USER_EMAIL, TEST_USER_PASSWORD);
        int page = 2;
        int size = 5;

        // when
        ResultActions actions = mockMvc.perform(get("/api/study-groups")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size))
        );

        // then
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(17))
                .andExpect(jsonPath("$.totalPages").value(4))
                .andExpect(jsonPath("$.size").value(size))
                .andExpect(jsonPath("$.number").value(page))
                .andExpect(jsonPath("$.content.length()").value(5));
    }

    @Test
    @DisplayName("모집중 상태로 필터링하여 스터디 목록을 조회할 수 있다.")
    void givenStateRecruitingAndPageable_whenGetStudyGroups_thenReturnStudyGroups() throws Exception {
        // given
        String accessToken = login(TEST_USER_EMAIL, TEST_USER_PASSWORD);

        int page = 1;
        int size = 5;
        GroupState state = GroupState.RECRUITING;

        // when
        ResultActions actions = mockMvc.perform(get("/api/study-groups")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .param("state", state.name())
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size))
        );

        // then
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(9))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.size").value(size))
                .andExpect(jsonPath("$.number").value(page))
                .andExpect(jsonPath("$.content.length()").value(4))
                .andExpect(jsonPath("$.content[0].state").value(state.name()));
    }

    @Test
    @DisplayName("진행중 상태로 필터링하여 스터디 목록을 조회할 수 있다.")
    void givenStateStartAndPageable_whenGetStudyGroups_thenReturnStudyGroups() throws Exception {
        // given
        String accessToken = login(TEST_USER_EMAIL, TEST_USER_PASSWORD);

        int page = 1;
        int size = 5;
        GroupState state = GroupState.START;

        // when
        ResultActions actions = mockMvc.perform(get("/api/study-groups")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .param("state", state.name())
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size))
        );

        // then
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(8))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.size").value(size))
                .andExpect(jsonPath("$.number").value(page))
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].state").value(state.name()));
    }

    @Test
    @DisplayName("특정 스터디 그룹의 상세 정보를 조회할 수 있다.")
    void givenStudyGroupId_whenGetStudyGroupDetail_thenReturnStudyGroup() throws Exception {
        // given
        String accessToken = login(TEST_USER_EMAIL, TEST_USER_PASSWORD);

        Long studyGroupId = TEST_STUDY_GROUP_ID;

        // when
        ResultActions actions = mockMvc.perform(
                get("/api/study-groups/{studyGroupId}", studyGroupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
        );

        // then
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studyGroupId").value(studyGroupId))
                .andExpect(jsonPath("$.title").value("상세정보 스터디"))
                .andExpect(jsonPath("$.curMemberCount").value(1))
                .andExpect(jsonPath("$.capacity").value(5))
                .andExpect(jsonPath("$.policy").value("AUTO"))
                .andExpect(jsonPath("$.state").value("RECRUITING"));

    }

    @Test
    @DisplayName("존재하지 않는 스터디 그룹을 상세 조회요청하면 404 에러를 응답한다.")
    void givenNotExistStudyGroupId_whenGetStudyGroupDetail_thenReturnNotFound() throws Exception {
        // given
        String accessToken = login(TEST_USER_EMAIL, TEST_USER_PASSWORD);

        Long notExistStudyGroupId = 999L;

        // when
        ResultActions actions = mockMvc.perform(
                get("/api/study-groups/{studyGroupId}", notExistStudyGroupId)
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

}
