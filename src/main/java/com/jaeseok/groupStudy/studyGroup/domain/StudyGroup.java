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

    // PENDING, APPROVED 유저 모두 관리
    private final Set<Participant> participantSet;

    public static StudyGroup of(Long id, StudyGroupInfo studyGroupInfo, Set<Participant> participants) {
        return new StudyGroup(id, studyGroupInfo, participants);
    }

    // 스터디 그룹 생성시 방장도 추가
    public static StudyGroup createWithHost(Long hostId, StudyGroupInfo info) {
        Long newGroupId = null; // 신규 그룹이라 ID null, JPA를 통해 DB에 저장될때 생성

        Participant host = Participant.host(hostId, newGroupId);
        Set<Participant> participants = new HashSet<>();
        participants.add(host);
        return new StudyGroup(newGroupId, info, participants);
    }

    public void apply(Long userId) {
        if (existsParticipant(userId)) {
            throw new IllegalArgumentException("이미 신청중이거나 참여중인 스터디 그룹입니다.");
        }

        Participant applied = Participant.apply(userId, this.id);
        this.participantSet.add(applied);
    }

    // 참여자를 승인
    public void approveParticipant(Long hostId, Long applicantId) {
        validateHost(hostId);

        Participant participant = findParticipant(applicantId);
        if (participant.status() != ParticipantStatus.PENDING) throw new IllegalStateException("대기중인 유저가 아닙니다.");
        if (isFull()) throw new IllegalArgumentException("현재 방 인원이 가득 찼습니다.");

        this.participantSet.remove(participant);
        Participant approved = participant.approve();
        this.participantSet.add(approved);
    }

    // 참여자를 거절
    public void rejectParticipant(Long hostId, Long rejectedId) {
        validateHost(hostId);
        Participant participant = findParticipant(rejectedId);

        if (participant.status() != ParticipantStatus.PENDING) throw new IllegalStateException("대기중인 유저가 아닙니다.");

        this.participantSet.remove(participant);
        Participant rejected = participant.reject();
        this.participantSet.add(rejected);
    }

    // 참여자를 강퇴
    public void kickParticipant(Long hostId, Long kickedId) {
        validateHost(hostId);
        Participant participant = findParticipant(kickedId);

        if (participant.status() != ParticipantStatus.APPROVED) throw new IllegalStateException("참여중인 유저가 아닙니다.");

        this.participantSet.remove(participant);
        Participant kicked = participant.kick();
        this.participantSet.add(kicked);
    }

    // 참여자가 신청 취소
    public void participantApplyCancel(Long participantId) {
        Participant participant = findParticipant(participantId);
        if (participant.status() != ParticipantStatus.PENDING) throw new IllegalStateException("대기중인 유저가 아닙니다.");

        this.participantSet.remove(participant);
        Participant cancelled = participant.cancel();
        this.participantSet.add(cancelled);
    }

    // 참여자가 퇴장
    public void participantLeave(Long participantId) {
        Participant participant = findParticipant(participantId);
        if (participant.isHost()) throw new IllegalArgumentException("방장은 퇴장할 수 없습니다.");

        this.participantSet.remove(participant);
        Participant left = participant.leave();
        this.participantSet.add(left);
    }

    // 방이 꽉 찬 상태인지 확인
    private boolean isFull() {
        return getApprovedParticipantCount() == this.getInfoCapacity();
    }

    // PENDING, APPROVED 상태인 참여자들 중 실 참여자인 APPROVED 상태 참여자의 수를 리턴
    private int getApprovedParticipantCount() {
        return (int) this.participantSet.stream()
                .filter(p -> p.status() == ParticipantStatus.APPROVED)
                .count();
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

    // userId로 유저가 현재 StudyGroup에 참여중인지 검사
    private boolean existsParticipant(Long userId) {
        return participantSet.stream()
                .anyMatch(p -> p.userId().equals(userId));
    }

//    public Set<Participant> getParticipants() {
//        return Collections.unmodifiableSet(participantSet);
//    }

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
}
