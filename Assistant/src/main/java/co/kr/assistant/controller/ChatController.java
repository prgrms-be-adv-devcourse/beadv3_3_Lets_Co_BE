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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated // 메서드 파라미터 유효성 검증(@Valid 등)을 활성화
@RestController
@RequiredArgsConstructor
@RequestMapping("/assistant") // 기본 경로 설정
public class ChatController {
    private final ChatService chatService;

    // 챗봇 시작 (세션 초기화 및 쿠키 발급)
    @PostMapping("/start")
    public ResponseEntity<BaseResponse<String>> start(HttpServletRequest request, HttpServletResponse response) {
        // 1. TokenUtil을 통해 쿠키에서 refreshToken 추출
        String accessToken = TokenUtil.getCookieValue(request, "accessToken");

        String ip = TokenUtil.getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        // 2. 세션 초기화 (회원/비회원 판별 포함)
        String chatToken = chatService.start(accessToken, ip, userAgent);

        // 3. 채팅용 세션 토큰 쿠키 발급 (기존 CookieUtil 활용)
        CookieUtil.addCookie(
                response,
                CookieUtil.CHAT_TOKEN_NAME,
                chatToken,
                CookieUtil.CHAT_TOKEN_EXPIRY);

        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", "채팅을 시작합니다."));
    }

    @PostMapping("/list")
    public ResponseEntity<BaseResponse<List<ChatListDTO>>> list(HttpServletRequest request) {
        // request에서 직접 chatToken 추출
        String chatToken = TokenUtil.getCookieValue(request, CookieUtil.CHAT_TOKEN_NAME);

        if (chatToken == null) {
            throw new IllegalArgumentException("채팅 세션이 만료되었거나 존재하지 않습니다.");
        }

        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", chatService.list(chatToken)));
    }

    @PostMapping("/ask")
    public ResponseEntity<BaseResponse<String>> ask(HttpServletRequest request, @RequestBody String question) {
        // 1. 쿠키에서 chatToken 추출
        String chatToken = TokenUtil.getCookieValue(request, CookieUtil.CHAT_TOKEN_NAME);

        if (chatToken == null) {
            throw new IllegalArgumentException("채팅 세션이 존재하지 않습니다.");
        }

        // 2. 서비스 호출 및 결과 반환
        String answer = chatService.ask(chatToken, question);

        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", answer));
    }
}
