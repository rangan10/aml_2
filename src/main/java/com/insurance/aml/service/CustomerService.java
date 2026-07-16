package com.insurance.aml.service;

import com.insurance.aml.dto.CustomerDto;
import com.insurance.aml.entity.Customer;
import com.insurance.aml.exception.ResourceNotFoundException;
import com.insurance.aml.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final RiskCategorizationService riskCategorizationService;

    public CustomerDto createCustomer(CustomerDto dto) {
        Customer customer = toEntity(dto);
        riskCategorizationService.applyRiskCategory(customer);
        Customer saved = customerRepository.save(customer);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public CustomerDto getCustomer(Long customerId) {
        return toDto(findCustomerOrThrow(customerId));
    }

    @Transactional(readOnly = true)
    public List<CustomerDto> getAllCustomers() {
        return customerRepository.findAll().stream().map(this::toDto).toList();
    }

    public CustomerDto updateCustomer(Long customerId, CustomerDto dto) {
        Customer existing = findCustomerOrThrow(customerId);

        existing.setFullName(dto.getFullName());
        existing.setPanNumber(dto.getPanNumber());
        existing.setMobileNumber(dto.getMobileNumber());
        existing.setEmail(dto.getEmail());
        existing.setAddressCurrent(dto.getAddressCurrent());
        existing.setAddressPermanent(dto.getAddressPermanent());
        existing.setAnnualIncome(dto.getAnnualIncome());
        existing.setOccupationType(dto.getOccupationType());
        existing.setPep(dto.isPep());
        existing.setNri(dto.isNri());

        riskCategorizationService.applyRiskCategory(existing);

        return toDto(customerRepository.save(existing));
    }

    private Customer findCustomerOrThrow(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Customer", customerId));
    }

    private Customer toEntity(CustomerDto dto) {
        return Customer.builder()
                .customerCode(dto.getCustomerCode())
                .fullName(dto.getFullName())
                .panNumber(dto.getPanNumber())
                .mobileNumber(dto.getMobileNumber())
                .email(dto.getEmail())
                .addressCurrent(dto.getAddressCurrent())
                .addressPermanent(dto.getAddressPermanent())
                .annualIncome(dto.getAnnualIncome())
                .occupationType(dto.getOccupationType())
                .pep(dto.isPep())
                .nri(dto.isNri())
                .build();
    }

    private CustomerDto toDto(Customer customer) {
        return CustomerDto.builder()
                .customerId(customer.getCustomerId())
                .customerCode(customer.getCustomerCode())
                .fullName(customer.getFullName())
                .panNumber(customer.getPanNumber())
                .mobileNumber(customer.getMobileNumber())
                .email(customer.getEmail())
                .addressCurrent(customer.getAddressCurrent())
                .addressPermanent(customer.getAddressPermanent())
                .annualIncome(customer.getAnnualIncome())
                .occupationType(customer.getOccupationType())
                .pep(customer.isPep())
                .nri(customer.isNri())
                .riskCategory(customer.getRiskCategory())
                .build();
    }
}
