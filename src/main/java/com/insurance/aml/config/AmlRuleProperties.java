package com.insurance.aml.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Binds the aml.rules.* section of application.yml so that every
 * rule threshold is externally configurable without a code change.
 */
@Component
@ConfigurationProperties(prefix = "aml.rules")
@Getter
@Setter
public class AmlRuleProperties {

    private Aml003 aml003 = new Aml003();
    private Aml004 aml004 = new Aml004();
    private Aml005 aml005 = new Aml005();
    private Aml006 aml006 = new Aml006();
    private Aml007 aml007 = new Aml007();
    private Aml008 aml008 = new Aml008();
    private Aml009 aml009 = new Aml009();
    private Aml015 aml015 = new Aml015();
    private Aml016 aml016 = new Aml016();
    private Aml017 aml017 = new Aml017();
    private Aml018 aml018 = new Aml018();
    private Aml019 aml019 = new Aml019();

    @Getter
    @Setter
    public static class Aml003 {
        private int sumInsuredMultiplier = 10;
        private int premiumMultiplier = 5;
    }

    @Getter
    @Setter
    public static class Aml004 {
        private BigDecimal cashDdThreshold = BigDecimal.valueOf(50000);
        private int monthlyCountThreshold = 3;
        private BigDecimal monthlyAmountThreshold = BigDecimal.valueOf(500000);
        private int yearlyCountThreshold = 21;
        private BigDecimal yearlyAmountThreshold = BigDecimal.valueOf(5000000);
    }

    @Getter
    @Setter
    public static class Aml005 {
        private int cancellationCountThreshold = 3;
        private int windowDays = 30;
    }

    @Getter
    @Setter
    public static class Aml006 {
        private BigDecimal premiumThreshold = BigDecimal.valueOf(50000);
        private BigDecimal refundThreshold = BigDecimal.valueOf(50000);
    }

    @Getter
    @Setter
    public static class Aml007 {
        private BigDecimal sumInsuredThreshold = BigDecimal.valueOf(10000000);
        private BigDecimal premiumThreshold = BigDecimal.valueOf(100000);
    }

    @Getter
    @Setter
    public static class Aml008 {
        private int notificationChangeCount = 2;
        private int alertChangeCount = 3;
    }

    @Getter
    @Setter
    public static class Aml009 {
        private BigDecimal overpaymentRefundThreshold = BigDecimal.valueOf(10000);
    }

    @Getter
    @Setter
    public static class Aml015 {
        private BigDecimal claimThreshold = BigDecimal.valueOf(1000000);
    }

    @Getter
    @Setter
    public static class Aml016 {
        private BigDecimal claimThreshold = BigDecimal.valueOf(10000000);
    }

    @Getter
    @Setter
    public static class Aml017 {
        private int nomineeChangeThreshold = 2;
        private int assigneeChangeThreshold = 2;
    }

    @Getter
    @Setter
    public static class Aml018 {
        private BigDecimal premiumThreshold = BigDecimal.valueOf(200000);
    }

    @Getter
    @Setter
    public static class Aml019 {
        private BigDecimal premiumThreshold = BigDecimal.valueOf(500000);
    }
}
