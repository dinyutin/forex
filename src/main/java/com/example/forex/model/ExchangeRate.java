package com.example.forex.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

@Document(collection = "exchange_rate")
public class ExchangeRate {

    @Id
    private String id;
    private LocalDate date;       // 儲存日期
    private BigDecimal usd;  // 美元對台幣匯率

    public ExchangeRate() {
    }

    public ExchangeRate(LocalDate date, BigDecimal usd) {
        this.date = date;
        this.usd = usd;
    }

    // Getter & Setter
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getUsd() {
        return usd;
    }
    public void setUsd(BigDecimal usd) {
        this.usd = usd;
    }
}
