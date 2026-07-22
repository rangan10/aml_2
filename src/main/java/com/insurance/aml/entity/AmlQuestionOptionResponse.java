package com.insurance.aml.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "aml_question_option_response")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmlQuestionOptionResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_response_id")
    private Long optionResponseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_id")
    private AmlQuestionResponse response;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    private AmlQuestionOption option;
}