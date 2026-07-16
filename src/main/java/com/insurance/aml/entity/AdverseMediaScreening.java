package com.insurance.aml.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "adverse_media_screening")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdverseMediaScreening {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "screening_id")
    private Long screeningId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "match_found", nullable = false)
    private boolean matchFound;

    @Column(name = "source", length = 100)
    private String source;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "screening_date", nullable = false)
    private LocalDateTime screeningDate;

    @PrePersist
    protected void onCreate() {
        if (this.screeningDate == null) {
            this.screeningDate = LocalDateTime.now();
        }
    }
}
