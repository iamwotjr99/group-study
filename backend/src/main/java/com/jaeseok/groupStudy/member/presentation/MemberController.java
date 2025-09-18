package com.jaeseok.groupStudy.member.presentation;

import com.jaeseok.groupStudy.auth.domain.UserPrincipal;
import com.jaeseok.groupStudy.member.application.MemberService;
import com.jaeseok.groupStudy.member.application.dto.MemberInfoDto;
import com.jaeseok.groupStudy.member.presentation.dto.MemberInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/me")
    public ResponseEntity<MemberInfoResponse> getMemberInfo(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal.userId();
        MemberInfoDto result = memberService.getMemberInfo(userId);

        MemberInfoResponse memberInfoResponse = MemberInfoResponse.of(result);

        return ResponseEntity.status(HttpStatus.OK).body(memberInfoResponse);
    }
}
