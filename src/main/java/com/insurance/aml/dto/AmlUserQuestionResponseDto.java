package com.insurance.aml.dto;

import com.insurance.aml.entity.AmlQuestionResponse;
import com.insurance.aml.entity.Customer;
import com.insurance.aml.entity.Tenant;
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

    private Long customer;

    private List<QuestionAnswerDto> answers = new ArrayList<>();

}
