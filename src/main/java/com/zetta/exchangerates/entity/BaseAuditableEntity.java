package com.zetta.exchangerates.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseAuditableEntity<T> {

    public abstract T getId();
    public abstract void setId(T id);

    @NotNull
    @Column(updatable = false, nullable = false)
    private UUID transactionId;
    @NotNull
    @Column(updatable = false, nullable = false)
    private ZonedDateTime date;

    @PrePersist
    protected void onCreate() {
        setDate(ZonedDateTime.now());
        setTransactionId(java.util.UUID.randomUUID());
    }
}
