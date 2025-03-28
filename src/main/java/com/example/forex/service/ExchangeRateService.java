package com.example.forex.service;

import com.example.forex.model.ExchangeRate;
import com.example.forex.repository.ExchangeRateRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ExchangeRateService {

    private final ExchangeRateRepository repository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ExchangeRateService(ExchangeRateRepository repository) {
        this.repository = repository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public void fetchAndSaveExchangeRates() {
        // 呼叫臺交所外匯 API
        String apiUrl = "https://openapi.taifex.com.tw/v1/DailyForeignExchangeRates";
        String response = restTemplate.getForObject(apiUrl, String.class);

        try {
            // 假設 API 回傳 JSON 陣列格式，每個物件包含 "Date" 與 "USD/NTD"
            JsonNode root = objectMapper.readTree(response);
            if (root.isArray()) {
                for (JsonNode node : root) {
                    // 取得日期字串，格式預設為 "yyyyMMdd"
                    String dateStr = node.get("Date").asText();
                    LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));

                    // 取得美元對台幣匯率（確認欄位名稱是否為 "USD/NTD"）
                    if (node.has("USD/NTD")) {
                        String rateStr = node.get("USD/NTD").asText();
                        BigDecimal rate = new BigDecimal(rateStr);

                        // 檢查資料庫中是否已存在相同日期的資料
                        Optional<ExchangeRate> existing = repository.findByDate(date);
                        if (existing.isEmpty()) {
                            ExchangeRate exchangeRate = new ExchangeRate(date, rate);
                            repository.save(exchangeRate);
                        }
                    }
                }
            } else {
                throw new RuntimeException("API 回傳格式不是 JSON 陣列");
            }
        } catch (Exception e) {
            throw new RuntimeException("解析 JSON 失敗: " + e.getMessage(), e);
        }
    }


    public List<ExchangeRate> queryExchangeRates(LocalDate startDate, LocalDate endDate) {
        return repository.findByDateGreaterThanEqualAndDateLessThanEqual(startDate, endDate);
    }
}
