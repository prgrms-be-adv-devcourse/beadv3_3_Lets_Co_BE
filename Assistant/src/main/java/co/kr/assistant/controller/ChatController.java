package co.kr.assistant.controller;

import co.kr.assistant.model.dto.ChatListDTO;
import co.kr.assistant.service.ChatService;
import co.kr.assistant.util.BaseResponse;
import co.kr.assistant.util.CookieUtil;
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
    @PostMapping("/init")
    public ResponseEntity<BaseResponse<String>> init(HttpServletRequest request, HttpServletResponse response) {
        String ip = request.getRemoteAddr();
        String ua = request.getHeader("User-Agent");

        // Service 호출하여 MariaDB/Redis에 저장하고 토큰 받아옴
        String chatToken = chatService.initSession(ip, ua);

        // 클라이언트 쿠키에 토큰 심어줌 (1시간 유효)
        CookieUtil.addCookie(response, "chatToken", chatToken, 3600);

        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", "Session Initialized"));
    }

    @PostMapping("/list")
    public ResponseEntity<BaseResponse<List<ChatListDTO>>> list (@CookieValue(name = "chatToken", required = false) String chatToken,
                                                                 HttpServletResponse response) {


        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", chatService.list(chatToken)));

    }
}
