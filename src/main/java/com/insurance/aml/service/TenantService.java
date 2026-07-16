package com.insurance.aml.service;

import com.insurance.aml.dto.TenantDto;
import com.insurance.aml.entity.Tenant;
import com.insurance.aml.exception.DuplicateResourceException;
import com.insurance.aml.exception.ResourceNotFoundException;
import com.insurance.aml.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TenantService {

    private final TenantRepository tenantRepository;

    public TenantDto createTenant(TenantDto dto) {
        if (tenantRepository.existsByTenantCode(dto.getTenantCode())) {
            throw DuplicateResourceException.forField("Tenant", "tenantCode", dto.getTenantCode());
        }

        Tenant tenant = Tenant.builder()
                .tenantCode(dto.getTenantCode())
                .tenantName(dto.getTenantName())
                .build();

        return toDto(tenantRepository.save(tenant));
    }

    @Transactional(readOnly = true)
    public TenantDto getTenant(Long tenantId) {
        return toDto(findTenantOrThrow(tenantId));
    }

    @Transactional(readOnly = true)
    public List<TenantDto> getAllTenants() {
        return tenantRepository.findAll().stream().map(this::toDto).toList();
    }

    Tenant findTenantOrThrow(Long tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Tenant", tenantId));
    }

    private TenantDto toDto(Tenant tenant) {
        return TenantDto.builder()
                .tenantId(tenant.getTenantId())
                .tenantCode(tenant.getTenantCode())
                .tenantName(tenant.getTenantName())
                .active(tenant.isActive())
                .build();
    }
}
