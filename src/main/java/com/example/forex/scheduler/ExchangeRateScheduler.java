package com.example.forex.scheduler;

import com.example.forex.service.ExchangeRateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ExchangeRateScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateScheduler.class);
    private final ExchangeRateService exchangeRateService;

    public ExchangeRateScheduler(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    // 每日 18:00:00 執行
    @Scheduled(cron = "0 0 18 * * ?")
    public void fetchExchangeRatesScheduled() {
        logger.info("開始每日 18:00 呼叫 fetchExchangeRates");
        exchangeRateService.fetchAndSaveExchangeRates();
        logger.info("完成每日 18:00 呼叫 fetchExchangeRates");
    }
}
