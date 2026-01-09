package co.kr.order.controller;

import co.kr.order.model.dto.BaseResponse;
import co.kr.order.model.dto.CartDetails;
import co.kr.order.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<BaseResponse<List<CartDetails>>> getList() {

        List<CartDetails> list = cartService.getCartList();

        BaseResponse<List<CartDetails>> res = new BaseResponse<>("ok", list);

        return ResponseEntity.ok(res);
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable("id") Long id) {

        cartService.deleteCart(id);
        BaseResponse<Void> res = new BaseResponse<>("ok", null);

        return ResponseEntity.ok(res);
    }
}
