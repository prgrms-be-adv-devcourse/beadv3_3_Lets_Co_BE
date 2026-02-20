package co.kr.assistant.service;

import co.kr.assistant.model.dto.ChatListDTO;

import java.util.List;

public interface ChatService {

    String start(String refreshToken, String ip, String ua);

    List<ChatListDTO> list(String chatToken);

}
