package co.kr.assistant.service;

import co.kr.assistant.model.dto.chat.ChatDTO;
import co.kr.assistant.model.dto.list.ChatListDTO;

import java.util.List;

public interface ChatService {

    String start(String accessToken, String ip, String ua);

    List<ChatListDTO> list(String chatToken);

    ChatDTO ask(String chatToken, String question);

}
