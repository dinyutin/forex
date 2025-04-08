package com.example.forex.controller;

import com.example.forex.dto.ExchangeRateQueryRequest;
import com.example.forex.dto.ExchangeRateQueryResponse;
import com.example.forex.dto.ExchangeRateQueryResponse.CurrencyInfo;
import com.example.forex.dto.ExchangeRateQueryResponse.ErrorInfo;
import com.example.forex.service.ExchangeRateService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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

        // 模擬 service 回傳錯誤
        ExchangeRateQueryResponse mockErrorResponse = new ExchangeRateQueryResponse();
        ErrorInfo errorInfo = new ErrorInfo();
        errorInfo.setCode("E001");
        errorInfo.setMessage("日期區間不符");
        mockErrorResponse.setError(errorInfo);
        mockErrorResponse.setCurrency(null);

        Mockito.when(exchangeRateService.queryExchangeRates(any(ExchangeRateQueryRequest.class)))
                .thenReturn(mockErrorResponse);

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
     * 查詢日期區間超出允許範圍（僅限於 1 年前 ~ 當下日期-1天），應回傳錯誤碼 E002。
     */
    @Test
    public void testQueryExchangeRates_Error_OutOfAllowedRange() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate minAllowed = today.minusYears(1);
        LocalDate invalidStartDate = minAllowed.minusDays(1);
        LocalDate validEndDate = today.minusDays(1);

        String requestJson = "{\n" +
                "    \"startDate\": \"" + invalidStartDate.format(REQUEST_FORMATTER) + "\",\n" +
                "    \"endDate\": \"" + validEndDate.format(REQUEST_FORMATTER) + "\",\n" +
                "    \"currency\": \"usd\"\n" +
                "}";

        // 模擬 service 回傳錯誤
        ExchangeRateQueryResponse mockErrorResponse = new ExchangeRateQueryResponse();
        ErrorInfo errorInfo = new ErrorInfo();
        errorInfo.setCode("E002");
        errorInfo.setMessage("日期區間僅限1年前~當下日期-1天");
        mockErrorResponse.setError(errorInfo);
        mockErrorResponse.setCurrency(null);

        Mockito.when(exchangeRateService.queryExchangeRates(any(ExchangeRateQueryRequest.class)))
                .thenReturn(mockErrorResponse);

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
     * 正確回傳兩筆資料。
     */
    @Test
    public void testQueryExchangeRates_Success() throws Exception {
        // 模擬回傳的資料
        ExchangeRateQueryResponse mockResponse = new ExchangeRateQueryResponse();
        ErrorInfo successError = new ErrorInfo();
        successError.setCode("0000");
        successError.setMessage("成功");
        mockResponse.setError(successError);

        List<CurrencyInfo> currencyList = Arrays.asList(
                createCurrencyInfo("20250324", "31.01"),
                createCurrencyInfo("20250325", "31.02")
        );
        mockResponse.setCurrency(currencyList);

        Mockito.when(exchangeRateService.queryExchangeRates(any(ExchangeRateQueryRequest.class)))
                .thenReturn(mockResponse);

        String requestJson = "{\n" +
                "    \"startDate\": \"2025/03/24\",\n" +
                "    \"endDate\": \"2025/03/26\",\n" +
                "    \"currency\": \"usd\"\n" +
                "}";

        mockMvc.perform(post("/api/exchangeRates/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code", is("0000")))
                .andExpect(jsonPath("$.error.message", is("成功")))
                .andExpect(jsonPath("$.currency", hasSize(2)))
                .andExpect(jsonPath("$.currency[0].date", is("20250324")))
                .andExpect(jsonPath("$.currency[0].usd", is("31.01")))
                .andExpect(jsonPath("$.currency[1].date", is("20250325")))
                .andExpect(jsonPath("$.currency[1].usd", is("31.02")));

        Mockito.verify(exchangeRateService, times(1))
                .queryExchangeRates(any(ExchangeRateQueryRequest.class));
    }

    // 建立CurrencyInfo物件
    private CurrencyInfo createCurrencyInfo(String date, String usd) {
        CurrencyInfo info = new CurrencyInfo();
        info.setDate(date);
        info.setUsd(usd);
        return info;
    }
    @Test
    public void testFetchExchangeRates_Success() throws Exception {
        // 因為 fetchAndSaveExchangeRates() 是 void，所以這邊不需要特別 mock 回傳值
        Mockito.doNothing().when(exchangeRateService).fetchAndSaveExchangeRates();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/exchangeRates/fetch"))
                .andExpect(status().isOk())
                .andExpect(content().string("美元對台幣資料已成功抓取並儲存！"));

        // 驗證 service 方法確實有被呼叫一次
        Mockito.verify(exchangeRateService, times(1)).fetchAndSaveExchangeRates();
    }

}
