package com.insurance.aml.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionAnswerDto {

    private String questionCode;

    private String questionText;

    private String answerText;

    private List<String> selectedOptionCodes;
}
