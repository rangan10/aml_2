package com.insurance.aml.service;

import com.insurance.aml.dto.AlertActionRequest;
import com.insurance.aml.dto.AmlAlertDto;
import com.insurance.aml.entity.*;
import com.insurance.aml.exception.InvalidAlertTransitionException;
import com.insurance.aml.repository.AmlAlertActionRepository;
import com.insurance.aml.repository.AmlAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Enforces the AML alert workflow:
 * <pre>
 *   SYSTEM -> OPERATIONS_TEAM -> OPERATIONS_HOD -> COMPLIANCE_OFFICER -> FIU_REPORTED / CLOSED
 * </pre>
 * Any stage (except CLOSED / FIU_REPORTED) may also close the alert directly
 * if it is determined to be a false positive.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AlertWorkflowService {

    private final AmlAlertRepository amlAlertRepository;
    private final AmlAlertActionRepository amlAlertActionRepository;
    private final AmlAlertService amlAlertService;

    /** Valid actions per current stage. */
    private static final Map<AlertStage, Set<AlertAction>> ALLOWED_ACTIONS = new EnumMap<>(AlertStage.class);
    /** Stage each action moves the alert to, keyed by (fromStage, action). */
    private static final Map<AlertStage, Map<AlertAction, AlertStage>> NEXT_STAGE = new EnumMap<>(AlertStage.class);

    static {
        ALLOWED_ACTIONS.put(AlertStage.SYSTEM, EnumSet.of(AlertAction.FORWARD, AlertAction.CLOSE));
        ALLOWED_ACTIONS.put(AlertStage.OPERATIONS_TEAM, EnumSet.of(AlertAction.FORWARD, AlertAction.CLOSE));
        ALLOWED_ACTIONS.put(AlertStage.OPERATIONS_HOD,
                EnumSet.of(AlertAction.FORWARD, AlertAction.ESCALATE, AlertAction.CLOSE));
        ALLOWED_ACTIONS.put(AlertStage.COMPLIANCE_OFFICER,
                EnumSet.of(AlertAction.REPORT_TO_FIU, AlertAction.CLOSE, AlertAction.REQUEST_INFO));
        ALLOWED_ACTIONS.put(AlertStage.FIU_REPORTED, EnumSet.of(AlertAction.CLOSE));
        ALLOWED_ACTIONS.put(AlertStage.CLOSED, EnumSet.noneOf(AlertAction.class));

        Map<AlertAction, AlertStage> fromSystem = new EnumMap<>(AlertAction.class);
        fromSystem.put(AlertAction.FORWARD, AlertStage.OPERATIONS_TEAM);
        fromSystem.put(AlertAction.CLOSE, AlertStage.CLOSED);
        NEXT_STAGE.put(AlertStage.SYSTEM, fromSystem);

        Map<AlertAction, AlertStage> fromOpsTeam = new EnumMap<>(AlertAction.class);
        fromOpsTeam.put(AlertAction.FORWARD, AlertStage.OPERATIONS_HOD);
        fromOpsTeam.put(AlertAction.CLOSE, AlertStage.CLOSED);
        NEXT_STAGE.put(AlertStage.OPERATIONS_TEAM, fromOpsTeam);

        Map<AlertAction, AlertStage> fromHod = new EnumMap<>(AlertAction.class);
        fromHod.put(AlertAction.FORWARD, AlertStage.COMPLIANCE_OFFICER);
        fromHod.put(AlertAction.ESCALATE, AlertStage.COMPLIANCE_OFFICER);
        fromHod.put(AlertAction.CLOSE, AlertStage.CLOSED);
        NEXT_STAGE.put(AlertStage.OPERATIONS_HOD, fromHod);

        Map<AlertAction, AlertStage> fromCompliance = new EnumMap<>(AlertAction.class);
        fromCompliance.put(AlertAction.REPORT_TO_FIU, AlertStage.FIU_REPORTED);
        fromCompliance.put(AlertAction.CLOSE, AlertStage.CLOSED);
        fromCompliance.put(AlertAction.REQUEST_INFO, AlertStage.OPERATIONS_TEAM);
        NEXT_STAGE.put(AlertStage.COMPLIANCE_OFFICER, fromCompliance);

        Map<AlertAction, AlertStage> fromFiu = new EnumMap<>(AlertAction.class);
        fromFiu.put(AlertAction.CLOSE, AlertStage.CLOSED);
        NEXT_STAGE.put(AlertStage.FIU_REPORTED, fromFiu);
    }

    /**
     * Applies an action to an alert, validating that the action is legal from
     * the alert's current stage, then advances the stage and records an audit entry.
     */
    public AmlAlertDto applyAction(Long alertId, AlertActionRequest request) {
        AmlAlert alert = amlAlertService.findAlertOrThrow(alertId);
        AlertStage currentStage = alert.getCurrentStage();

        Set<AlertAction> allowed = ALLOWED_ACTIONS.getOrDefault(currentStage, Set.of());
        if (!allowed.contains(request.getAction())) {
            throw new InvalidAlertTransitionException(
                    "Action " + request.getAction() + " is not valid from stage " + currentStage);
        }

        AlertStage nextStage = NEXT_STAGE.get(currentStage).get(request.getAction());

        AmlAlertAction actionRecord = AmlAlertAction.builder()
                .alert(alert)
                .stage(currentStage)
                .actionBy(request.getActionBy())
                .action(request.getAction())
                .remarks(request.getRemarks())
                .build();
        amlAlertActionRepository.save(actionRecord);

        alert.setCurrentStage(nextStage);
        alert.setStatus(deriveStatus(nextStage));
        AmlAlert saved = amlAlertRepository.save(alert);

        log.info("Alert {} moved from {} to {} via action {} by {}",
                alertId, currentStage, nextStage, request.getAction(), request.getActionBy());

        return amlAlertService.toDto(saved);
    }

    private AlertStatus deriveStatus(AlertStage stage) {
        return switch (stage) {
            case SYSTEM, OPERATIONS_TEAM -> AlertStatus.OPEN;
            case OPERATIONS_HOD, COMPLIANCE_OFFICER -> AlertStatus.IN_PROGRESS;
            case FIU_REPORTED -> AlertStatus.REPORTED_TO_FIU;
            case CLOSED -> AlertStatus.CLOSED;
        };
    }
}
