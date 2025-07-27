package com.jaeseok.groupStudy.studyGroup.domain;

import com.jaeseok.groupStudy.participant.domain.Participant;
import com.jaeseok.groupStudy.participant.domain.ParticipantStatus;
import com.jaeseok.groupStudy.studyGroup.domain.vo.StudyGroupInfo;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 역할 - 스터디 그룹 방 자체
 * 책임 - 모집 방식 설정/변경, 그룹 상태 설정/변경, 참여자 승인/거절/강퇴, 참여자 정원 관리
 * 협력 - 방장, 참여자
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class StudyGroup {
    private final Long id;
    private final Long hostId;
    private final StudyGroupInfo studyGroupInfo;
    private Set<Participant> participantSet;


    static public StudyGroup create(Long hostId, StudyGroupInfo studyGroupInfo) {
        return new StudyGroup(null, hostId, studyGroupInfo, new HashSet<>());
    }

    // 테스트를 위한 메서드
    static public StudyGroup createForTest(Long id, Long hostId, StudyGroupInfo studyGroupInfo) {
        return new StudyGroup(id, hostId, studyGroupInfo, new HashSet<>());
    }

    // 참여자 승인
    public Participant approveParticipant(Long hostId, Participant participant) {
        validateHost(hostId);
        validateParticipantInThisGroup(participant);
        if (participant.state() != ParticipantStatus.PENDING) throw new IllegalStateException("대기중인 유저가 아닙니다.");
        if (isPull()) throw new IllegalArgumentException("현재 방 인원이 가득 찼습니다.");

        Participant approved = participant.approve();
        participantSet.add(approved);
        return approved;
    }

    // 참여자 거절
    public Participant rejectParticipant(Long hostId, Participant participant) {
        validateHost(hostId);
        validateParticipantInThisGroup(participant);
        if (participant.state() != ParticipantStatus.PENDING) throw new IllegalStateException("대기중인 유저가 아닙니다.");

        return participant.reject();
    }

    // 참여자 강퇴
    public Participant kickParticipant(Long hostId, Participant participant) {
        validateHost(hostId);
        validateParticipantInThisGroup(participant);
        if (participant.state() != ParticipantStatus.APPROVED) throw new IllegalStateException("참여중인 유저가 아닙니다.");

        Participant kicked = participant.kick();
        participantSet.remove(kicked);
        return kicked;
    }

    // 방이 꽉 찬 상태인지 확인
    public boolean isPull() {
        return participantSet.size() == studyGroupInfo.getCapacity();
    }

    // 방장인지 권한 확인
    private void validateHost(Long hostId) {
        if (!hostId.equals(this.hostId)) throw new IllegalArgumentException("해당 유저는 방장 권한이 없습니다.");
    }

    // 해당 참여자가 현재 StudyGroup의 소속인지 확인
    private void validateParticipantInThisGroup(Participant participant) {
        if (!participant.studyGroupId().equals(this.id)) {
            throw new IllegalArgumentException("해당 참여자는 이 스터디 그룹에 속하고 있지 않습니다.");
        }
    }

    public String getInfoTitle() {
        return studyGroupInfo.getTitle();
    }

    public int getInfoCapacity() {
        return studyGroupInfo.getCapacity();
    }

    public LocalDateTime getInfoDeadline() {
        return studyGroupInfo.getDeadline();
    }

    public RecruitingPolicy getInfoPolicy() {
        return studyGroupInfo.getPolicy();
    }

    public GroupStatus getInfoState() {
        return studyGroupInfo.getState();
    }

    private StudyGroup(Long id, Long hostId, StudyGroupInfo studyGroupInfo, Set<Participant> participantSet) {
        this.id = id;
        this.hostId = hostId;
        this.studyGroupInfo = studyGroupInfo;
        this.participantSet = participantSet;
    }
}
