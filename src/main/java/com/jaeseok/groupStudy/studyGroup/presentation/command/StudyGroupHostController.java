package com.jaeseok.groupStudy.studyGroup.presentation.command;

import com.jaeseok.groupStudy.auth.domain.UserPrincipal;
import com.jaeseok.groupStudy.studyGroup.application.command.StudyGroupHostService;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.ApproveStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.KickStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.RejectStudyGroupCommand;
import lombok.RequiredArgsConstructor;
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
public class StudyGroupHostController {

    private final StudyGroupHostService studyGroupHostService;

    @PostMapping("/applicants/{applicantId}/approve")
    public ResponseEntity<String> approveApplication(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long studyGroupId,
            @PathVariable Long applicantId
    ) {
        ApproveStudyGroupCommand command = new ApproveStudyGroupCommand(
                studyGroupId, userPrincipal.userId(), applicantId);
        studyGroupHostService.approveApplication(command);

        return ResponseEntity.ok().body("승인 성공");
    }

    @DeleteMapping("/applicants/{applicantId}/reject")
    public ResponseEntity<String> rejectApplication(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long studyGroupId,
            @PathVariable Long applicantId
    ) {
        RejectStudyGroupCommand command = new RejectStudyGroupCommand(
                studyGroupId, userPrincipal.userId(), applicantId);
        studyGroupHostService.rejectApplication(command);

        return ResponseEntity.ok().body("거절 성공");
    }

    @DeleteMapping("/participants/{participantId}/kick")
    public ResponseEntity<String> kickParticipant(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long studyGroupId,
            @PathVariable Long participantId
    ) {
        KickStudyGroupCommand command = new KickStudyGroupCommand(studyGroupId,
                userPrincipal.userId(), participantId);
        studyGroupHostService.kickParticipation(command);

        return ResponseEntity.ok().body("강퇴 성공");
    }
}
