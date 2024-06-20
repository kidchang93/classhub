package kr.co.chunjae.backend.service;

import kr.co.chunjae.backend.dto.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate template;


    public void sendUpdate(String classCode, Message message) {
        template.convertAndSend("/sub/class/" + classCode, message);
        log.info("들어온 메세지 : " + message);
    }


    public void joinClassroom(String classCode, Message message,
                              SimpMessageHeaderAccessor headerAccessor) {
        message.setSessionId(headerAccessor.getSessionId());
        template.convertAndSend("/sub/class/" + classCode + "/join", message);
    }

    public void leaveClassroom(String classCode, Message message,
                               SimpMessageHeaderAccessor headerAccessor) {
        message.setSessionId(headerAccessor.getSessionId());
        template.convertAndSend("/sub/class/" + classCode + "/leave", message);
    }

    public void pickerStart(String classCode, Message message) {
        template.convertAndSend("/sub/class/" + classCode + "/picker/start", message);

    }

    public void pickerSelect(String classCode, Message message,
                             SimpMessageHeaderAccessor headerAccessor) {
        message.setSessionId(headerAccessor.getSessionId());
        template.convertAndSend("/sub/class/" + classCode + "/picker/select", message);
    }

    public void pickerEnd(String classCode, Message message) {
        template.convertAndSend("/sub/class/" + classCode + "/picker/end", message);
    }
}
