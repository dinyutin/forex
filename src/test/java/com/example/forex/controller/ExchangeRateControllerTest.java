package com.example.forex.controller;

import com.example.forex.model.ExchangeRate;
import com.example.forex.service.ExchangeRateService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExchangeRateController.class)
public class ExchangeRateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExchangeRateService exchangeRateService;

    // 定義請求日期字串的格式
    private static final DateTimeFormatter REQUEST_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");


    /**
     * 測試錯誤情形一：
     * 起始日期大於結束日期時，應回傳錯誤碼 E001 與 "日期區間不符" 訊息。
     */
    @Test
    public void testQueryExchangeRates_Error_StartDateAfterEndDate() throws Exception {
        String requestJson = "{\n" +
                "    \"startDate\": \"2025/03/27\",\n" +
                "    \"endDate\": \"2025/03/26\",\n" +
                "    \"currency\": \"usd\"\n" +
                "}";

        mockMvc.perform(post("/api/exchangeRates/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code", is("E001")))
                .andExpect(jsonPath("$.error.message", is("日期區間不符")))
                .andExpect(jsonPath("$.currency").doesNotExist());
    }

    /**
     * 測試錯誤情形二：
     * 查詢日期區間超出允許範圍（僅限於 1 年前 ~ 當下日期-1天），應回傳錯誤碼 E002 與相應訊息。
     */
    @Test
    public void testQueryExchangeRates_Error_OutOfAllowedRange() throws Exception {
        LocalDate today = LocalDate.now();
        // 允許的最小日期為 today.minusYears(1)
        // 允許的最大日期為 today.minusDays(1)
        LocalDate minAllowed = today.minusYears(1);
        LocalDate maxAllowed = today.minusDays(1);

        // 測試出界情況：起始日期早於允許範圍 (例如：minAllowed.minusDays(1))
        LocalDate invalidStartDate = minAllowed.minusDays(1);
        // 結束日期設定為允許的最大日期
        LocalDate validEndDate = maxAllowed;

        String requestJson = "{\n" +
                "    \"startDate\": \"" + invalidStartDate.format(REQUEST_FORMATTER) + "\",\n" +
                "    \"endDate\": \"" + validEndDate.format(REQUEST_FORMATTER) + "\",\n" +
                "    \"currency\": \"usd\"\n" +
                "}";

        mockMvc.perform(post("/api/exchangeRates/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code", is("E002")))
                .andExpect(jsonPath("$.error.message", is("日期區間僅限1年前~當下日期-1天")))
                .andExpect(jsonPath("$.currency").doesNotExist());
    }
    /**
     * 測試正確查詢：
     * 請求日期區間符合規範，且模擬 Service 回傳兩筆資料，
     * 驗證回傳 JSON 結構與資料內容是否正確（包含四捨五入效果）。
     */
    @Test
    public void testQueryExchangeRates_Success() throws Exception {
        // 建立模擬資料
        ExchangeRate rate1 = new ExchangeRate(LocalDate.of(2025, 3, 24), new BigDecimal("31.01"));
        ExchangeRate rate2 = new ExchangeRate(LocalDate.of(2025, 3, 25), new BigDecimal("31.016"));
        // 在 Controller 中對匯率做 setScale(2, RoundingMode.HALF_UP)
        // 因此 rate2 會四捨五入為 "31.02"
        List<ExchangeRate> rates = Arrays.asList(rate1, rate2);
        Mockito.when(exchangeRateService.queryExchangeRates(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(rates);

        // 模擬請求 JSON (日期格式：yyyy/MM/dd)
        String requestJson = "{\n" +
                "    \"startDate\": \"2025/03/24\",\n" +
                "    \"endDate\": \"2025/03/26\",\n" +
                "    \"currency\": \"usd\"\n" +
                "}";

        // 發送 POST 請求並驗證結果
        mockMvc.perform(post("/api/exchangeRates/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                // 驗證成功訊息
                .andExpect(jsonPath("$.error.code", is("0000")))
                .andExpect(jsonPath("$.error.message", is("成功")))
                // 驗證回傳資料筆數
                .andExpect(jsonPath("$.currency", hasSize(2)))
                // 驗證第一筆資料
                .andExpect(jsonPath("$.currency[0].date", is("20250324")))
                .andExpect(jsonPath("$.currency[0].usd", is("31.01")))
                // 驗證第二筆資料 (31.016 轉為 "31.02")
                .andExpect(jsonPath("$.currency[1].date", is("20250325")))
                .andExpect(jsonPath("$.currency[1].usd", is("31.02")));

        // 驗證 service 方法被呼叫一次
        Mockito.verify(exchangeRateService, times(1))
                .queryExchangeRates(any(LocalDate.class), any(LocalDate.class));
    }
}
