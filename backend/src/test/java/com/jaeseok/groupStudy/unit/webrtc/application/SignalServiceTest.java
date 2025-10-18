package com.jaeseok.groupStudy.unit.webrtc.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.jaeseok.groupStudy.studyGroup.domain.StudyGroup;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroupCommandRepository;
import com.jaeseok.groupStudy.studyGroup.exception.StudyGroupMemberAccessException;
import com.jaeseok.groupStudy.studyGroup.exception.StudyGroupNotFoundException;
import com.jaeseok.groupStudy.webrtc.application.SignalService;
import com.jaeseok.groupStudy.webrtc.dto.SignalMessage;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
@DisplayName("Signaling Service 단위 테스트")
class SignalServiceTest {

    @InjectMocks
    SignalService signalService;

    @Mock
    SimpMessagingTemplate messagingTemplate;

    @Mock
    StudyGroupCommandRepository studyGroupRepository;

    @Mock
    StudyGroup mockStudyGroup;

    Long roomId;
    Long senderId;
    Long receiverId;
    SignalMessage signalMessage;

    @BeforeEach
    void setUp() {
        roomId = 1L;
        senderId = 10L;
        receiverId = 20L;
        signalMessage = new SignalMessage("offer", "sdp data ...", senderId, receiverId);
    }

    @Test
    @DisplayName("sender의 시그널을 receiver에게 방송한다.")
    void givenRoomIdAndSignalMessage_whenRelaySignal_thenCollectExecute() {
        // given
        given(studyGroupRepository.findById(roomId)).willReturn(Optional.of(mockStudyGroup));

        willDoNothing().given(mockStudyGroup).isMember(senderId);
        willDoNothing().given(mockStudyGroup).isMember(receiverId);

        String expectedDestination = "/sub/signal/user/" + receiverId;

        // when
        signalService.relaySignal(roomId, signalMessage);

        // then
        verify(studyGroupRepository, times(1)).findById(roomId);
        verify(mockStudyGroup, times(1)).isMember(senderId);
        verify(mockStudyGroup, times(1)).isMember(receiverId);

        verify(messagingTemplate, times(1)).convertAndSend(expectedDestination, signalMessage);
    }

    @Test
    @DisplayName("시그널링 중계할 때, 해당 스터디 그룹이 존재하지 않으면 예외를 던진다.")
    void givenNotExistRoomId_whenRelaySignal_thenThrowException() {
        // given
        given(studyGroupRepository.findById(roomId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> signalService.relaySignal(roomId, signalMessage))
                .isInstanceOf(StudyGroupNotFoundException.class)
                .hasMessage("존재하지 않는 스터디 그룹 입니다.");

        verify(messagingTemplate, never()).convertAndSend(anyString(), any(SignalMessage.class));
    }

    @Test
    @DisplayName("시그널링 중계할 때, sender가 해당 스터디 그룹의 멤버가 아니라면 예외를 던진다.")
    void givenNotExistSenderMember_whenRelaySignal_thenThrowException() {
        // given
        given(studyGroupRepository.findById(roomId)).willReturn(Optional.of(mockStudyGroup));

        willThrow(new StudyGroupMemberAccessException("해당 유저는 승인된 참여자가 아닙니다."))
                .given(mockStudyGroup).isMember(senderId);

        // when & then
        assertThatThrownBy(() -> signalService.relaySignal(roomId, signalMessage))
                .isInstanceOf(StudyGroupMemberAccessException.class)
                .hasMessage("해당 유저는 승인된 참여자가 아닙니다.");

        verify(studyGroupRepository, times(1)).findById(roomId);
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(SignalMessage.class));
        verify(mockStudyGroup, never()).isMember(receiverId);
    }

    @Test
    @DisplayName("시그널링 중계할 때, receiver가 해당 스터디 그룹의 멤버가 아니라면 예외를 던진다.")
    void givenNotExistReceiverMember_whenRelaySignal_thenThrowException() {
        // given
        given(studyGroupRepository.findById(roomId)).willReturn(Optional.of(mockStudyGroup));
        willDoNothing().given(mockStudyGroup).isMember(senderId);

        willThrow(new StudyGroupMemberAccessException("해당 유저는 승인된 참여자가 아닙니다."))
                .given(mockStudyGroup).isMember(receiverId);

        // when & then
        assertThatThrownBy(() -> signalService.relaySignal(roomId, signalMessage))
                .isInstanceOf(StudyGroupMemberAccessException.class)
                .hasMessage("해당 유저는 승인된 참여자가 아닙니다.");

        verify(studyGroupRepository, times(1)).findById(roomId);
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(SignalMessage.class));
        verify(mockStudyGroup, times(1)).isMember(senderId);
    }
}
