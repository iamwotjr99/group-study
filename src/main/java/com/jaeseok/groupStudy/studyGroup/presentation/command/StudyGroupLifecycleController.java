package com.jaeseok.groupStudy.studyGroup.presentation.command;

import com.jaeseok.groupStudy.auth.domain.UserPrincipal;
import com.jaeseok.groupStudy.studyGroup.application.command.StudyGroupLifecycleService;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.CloseStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.CreateStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.CreateStudyGroupInfo;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.StartStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.presentation.command.dto.CreateStudyGroupRequest;
import com.jaeseok.groupStudy.studyGroup.presentation.command.dto.CreateStudyGroupResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/study-groups")
@RequiredArgsConstructor
public class StudyGroupLifecycleController {

    private final StudyGroupLifecycleService studyGroupLifecycleService;

    @PostMapping
    public ResponseEntity<CreateStudyGroupResponse> createStudyGroup(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody CreateStudyGroupRequest request
    ) {
        CreateStudyGroupCommand command = new CreateStudyGroupCommand(
                userPrincipal.userId(), request.toStudyGroupInfo());
        CreateStudyGroupInfo resultInfo = studyGroupLifecycleService.createStudyGroup(command);
        CreateStudyGroupResponse response = new CreateStudyGroupResponse(resultInfo.studyGroupId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{studyGroupId}/start")
    public ResponseEntity<String> startStudyGroup(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long studyGroupId
    ) {
        StartStudyGroupCommand command = new StartStudyGroupCommand(
                studyGroupId, userPrincipal.userId());
        studyGroupLifecycleService.startStudyGroup(command);

        return ResponseEntity.status(HttpStatus.OK).body("스터디를 시작했습니다.");
    }

    @PostMapping("/{studyGroupId}/close")
    public ResponseEntity<String> closeStudyGroup(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long studyGroupId
    ) {
        CloseStudyGroupCommand command = new CloseStudyGroupCommand(
                studyGroupId, userPrincipal.userId());
        studyGroupLifecycleService.closeStudyGroup(command);

        return ResponseEntity.status(HttpStatus.OK).body("스터디를 종료했습니다.");
    }
}
