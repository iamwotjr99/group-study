package com.jaeseok.groupStudy.studyGroup.domain;

import com.jaeseok.groupStudy.studyGroup.domain.participant.Participant;
import com.jaeseok.groupStudy.studyGroup.domain.participant.ParticipantStatus;
import com.jaeseok.groupStudy.studyGroup.domain.vo.StudyGroupInfo;
import java.time.LocalDateTime;
import java.util.Collections;
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
    private final StudyGroupInfo studyGroupInfo;
    private Set<Participant> participantSet;

    public static StudyGroup of(Long id, StudyGroupInfo studyGroupInfo, Set<Participant> participants) {
        return new StudyGroup(id, studyGroupInfo, participants);
    }

    // 스터디 그룹 생성시 방장도 추가
    public static StudyGroup createWithHost(Long groupId, Long hostId, StudyGroupInfo info) {
        Participant host = Participant.host(hostId, groupId);
        Set<Participant> participants = new HashSet<>();
        participants.add(host);
        return new StudyGroup(groupId, info, participants);
    }

    // 참여자를 승인
    public Participant approveParticipant(Long hostId, Participant participant) {
        validateHost(hostId);
        validateParticipantInThisGroup(participant);
        if (participant.status() != ParticipantStatus.PENDING) throw new IllegalStateException("대기중인 유저가 아닙니다.");
        if (isFull()) throw new IllegalArgumentException("현재 방 인원이 가득 찼습니다.");

        Participant approved = participant.approve();
        participantSet.add(approved);
        return approved;
    }

    // 참여자를 거절
    public Participant rejectParticipant(Long hostId, Participant participant) {
        validateHost(hostId);
        validateParticipantInThisGroup(participant);
        if (participant.status() != ParticipantStatus.PENDING) throw new IllegalStateException("대기중인 유저가 아닙니다.");

        return participant.reject();
    }

    // 참여자를 강퇴
    public Participant kickParticipant(Long hostId, Participant participant) {
        validateHost(hostId);
        validateParticipantInThisGroup(participant);
        if (participant.status() != ParticipantStatus.APPROVED) throw new IllegalStateException("참여중인 유저가 아닙니다.");

        Participant kicked = participant.kick();
        participantSet.remove(kicked);
        return kicked;
    }

    // 참여자가 신청 취소
    public Participant participantCancel(Participant participant) {
        validateParticipantInThisGroup(participant);
        if (participant.status() != ParticipantStatus.PENDING) throw new IllegalStateException("대기중인 유저가 아닙니다.");

        return participant.cancel();
    }

    // 참여자가 퇴장
    public Participant participantLeave(Participant participant) {
        validateParticipantInThisGroup(participant);
        findParticipant(participant.userId());
        if (participant.isHost()) throw new IllegalArgumentException("방장은 퇴장할 수 없습니다.");

        Participant left = participant.leave();
        participantSet.remove(left);
        return left;
    }

    // 방이 꽉 찬 상태인지 확인
    public boolean isFull() {
        return participantSet.size() == studyGroupInfo.getCapacity();
    }

    // 현재 스터디 그룹의 방장 리턴
    public Participant getHost() {
        return participantSet.stream()
                .filter(Participant::isHost)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("방장이 없습니다."));
    }

    // 방장인지 권한 확인
    private void validateHost(Long userId) {
        Participant host = findParticipant(userId);
        if (!host.isHost()) throw new IllegalArgumentException("해당 유저는 방장 권한이 없습니다.");
    }

    // userId로 현재 StudyGroup의 참여자인지 탐색
    private Participant findParticipant(Long userId) {
        return participantSet.stream()
                .filter(p -> p.userId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 유저는 이 스터디 그룹의 참여자가 아닙니다."));
    }

    // 해당 참여자가 현재 StudyGroup의 소속인지 확인
    // Pending(승인 대기) 상태여도 소속은 맞음
    private void validateParticipantInThisGroup(Participant participant) {
        if (!participant.studyGroupId().equals(this.id)) {
            throw new IllegalArgumentException("해당 참여자는 이 스터디 그룹의 소속이 아닙니다.");
        }
    }

    public Set<Participant> getParticipants() {
        return Collections.unmodifiableSet(participantSet);
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

    public GroupState getInfoState() {
        return studyGroupInfo.getState();
    }

    private StudyGroup(Long id, StudyGroupInfo studyGroupInfo, Set<Participant> participantSet) {
        this.id = id;
        this.studyGroupInfo = studyGroupInfo;
        this.participantSet = participantSet;
    }
}
