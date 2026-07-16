package com.insurance.aml.repository;

import com.insurance.aml.entity.AmlAlertAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AmlAlertActionRepository extends JpaRepository<AmlAlertAction, Long> {

    List<AmlAlertAction> findByAlert_AlertIdOrderByActionDateAsc(Long alertId);
}
