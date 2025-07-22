package com.jaeseok.groupStudy.studyGroup.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("스터디 그룹 도메인 테스트")
class StudyGroupTest {

    private static final Long HOST_ID = 1L;


    @Test
    @DisplayName("유저가 스터디 그룹을 만들 수 있다.")
    void givenStudyGroupInfoWithHostId_whenCreateStudyGroup_thenReturnStudyGroup() {
        // given & when
        StudyGroup studyGroup = StudyGroup.create(HOST_ID, "Spring Study", 3,
                LocalDateTime.now().plusDays(1));

        // then
        assertEquals(HOST_ID, studyGroup.getHostId());
        assertEquals("Spring Study", studyGroup.getTitle());
    }

    @Test
    void approveParticipant() {
    }

    @Test
    void rejectParticipant() {
    }

    @Test
    void kickParticipant() {
    }

    @Test
    void autoPolicy() {
    }

    @Test
    void approvePolicy() {
    }

    @Test
    void recruit() {
    }

    @Test
    void close() {
    }

    @Test
    void start() {
    }

    @Test
    void isPull() {
    }
}