package com.insurance.aml.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmlUserQuestionResponseDto {

    private Long tenant;

    private Long userProfileId;

    private List<QuestionAnswerDto> answers = new ArrayList<>();

}
