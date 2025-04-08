package com.example.forex.dto;

import java.util.List;
//查完資料以後，組成一個格式，回傳給前端
public class ExchangeRateQueryResponse {

    private ErrorInfo error;
    private List<CurrencyInfo> currency;

    public ErrorInfo getError() {
        return error;
    }
    public void setError(ErrorInfo error) {
        this.error = error;
    }
    public List<CurrencyInfo> getCurrency() {
        return currency;
    }
    public void setCurrency(List<CurrencyInfo> currency) {
        this.currency = currency;
    }

    // 回傳結果中的錯誤資訊
    public static class ErrorInfo {
        private String code;
        private String message;

        public String getCode() {
            return code;
        }
        public void setCode(String code) {
            this.code = code;
        }
        public String getMessage() {
            return message;
        }
        public void setMessage(String message) {
            this.message = message;
        }
    }

    // 回傳結果中的幣別資料
    public static class CurrencyInfo {
        private String date; // 格式：yyyyMMdd
        private String usd;  // 例如 "31.01"

        public String getDate() {
            return date;
        }
        public void setDate(String date) {
            this.date = date;
        }
        public String getUsd() {
            return usd;
        }
        public void setUsd(String usd) {
            this.usd = usd;
        }
    }
}
//DTO（Data Transfer Object）用來在不同系統、不同層之間傳遞資料