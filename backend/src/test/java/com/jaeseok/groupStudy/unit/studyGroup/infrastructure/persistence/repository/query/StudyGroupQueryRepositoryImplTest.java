package com.jaeseok.groupStudy.unit.studyGroup.infrastructure.persistence.repository.query;

import static org.assertj.core.api.Assertions.*;

import com.jaeseok.groupStudy.member.infrastructure.persistence.JpaMemberRepository;
import com.jaeseok.groupStudy.member.infrastructure.persistence.entity.MemberEntity;
import com.jaeseok.groupStudy.member.infrastructure.persistence.entity.MemberInfoEntity;
import com.jaeseok.groupStudy.studyGroup.domain.GroupState;
import com.jaeseok.groupStudy.studyGroup.domain.RecruitingPolicy;
import com.jaeseok.groupStudy.studyGroup.domain.participant.ParticipantRole;
import com.jaeseok.groupStudy.studyGroup.domain.participant.ParticipantStatus;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.entity.ParticipantEntity;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.entity.StudyGroupEntity;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.entity.StudyGroupInfoEntity;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.JpaParticipantRepository;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.command.JpaStudyGroupCommandRepository;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.query.StudyGroupQueryRepository;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.query.StudyGroupQueryRepositoryImpl;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.query.dto.ParticipantDto;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.query.dto.StudyGroupDetailDto;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.query.dto.StudyGroupSummaryDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataJpaTest
@Import({StudyGroupQueryRepositoryImpl.class, StudyGroupQueryRepositoryImplTest.QueryDSLConfig.class})
class StudyGroupQueryRepositoryImplTest {

    // QueryDSL 설정을 위한 내부 클래스
    static class QueryDSLConfig {
        @PersistenceContext
        private EntityManager entityManager;

        @Bean
        public JPAQueryFactory jpaQueryFactory() {
            return new JPAQueryFactory(entityManager);
        }
    }

    @Autowired
    JPAQueryFactory jpaQueryFactory;

    @Autowired
    JpaStudyGroupCommandRepository jpaStudyGroupCommandRepository;

    @Autowired
    JpaParticipantRepository jpaParticipantRepository;

    @Autowired
    JpaMemberRepository jpaMemberRepository;

    StudyGroupQueryRepository studyGroupQueryRepository;

    List<StudyGroupEntity> savedStudyGroups = new ArrayList<>();

    @BeforeEach
    void setUp() {
        studyGroupQueryRepository = new StudyGroupQueryRepositoryImpl(jpaQueryFactory);
    }

    @Test
    @DisplayName("스터디 그룹 상세 정보 조회시, 해당 스터디 그룹이 존재하면 조회할 수 있다.")
    void givenStudyGroupId_whenFindStudyGroupDetailById_thenReturnStudyGroupDetailDto() {
        // given
        MemberEntity host = createAndSaveMember("호스트");
        MemberEntity approvedUser = createAndSaveMember("승인된 유저");
        MemberEntity pendingUser = createAndSaveMember("대기중 유저");
        StudyGroupEntity group = createAndSaveStudyGroup("스터디 그룹 테스트 001", GroupState.RECRUITING, 3);
        createAndSaveParticipant(host, group, ParticipantRole.HOST, ParticipantStatus.APPROVED);
        createAndSaveParticipant(approvedUser, group, ParticipantRole.MEMBER, ParticipantStatus.APPROVED);
        createAndSaveParticipant(pendingUser, group, ParticipantRole.MEMBER, ParticipantStatus.PENDING);

        // when
        StudyGroupDetailDto resultDto = studyGroupQueryRepository.findStudyGroupDetailById(
                group.getId()).get();

        // then
        assertThat(resultDto).isNotNull();
        assertThat(resultDto.title()).isEqualTo("스터디 그룹 테스트 001");
        assertThat(resultDto.capacity()).isEqualTo(3);
        assertThat(resultDto.policy()).isEqualTo(RecruitingPolicy.APPROVAL);
        assertThat(resultDto.state()).isEqualTo(GroupState.RECRUITING);
        assertThat(resultDto.curMemberCount()).isEqualTo(2);
        assertThat(resultDto.participants())
                .hasSize(3)
                .extracting(ParticipantDto::userId)
                .containsExactlyInAnyOrder(host.getId(), approvedUser.getId(), pendingUser.getId());
    }

    @Test
    @DisplayName("스터디 그룹 상세 정보 조회시, 해당 스터디 그룹이 존재하지 않으면 Optional.null을 리턴한다.")
    void givenNotExistStudyGroupId_whenFindStudyGroupDetailById_thenReturnNull() {
        // given
        Long notExistStudyGroupId = 9999L;

        // when
        Optional<StudyGroupDetailDto> studyGroupDetailById = studyGroupQueryRepository.findStudyGroupDetailById(
                notExistStudyGroupId);

        // then
        assertThat(studyGroupDetailById).isNotNull();
        assertThat(studyGroupDetailById.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("스터디 그룹 목록 조회 시, 상태(state)가 null이면 전체 조회가 되어야한다.")
    void givenNullStateAndPageable_whenFindStudyGroupSummaries_thenReturnAllStudyGroupSummaries() {
        // given
        saveStudyGroupListToPaging(10, GroupState.RECRUITING);
        saveStudyGroupListToPaging(10, GroupState.START);
        saveStudyGroupListToPaging(5, GroupState.CLOSE);

        int page = 1;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);

        // when
        Page<StudyGroupSummaryDto> resultPage = studyGroupQueryRepository.findStudyGroupSummaries(
                null, pageable);

        // then
        assertThat(resultPage.getTotalElements()).isEqualTo(25); // 총 데이터 갯수
        assertThat(resultPage.getTotalPages()).isEqualTo(3); // 총 페이지 갯수
        assertThat(resultPage.getNumber()).isEqualTo(page); // 현재 페이지 번호
        assertThat(resultPage.getSize()).isEqualTo(size); // 현재 페이지에 요청한 크기
        assertThat(resultPage.getContent()).hasSize(size); // 현재 페이지의 데이터 개수
    }

    @Test
    @DisplayName("스터디 그룹 목록 조회 시, 상태(state)가 '모집중'이면 모집중인 그룹만 필터링 되어야한다.")
    void givenRecruitingState_whenFindStudyGroupSummaries_thenReturnRecruitingStudyGroupSummaries() {
        // given
        saveStudyGroupListToPaging(10, GroupState.RECRUITING);
        saveStudyGroupListToPaging(5, GroupState.START);
        Pageable pageable = PageRequest.of(0, 5);

        // when
        Page<StudyGroupSummaryDto> resultPage = studyGroupQueryRepository.findStudyGroupSummaries(
                GroupState.RECRUITING, pageable);

        // then
        assertThat(resultPage.getTotalElements()).isEqualTo(10);
        assertThat(resultPage.getTotalPages()).isEqualTo(2);
        assertThat(resultPage.getContent())
                .hasSize(5)
                .allMatch(dto -> dto.state().equals(GroupState.RECRUITING));
    }

    @Test
    @DisplayName("스터디 그룹 목록 조회 시, 상태(state)가 '진행중'이면 진행중인 그룹만 필터링 되어야한다.")
    void givenStartState_whenFindStudyGroupSummaries_thenReturnStartStudyGroupSummaries() {
        // given
        saveStudyGroupListToPaging(10, GroupState.RECRUITING);
        saveStudyGroupListToPaging(5, GroupState.START);
        Pageable pageable = PageRequest.of(0, 5);

        // when
        Page<StudyGroupSummaryDto> resultPage = studyGroupQueryRepository.findStudyGroupSummaries(
                GroupState.START, pageable);

        // then
        assertThat(resultPage.getTotalElements()).isEqualTo(5);
        assertThat(resultPage.getTotalPages()).isEqualTo(1);
        assertThat(resultPage.getContent())
                .hasSize(5)
                .allMatch(dto -> dto.state().equals(GroupState.START));
    }

    @Test
    @DisplayName("스터디 그룹 목록 조회 시, 상태(state)가 '종료'이면 종료한 그룹만 필터링 되어야한다.")
    void givenCloseState_whenFindStudyGroupSummaries_thenReturnCloseStudyGroupSummaries() {
        // given
        saveStudyGroupListToPaging(10, GroupState.RECRUITING);
        saveStudyGroupListToPaging(5, GroupState.START);
        saveStudyGroupListToPaging(6, GroupState.CLOSE);
        Pageable pageable = PageRequest.of(0, 5);

        // when
        Page<StudyGroupSummaryDto> resultPage = studyGroupQueryRepository.findStudyGroupSummaries(
                GroupState.CLOSE, pageable);

        // then
        assertThat(resultPage.getTotalElements()).isEqualTo(6);
        assertThat(resultPage.getTotalPages()).isEqualTo(2);
        assertThat(resultPage.getContent())
                .hasSize(5)
                .allMatch(dto -> dto.state().equals(GroupState.CLOSE));
    }

    @Test
    @DisplayName("스터디 그룹안에 있는 모든 상태의 참여자들을 조회할 수 있다.")
    void givenStudyGroupId_whenFindParticipantsByStudyGroupId_thenReturnParticipantDtos() {
        // given
        MemberEntity host = createAndSaveMember("호스트");
        MemberEntity approvedUser = createAndSaveMember("승인된 유저");
        MemberEntity pendingUser = createAndSaveMember("대기중 유저");
        StudyGroupEntity group = createAndSaveStudyGroup("스터디 그룹 테스트 001", GroupState.RECRUITING, 3);
        createAndSaveParticipant(host, group, ParticipantRole.HOST, ParticipantStatus.APPROVED);
        createAndSaveParticipant(approvedUser, group, ParticipantRole.MEMBER, ParticipantStatus.APPROVED);
        createAndSaveParticipant(pendingUser, group, ParticipantRole.MEMBER, ParticipantStatus.PENDING);

        // when
        Set<ParticipantDto> resultDtos = studyGroupQueryRepository.findParticipantsByStudyGroupId(
                group.getId());

        // then
        assertThat(resultDtos).isNotNull();
        assertThat(resultDtos)
                .hasSize(3)
                .extracting(ParticipantDto::userId)
                .containsExactlyInAnyOrder(host.getId(), approvedUser.getId(), pendingUser.getId());
    }

    private MemberEntity createAndSaveMember(String nickname) {
        MemberEntity member = MemberEntity.builder()
                .memberInfoEntity(MemberInfoEntity.builder().nickname(nickname).email(nickname + "@test.com").build())
                .build();
        return jpaMemberRepository.save(member);
    }

    private StudyGroupEntity createAndSaveStudyGroup(String title, GroupState state, int capacity) {
        StudyGroupInfoEntity info = StudyGroupInfoEntity.builder()
                .title(title)
                .capacity(capacity)
                .deadline(LocalDateTime.now().plusDays(1))
                .policy(RecruitingPolicy.APPROVAL)
                .state(state)
                .build();
        return jpaStudyGroupCommandRepository.save(StudyGroupEntity.builder().infoEntity(info).build());
    }

    private ParticipantEntity createAndSaveParticipant(MemberEntity member, StudyGroupEntity group, ParticipantRole role, ParticipantStatus status) {
        ParticipantEntity participant = ParticipantEntity.builder()
                .userId(member.getId())
                .studyGroupEntity(group)
                .role(role)
                .status(status)
                .build();
        return jpaParticipantRepository.save(participant);
    }

    private void saveStudyGroupListToPaging(int size, GroupState state) {
        for (int i = 0; i < size; i++) {
            StudyGroupInfoEntity infoEntity = StudyGroupInfoEntity.builder()
                    .title("페이징 테스트 " + i)
                    .capacity(5)
                    .deadline(LocalDateTime.now().plusDays(i))
                    .policy(RecruitingPolicy.APPROVAL)
                    .state(state)
                    .build();
            StudyGroupEntity studyGroupEntity = StudyGroupEntity.builder()
                    .infoEntity(infoEntity)
                    .participantEntitySet(new HashSet<>())
                    .build();

            savedStudyGroups.add(studyGroupEntity);
        }

        jpaStudyGroupCommandRepository.saveAll(savedStudyGroups);
    }
}