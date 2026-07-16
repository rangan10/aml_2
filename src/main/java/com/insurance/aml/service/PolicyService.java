package com.insurance.aml.service;

import com.insurance.aml.dto.PolicyDto;
import com.insurance.aml.entity.Customer;
import com.insurance.aml.entity.Policy;
import com.insurance.aml.exception.ResourceNotFoundException;
import com.insurance.aml.repository.CustomerRepository;
import com.insurance.aml.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final CustomerRepository customerRepository;

    public PolicyDto createPolicy(PolicyDto dto) {
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Customer", dto.getCustomerId()));

        Policy policy = Policy.builder()
                .policyNumber(dto.getPolicyNumber())
                .customer(customer)
                .productType(dto.getProductType())
                .proposerName(dto.getProposerName())
                .sumInsured(dto.getSumInsured())
                .annualPremium(dto.getAnnualPremium())
                .assetHypothecated(dto.isAssetHypothecated())
                .policyStartDate(dto.getPolicyStartDate())
                .build();

        return toDto(policyRepository.save(policy));
    }

    @Transactional(readOnly = true)
    public PolicyDto getPolicy(Long policyId) {
        return toDto(findPolicyOrThrow(policyId));
    }

    @Transactional(readOnly = true)
    public List<PolicyDto> getPoliciesForCustomer(Long customerId) {
        return policyRepository.findByCustomer_CustomerId(customerId).stream().map(this::toDto).toList();
    }

    private Policy findPolicyOrThrow(Long policyId) {
        return policyRepository.findById(policyId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Policy", policyId));
    }

    private PolicyDto toDto(Policy policy) {
        return PolicyDto.builder()
                .policyId(policy.getPolicyId())
                .policyNumber(policy.getPolicyNumber())
                .customerId(policy.getCustomer().getCustomerId())
                .productType(policy.getProductType())
                .proposerName(policy.getProposerName())
                .sumInsured(policy.getSumInsured())
                .annualPremium(policy.getAnnualPremium())
                .assetHypothecated(policy.isAssetHypothecated())
                .policyStartDate(policy.getPolicyStartDate())
                .build();
    }
}
