package co.kr.assistant.service;

import co.kr.assistant.model.dto.ChatListDTO;

import java.util.List;

public interface ChatService {

    String initSession(String ip, String ua);

    List<ChatListDTO> list(String chatToken);

}
