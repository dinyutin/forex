package com.example.forex.dto;
//接收前端傳進來的查詢條件
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
//DTO（Data Transfer Object）用來在不同系統、不同層之間傳遞資料