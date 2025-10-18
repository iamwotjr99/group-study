package com.jaeseok.groupStudy.webrtc.application;

import com.jaeseok.groupStudy.studyGroup.domain.StudyGroup;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroupCommandRepository;
import com.jaeseok.groupStudy.studyGroup.exception.StudyGroupNotFoundException;
import com.jaeseok.groupStudy.webrtc.dto.SignalMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignalService {

    private final SimpMessagingTemplate messagingTemplate;
    private final StudyGroupCommandRepository studyGroupRepository;

    public void relaySignal(Long roomId, SignalMessage message) {
        Long senderId = message.senderId();
        Long receiverId = message.receiverId();

        validateAccess(roomId, senderId, receiverId);

        String destination = "/sub/signal/user/" + receiverId;

        messagingTemplate.convertAndSend(destination, message);
    }

    private void validateAccess(Long roomId, Long senderId, Long receiverId) {
        StudyGroup studyGroup = checkStudyGroup(roomId);
        studyGroup.isMember(senderId);
        studyGroup.isMember(receiverId);
    }

    private StudyGroup checkStudyGroup(Long studyGroupId) {
        return studyGroupRepository.findById(studyGroupId)
                .orElseThrow(() -> new StudyGroupNotFoundException("존재하지 않는 스터디 그룹 입니다."));
    }
}
