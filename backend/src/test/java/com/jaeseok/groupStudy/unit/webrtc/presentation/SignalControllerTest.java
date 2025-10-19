package com.jaeseok.groupStudy.unit.webrtc.presentation;

import static org.mockito.BDDMockito.*;

import com.jaeseok.groupStudy.webrtc.application.SignalService;
import com.jaeseok.groupStudy.webrtc.dto.SignalMessage;
import com.jaeseok.groupStudy.webrtc.presentation.SignalController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Signal Controller 단위 테스트")
class SignalControllerTest {

    @InjectMocks
    SignalController signalController;

    @Mock
    SignalService signalService;

    @Test
    @DisplayName("시그널링 메시지를 수신하면 서비스 계층에 위임한다")
    void givenRoomIdAndMessage_whenHandleSignal_thenDelegateToService() {
        // given
        Long roomId = 1L;
        Long senderId = 10L;
        Long receiverId = 20L;
        SignalMessage message = new SignalMessage("offer", "sdp data ...", senderId, receiverId);

        willDoNothing().given(signalService).relaySignal(roomId, message);

        // when
        signalController.handleSignalMessage(roomId, message);

        // then
        verify(signalService, times(1)).relaySignal(roomId, message);
        verifyNoMoreInteractions(signalService);
    }
}
