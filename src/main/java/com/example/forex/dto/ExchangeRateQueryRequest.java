package com.example.forex.dto;

public class ExchangeRateQueryRequest {
    private String startDate; // 格式：yyyy/MM/dd，例如 "2025/03/24"
    private String endDate;   // 格式：yyyy/MM/dd，例如 "2025/03/26"
    private String currency;  // 例如 "usd"

    public String getStartDate() {
        return startDate;
    }
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    public String getEndDate() {
        return endDate;
    }
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    public String getCurrency() {
        return currency;
    }
    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
