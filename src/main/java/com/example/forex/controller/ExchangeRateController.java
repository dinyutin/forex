package com.example.forex.controller;

import com.example.forex.dto.ExchangeRateQueryRequest;
import com.example.forex.dto.ExchangeRateQueryResponse;
import com.example.forex.dto.ExchangeRateQueryResponse.CurrencyInfo;
import com.example.forex.dto.ExchangeRateQueryResponse.ErrorInfo;
import com.example.forex.model.ExchangeRate;
import com.example.forex.service.ExchangeRateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/exchangeRates")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping ("/fetch")
    public ResponseEntity<String> fetchExchangeRates() {
        exchangeRateService.fetchAndSaveExchangeRates();
        return ResponseEntity.ok("美元對台幣資料已成功抓取並儲存！");
    }
    @PostMapping("/query")
    public ResponseEntity<ExchangeRateQueryResponse> queryExchangeRates(@RequestBody ExchangeRateQueryRequest request) {
        // 將請求日期字串 (格式：yyyy/MM/dd) 轉換為 LocalDate
        DateTimeFormatter requestFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDate startDate = LocalDate.parse(request.getStartDate(), requestFormatter);
        LocalDate endDate = LocalDate.parse(request.getEndDate(), requestFormatter);

        // 驗證日期區間：起始日期不得大於結束日期
        if (startDate.isAfter(endDate)) {
            ExchangeRateQueryResponse errorResponse = new ExchangeRateQueryResponse();
            ErrorInfo errorInfo = new ErrorInfo();
            errorInfo.setCode("E001");
            errorInfo.setMessage("日期區間不符");
            errorResponse.setError(errorInfo);
            errorResponse.setCurrency(null);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        // 新增驗證：查詢區間僅限於 1 年前 ~ 當下日期 - 1 天
        LocalDate today = LocalDate.now();
        LocalDate minAllowed = today.minusYears(1);     // 1 年前的日期
        LocalDate maxAllowed = today.minusDays(1);        // 當下日期減 1 天

        if (startDate.isBefore(minAllowed) || endDate.isAfter(maxAllowed)) {
            ExchangeRateQueryResponse errorResponse = new ExchangeRateQueryResponse();
            ExchangeRateQueryResponse.ErrorInfo errorInfo = new ExchangeRateQueryResponse.ErrorInfo();
            errorInfo.setCode("E002");
            errorInfo.setMessage("日期區間僅限1年前~當下日期-1天");
            errorResponse.setError(errorInfo);
            errorResponse.setCurrency(null);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        List<ExchangeRate> rates = exchangeRateService.queryExchangeRates(startDate, endDate);

        // 將查詢結果轉換為回傳格式，日期格式轉為 yyyyMMdd，幣值取兩位小數
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        List<CurrencyInfo> currencyInfoList = rates.stream().map(rate -> {
            CurrencyInfo info = new CurrencyInfo();
            info.setDate(rate.getDate().format(outputFormatter));
            info.setUsd(rate.getUsd().setScale(2, RoundingMode.HALF_UP).toString());
            return info;
        }).collect(Collectors.toList());

        ExchangeRateQueryResponse response = new ExchangeRateQueryResponse();
        ErrorInfo successError = new ErrorInfo();
        successError.setCode("0000");
        successError.setMessage("成功");
        response.setError(successError);
        response.setCurrency(currencyInfoList);

        return ResponseEntity.ok(response);
    }
}
