package com.jaeseok.groupStudy.common.exception;

import com.jaeseok.groupStudy.studyGroup.exception.StudyGroupMemberAccessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;

@Slf4j
@ControllerAdvice
@Controller
public class WebSocketExceptionHandler {

    @MessageExceptionHandler(StudyGroupMemberAccessException.class)
    @SendToUser(destinations = "/queue/errors", broadcast = false)
    public String handleStudyGroupMemberAccessException(StudyGroupMemberAccessException e) {
        log.warn("STOMP Exception Handler: {}", e.getMessage());
        return e.getMessage();
    }
}
