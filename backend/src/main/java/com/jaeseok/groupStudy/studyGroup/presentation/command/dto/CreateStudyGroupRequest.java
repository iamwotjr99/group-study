package com.jaeseok.groupStudy.studyGroup.presentation.command.dto;

import com.jaeseok.groupStudy.studyGroup.domain.RecruitingPolicy;
import com.jaeseok.groupStudy.studyGroup.domain.vo.StudyGroupInfo;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record CreateStudyGroupRequest(
        @NotBlank(message = "스터디 제목은 비워둘 수 없습니다.")
        @Size(max = 20, message = "스터디 제목은 20자를 초과할 수 없습니다.")
        String title,

        @NotNull(message = "최대 인원은 필수입니다.")
        @Min(value = 2, message = "최대 인원은 최소 2명 이상이어야 합니다.")
        Integer capacity,

        @NotNull(message = "마감일은 필수입니다.")
        @Future(message = "마감일은 현재 시간 이후여야 합니다.")
        LocalDateTime deadline,

        @NotNull(message = "모집 정책은 필수입니다.")
        RecruitingPolicy policy
) {

}
