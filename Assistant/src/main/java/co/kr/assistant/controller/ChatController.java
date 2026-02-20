package co.kr.assistant.controller;

import co.kr.assistant.model.dto.ChatListDTO;
import co.kr.assistant.service.ChatService;
import co.kr.assistant.util.BaseResponse;
import co.kr.assistant.util.CookieUtil;
import co.kr.assistant.util.TokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated // 메서드 파라미터 유효성 검증(@Valid 등)을 활성화
@RestController
@RequiredArgsConstructor
@RequestMapping("/Assistant") // 기본 경로 설정
public class ChatController {
    private final ChatService chatService;

    // 챗봇 시작 (세션 초기화 및 쿠키 발급)
    @PostMapping("/start")
    public ResponseEntity<BaseResponse<String>> start(HttpServletRequest request, HttpServletResponse response) {
        // 1. TokenUtil을 통해 쿠키에서 refreshToken 추출
        String refreshToken = TokenUtil.getCookieValue(request, "refreshToken");

        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        // 2. 세션 초기화 (회원/비회원 판별 포함)
        String chatToken = chatService.start(refreshToken, ip, userAgent);

        // 3. 채팅용 세션 토큰 쿠키 발급 (기존 CookieUtil 활용)
        CookieUtil.addCookie(
                response,
                CookieUtil.CHAT_TOKEN_NAME,
                chatToken,
                CookieUtil.CHAT_TOKEN_EXPIRY);

        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", "채팅을 시작합니다."));
    }

    @PostMapping("/list")
    public ResponseEntity<BaseResponse<List<ChatListDTO>>> list (@CookieValue(name = "chatToken", required = false) String chatToken,
                                                                 HttpServletResponse response) {


        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", chatService.list(chatToken)));

    }
}
