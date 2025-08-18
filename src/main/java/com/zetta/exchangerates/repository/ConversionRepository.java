package com.zetta.exchangerates.repository;

import com.zetta.exchangerates.entity.Conversion;
import feign.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversionRepository extends JpaRepository<Conversion, Long> {

    Optional<Conversion> getOneByTransactionId(UUID transactionId);

    @Query("FROM Conversion c WHERE cast(c.date as Date) = :date AND c.transactionId = :transactionId")
    Optional<Conversion> getOneByDateAndTransactionId(LocalDate date, UUID transactionId);

    @Query("FROM Conversion c WHERE cast(c.date as Date) = :date")
    List<Conversion> findAllByDate(@Param("date") LocalDate date, Pageable pageable);
}
