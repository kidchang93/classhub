package kr.co.chunjae.backend.controller;


import kr.co.chunjae.backend.dto.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    private final SimpMessagingTemplate template;

    public WebSocketController(SimpMessagingTemplate template) {
        this.template = template;
    }

    @MessageMapping("/update/{classCode}")
    public void sendUpdate(@DestinationVariable(value = "classCode") String classCode, Message message) {
        template.convertAndSend("/sub/class/" + classCode, message);
    }

    @MessageMapping("/join/{classCode}")
    public void joinClassroom(@DestinationVariable(value = "classCode") String classCode, Message message,
                              SimpMessageHeaderAccessor headerAccessor) {
        message.setSessionId(headerAccessor.getSessionId());
        template.convertAndSend("/sub/class/" + classCode + "/join", message);
    }

    @MessageMapping("/leave/{classCode}")
    public void leaveClassroom(@DestinationVariable(value = "classCode") String classCode, Message message,
                               SimpMessageHeaderAccessor headerAccessor) {
        message.setSessionId(headerAccessor.getSessionId());
        template.convertAndSend("/sub/class/" + classCode + "/leave", message);
    }
}
