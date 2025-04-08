package com.example.forex.service;

import com.example.forex.dto.ExchangeRateQueryRequest;
import com.example.forex.dto.ExchangeRateQueryResponse;
import com.example.forex.model.ExchangeRate;
import com.example.forex.repository.ExchangeRateRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public ExchangeRateQueryResponse queryExchangeRates(ExchangeRateQueryRequest request) {
        ExchangeRateQueryResponse response = new ExchangeRateQueryResponse();

        // 轉換日期字串 -> LocalDate
        DateTimeFormatter requestFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDate startDate;
        LocalDate endDate;
        try {
            startDate = LocalDate.parse(request.getStartDate(), requestFormatter);
            endDate = LocalDate.parse(request.getEndDate(), requestFormatter);
        } catch (Exception e) {
            ExchangeRateQueryResponse.ErrorInfo errorInfo = new ExchangeRateQueryResponse.ErrorInfo();
            errorInfo.setCode("E003");
            errorInfo.setMessage("日期格式錯誤");
            response.setError(errorInfo);
            response.setCurrency(null);
            return response;
        }

        // 驗證起始日期不得大於結束日期
        if (startDate.isAfter(endDate)) {
            ExchangeRateQueryResponse.ErrorInfo errorInfo = new ExchangeRateQueryResponse.ErrorInfo();
            errorInfo.setCode("E001");
            errorInfo.setMessage("日期區間不符");
            response.setError(errorInfo);
            response.setCurrency(null);
            return response;
        }

        // 驗證只允許查1年前 ~ 昨天
        LocalDate today = LocalDate.now();
        LocalDate minAllowed = today.minusYears(1);
        LocalDate maxAllowed = today.minusDays(1);

        if (startDate.isBefore(minAllowed) || endDate.isAfter(maxAllowed)) {
            ExchangeRateQueryResponse.ErrorInfo errorInfo = new ExchangeRateQueryResponse.ErrorInfo();
            errorInfo.setCode("E002");
            errorInfo.setMessage("日期區間僅限1年前~當下日期-1天");
            response.setError(errorInfo);
            response.setCurrency(null);
            return response;
        }

        // 查詢資料
        List<ExchangeRate> rates = repository.findByDateGreaterThanEqualAndDateLessThanEqual(startDate, endDate);

        // 組 CurrencyInfo
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        List<ExchangeRateQueryResponse.CurrencyInfo> currencyInfoList = rates.stream().map(rate -> {
            ExchangeRateQueryResponse.CurrencyInfo info = new ExchangeRateQueryResponse.CurrencyInfo();
            info.setDate(rate.getDate().format(outputFormatter));
            info.setUsd(rate.getUsd().setScale(2, RoundingMode.HALF_UP).toString());
            return info;
        }).collect(Collectors.toList());

        // 成功回傳
        ExchangeRateQueryResponse.ErrorInfo successError = new ExchangeRateQueryResponse.ErrorInfo();
        successError.setCode("0000");
        successError.setMessage("成功");
        response.setError(successError);
        response.setCurrency(currencyInfoList);

        return response;
    }

    public List<ExchangeRate> queryExchangeRates(LocalDate startDate, LocalDate endDate) {
        return repository.findByDateGreaterThanEqualAndDateLessThanEqual(startDate, endDate);
    }
}
