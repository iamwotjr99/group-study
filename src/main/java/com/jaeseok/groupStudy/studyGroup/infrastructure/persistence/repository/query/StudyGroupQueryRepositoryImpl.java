package com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.query;

import com.jaeseok.groupStudy.member.infrastructure.persistence.entity.QMemberEntity;
import com.jaeseok.groupStudy.studyGroup.domain.GroupState;
import com.jaeseok.groupStudy.studyGroup.domain.participant.ParticipantStatus;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.entity.QParticipantEntity;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.entity.QStudyGroupEntity;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.query.dto.ParticipantDto;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.query.dto.StudyGroupDetailDto;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.query.dto.StudyGroupSummaryDto;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StudyGroupQueryRepositoryImpl implements StudyGroupQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<StudyGroupDetailDto> findStudyGroupDetailById(Long studyGroupId) {
        QStudyGroupEntity studyGroupEntity = QStudyGroupEntity.studyGroupEntity;
        QParticipantEntity participantEntity = QParticipantEntity.participantEntity;

        StudyGroupDetailDto studyGroupDetailDto = queryFactory
                .select(Projections.constructor(StudyGroupDetailDto.class,
                        studyGroupEntity.id,
                        studyGroupEntity.infoEntity.title,
                        JPAExpressions // curMemberCount
                                .select(participantEntity.count())
                                .from(participantEntity)
                                .where(isApprovedParticipant(studyGroupId)),
                        studyGroupEntity.infoEntity.capacity,
                        studyGroupEntity.infoEntity.deadline,
                        studyGroupEntity.infoEntity.policy,
                        studyGroupEntity.infoEntity.state
                ))
                .from(studyGroupEntity)
                .where(studyGroupEntity.id.eq(studyGroupId))
                .fetchOne();

        Optional<StudyGroupDetailDto> optionalStudyGroupDetailDto = Optional.ofNullable(studyGroupDetailDto);

        optionalStudyGroupDetailDto.ifPresent(dto -> {
            Set<ParticipantDto> participants = findParticipantsByStudyGroupId(studyGroupId);
            dto.withParticipants(participants);
        });

        return optionalStudyGroupDetailDto;
    }

    @Override
    public Page<StudyGroupSummaryDto> findStudyGroupSummaries(GroupState state, Pageable pageable) {
        QStudyGroupEntity studyGroupEntity = QStudyGroupEntity.studyGroupEntity;
        QParticipantEntity participantEntity = QParticipantEntity.participantEntity;

        List<StudyGroupSummaryDto> result = queryFactory
                .select(Projections.constructor(StudyGroupSummaryDto.class,
                        studyGroupEntity.id,
                        studyGroupEntity.infoEntity.title,
                        JPAExpressions
                                .select(participantEntity.count())
                                .from(participantEntity)
                                .where(
                                        participantEntity.studyGroupEntity.id.eq(
                                                studyGroupEntity.id),
                                        participantEntity.status.eq(ParticipantStatus.APPROVED)
                                ),
                        studyGroupEntity.infoEntity.capacity,
                        studyGroupEntity.infoEntity.deadline,
                        studyGroupEntity.infoEntity.policy,
                        studyGroupEntity.infoEntity.state
                ))
                .from(studyGroupEntity)
                .orderBy(studyGroupEntity.createdAt.desc())
                .where(stateEq(state))
                // 페이징
                .offset(pageable.getOffset()) // 몇 번째 데이터부터 시작할지
                .limit(pageable.getPageSize()) // 몇 개를 가져올지
                //
                .fetch();

        Long totalCount = queryFactory
                .select(studyGroupEntity.count())
                .from(studyGroupEntity)
                .where(stateEq(state))
                .fetchOne();

        return new PageImpl<>(result, pageable, totalCount);
    }


    @Override
    public Set<ParticipantDto> findParticipantsByStudyGroupId(Long studyGroupId) {
        QParticipantEntity participantEntity = QParticipantEntity.participantEntity;
        QMemberEntity memberEntity = QMemberEntity.memberEntity;

        return queryFactory
                .select(Projections.constructor(ParticipantDto.class,
                        participantEntity.id,
                        memberEntity.memberInfoEntity.nickname,
                        memberEntity.memberInfoEntity.email,
                        participantEntity.role,
                        participantEntity.status))
                .from(participantEntity)
                .join(memberEntity)
                .on(participantEntity.userId.eq(memberEntity.id))
                .where(participantEntity.studyGroupEntity.id.eq(studyGroupId))
                .fetch()
                .stream()
                .collect(Collectors.toSet());
    }

    private BooleanExpression isApprovedParticipant(Long studyGroupId) {
        QParticipantEntity participantEntity = QParticipantEntity.participantEntity;
        return participantEntity.studyGroupEntity.id.eq(studyGroupId)
                .and(participantEntity.status.eq(ParticipantStatus.APPROVED));
    }

    private BooleanExpression stateEq(GroupState state) {
        QStudyGroupEntity studyGroupEntity = QStudyGroupEntity.studyGroupEntity;
        // state 파라미터가 null이면 모든 상태의 스터디 그룹 조회
        // state 파라미터가 존재하면 해당 상태의 스터디 그룹 조회
        return state == null ? null : studyGroupEntity.infoEntity.state.eq(state);
    }
}
