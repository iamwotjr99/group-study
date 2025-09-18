package com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.query;

import com.jaeseok.groupStudy.member.infrastructure.persistence.entity.MemberEntity;
import com.jaeseok.groupStudy.member.infrastructure.persistence.entity.QMemberEntity;
import com.jaeseok.groupStudy.studyGroup.domain.GroupState;
import com.jaeseok.groupStudy.studyGroup.domain.participant.ParticipantStatus;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.entity.ParticipantEntity;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.entity.QParticipantEntity;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.entity.QStudyGroupEntity;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.query.dto.ParticipantDto;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.query.dto.StudyGroupDetailDto;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.query.dto.StudyGroupSummaryDto;
import com.querydsl.core.Tuple;
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

        StudyGroupDetailDto initialDto = queryFactory
                .select(Projections.constructor(StudyGroupDetailDto.class,
                        studyGroupEntity.id,
                        studyGroupEntity.infoEntity.title,
                        JPAExpressions // curMemberCount
                                .select(participantEntity.count().intValue())
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

        if (initialDto == null) {
            return Optional.empty();
        }

        Set<ParticipantDto> participants = findParticipantsByStudyGroupId(studyGroupId);
        StudyGroupDetailDto finalDto = initialDto.withParticipants(participants);

        return Optional.of(finalDto);
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
                                .select(participantEntity.count().intValue())
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

        List<Tuple> result = queryFactory
                .select(participantEntity, memberEntity)
                .from(participantEntity)
                .join(memberEntity)
                .on(participantEntity.userId.eq(memberEntity.id))
                .where(isApprovedParticipant(studyGroupId))
                .fetch();

        return result.stream()
                .map(tuple -> {
                    ParticipantEntity participant = tuple.get(participantEntity);
                    MemberEntity member = tuple.get(memberEntity);
                    return new ParticipantDto(
                            member.getId(),
                            member.getMemberInfoEntity().getNickname(),
                            member.getMemberInfoEntity().getEmail(),
                            participant.getRole(),
                            participant.getStatus()
                    );
                })
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
