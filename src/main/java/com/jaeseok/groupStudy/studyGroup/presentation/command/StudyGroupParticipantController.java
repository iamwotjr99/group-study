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
    public ResponseEntity<String> apply(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long studyGroupId
    ) {
        ApplyStudyGroupCommand command = new ApplyStudyGroupCommand(studyGroupId,
                userPrincipal.userId());
        studyGroupParticipantService.applyForStudyGroup(command);

        return ResponseEntity.status(HttpStatus.CREATED).body("가입 신청에 성공했습니다.");
    }

    @DeleteMapping("/applicants/cancel")
    public ResponseEntity<String> cancel(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long studyGroupId
    ) {
        CancelStudyGroupCommand command = new CancelStudyGroupCommand(studyGroupId,
                userPrincipal.userId());
        studyGroupParticipantService.cancelApplication(command);

        return ResponseEntity.status(HttpStatus.OK).body("가입 신청 취소에 성공했습니다.");
    }

    @DeleteMapping("/participants/leave")
    public ResponseEntity<String> leave(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long studyGroupId
    ) {
        LeaveStudyGroupCommand command = new LeaveStudyGroupCommand(studyGroupId,
                userPrincipal.userId());
        studyGroupParticipantService.leaveStudyGroup(command);

        return ResponseEntity.status(HttpStatus.OK).body("스터디 그룹을 퇴장했습니다.");
    }
}
