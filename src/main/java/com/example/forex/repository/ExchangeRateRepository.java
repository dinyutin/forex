package com.example.forex.repository;

import com.example.forex.model.ExchangeRate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends MongoRepository<ExchangeRate, String> {
    Optional<ExchangeRate> findByDate(LocalDate date);
    @Query("{ 'date' : { $gte: ?0, $lte: ?1 } }")
    List<ExchangeRate> findByDateGreaterThanEqualAndDateLessThanEqual(LocalDate startDate, LocalDate endDate);
}
