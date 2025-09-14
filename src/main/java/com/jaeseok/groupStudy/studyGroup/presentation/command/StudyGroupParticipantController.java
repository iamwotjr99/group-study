package com.jaeseok.groupStudy.studyGroup.presentation.command;

import com.jaeseok.groupStudy.auth.domain.UserPrincipal;
import com.jaeseok.groupStudy.studyGroup.application.command.StudyGroupParticipantService;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.ApplyStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.CancelStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.LeaveStudyGroupCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/study-groups/{studyGroupId}")
@RequiredArgsConstructor
public class StudyGroupParticipantController {

    private final StudyGroupParticipantService studyGroupParticipantService;

    @PostMapping("/applicants/apply")
    public ResponseEntity<Void> apply(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long studyGroupId
    ) {
        ApplyStudyGroupCommand command = new ApplyStudyGroupCommand(studyGroupId,
                userPrincipal.userId());
        studyGroupParticipantService.applyForStudyGroup(command);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/applicants/cancel")
    public ResponseEntity<Void> cancel(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long studyGroupId
    ) {
        CancelStudyGroupCommand command = new CancelStudyGroupCommand(studyGroupId,
                userPrincipal.userId());
        studyGroupParticipantService.cancelApplication(command);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/participants/leave")
    public ResponseEntity<Void> leave(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long studyGroupId
    ) {
        LeaveStudyGroupCommand command = new LeaveStudyGroupCommand(studyGroupId,
                userPrincipal.userId());
        studyGroupParticipantService.leaveStudyGroup(command);

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
