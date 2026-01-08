package co.kr.order.controller;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    @DisplayName("카트 리스트 조회 테스트")
    void 카트_리스트_조회_테스트() throws Exception {

        mvc.perform(get("/carts"))
                .andExpect(status().isOk());



    }
}


//mockMvc.perform(get("/api/orders/{id}", orderId))
//        .andExpect(status().isOk()) // HTTP 200 확인
//        .andExpect(jsonPath("$.orderIdx").value(orderId)) // JSON 필드값 검증
//        .andExpect(jsonPath("$.orderCode").value("ORD-20260108-001"))
//        .andExpect(jsonPath("$.totalAmount").value(15000.00))
//        .andDo(print()); // 콘솔에 요청/응답 로그 출력