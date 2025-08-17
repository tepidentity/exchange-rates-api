package com.zetta.exchangerates.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@NoArgsConstructor
@Getter
@Setter
@Entity
public class Conversion extends BaseAuditableEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "conversion_seq_gen")
    @SequenceGenerator(name = "conversion_seq_gen", sequenceName = "conversion_seq", allocationSize = 1)
    @Column(updatable = false, nullable = false)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Currency sourceCurrency;
    @NotNull
    @Enumerated(EnumType.STRING)
    private Currency targetCurrency;
    @NotNull
    private Double sourceAmount;
    @NotNull
    private Double targetAmount;

    @PrePersist
    protected void onCreate() {
        setDate(ZonedDateTime.now());
        setTransactionId(java.util.UUID.randomUUID());
    }
}
