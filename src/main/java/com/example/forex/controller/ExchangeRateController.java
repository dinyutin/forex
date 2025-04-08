package com.example.forex.controller;

import com.example.forex.dto.ExchangeRateQueryRequest;
import com.example.forex.dto.ExchangeRateQueryResponse;
import com.example.forex.service.ExchangeRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/exchangeRates")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    @Autowired
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
        ExchangeRateQueryResponse response = exchangeRateService.queryExchangeRates(request);
        if (!"0000".equals(response.getError().getCode())) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
}
