package com.example.forex.scheduler;

import com.example.forex.service.ExchangeRateService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.*;

@SpringBootTest
public class ExchangeRateSchedulerTest {

    @Autowired
    private ExchangeRateScheduler scheduler;

    // 將 ExchangeRateService 改成 Mock Bean 以驗證方法呼叫
    @MockBean
    private ExchangeRateService exchangeRateService;

    @Test
    public void testFetchExchangeRatesScheduled() {
        // 執行批次方法
        scheduler.fetchExchangeRatesScheduled();

        // 驗證 ExchangeRateService.fetchAndSaveExchangeRates() 是否被呼叫一次
        Mockito.verify(exchangeRateService, Mockito.times(1)).fetchAndSaveExchangeRates();
    }
}
