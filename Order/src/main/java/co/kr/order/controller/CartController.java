package co.kr.order.controller;

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
    public ResponseEntity<List<CartDetails>> getList() {

        List<CartDetails> list = cartService.getCartList();

        return ResponseEntity.ok(list);
    }

    @PostMapping
    public ResponseEntity<CartDetails> add(@RequestBody) {

        CartDetails details = cartService.add();

        return ResponseEntity.ok(details);
    }
}
