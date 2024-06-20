package kr.co.chunjae.backend.controller;


import kr.co.chunjae.backend.dto.Message;
import kr.co.chunjae.backend.service.ClassroomService;
import kr.co.chunjae.backend.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final WebSocketService webSocketService;


    @MessageMapping("/update/{classCode}")
    public void sendUpdate(@DestinationVariable(value = "classCode") String classCode, Message message) {
        webSocketService.sendUpdate(classCode, message);
    }

    @MessageMapping("/join/{classCode}")
    public void joinClassroom(@DestinationVariable(value = "classCode") String classCode, Message message,
                              SimpMessageHeaderAccessor headerAccessor) {
        webSocketService.joinClassroom(classCode, message, headerAccessor);
    }

    @MessageMapping("/leave/{classCode}")
    public void leaveClassroom(@DestinationVariable(value = "classCode") String classCode, Message message,
                               SimpMessageHeaderAccessor headerAccessor) {
        webSocketService.leaveClassroom(classCode, message, headerAccessor);
    }

    @MessageMapping("/picker/start/{classCode}")
    public void pickerStart(@DestinationVariable(value = "classCode") String classCode, Message message) {
        webSocketService.pickerStart(classCode, message);
    }

    @MessageMapping("/picker/select/{classCode}")
    public void pickerSelect(@DestinationVariable(value = "classCode") String classCode, Message message,
                             SimpMessageHeaderAccessor headerAccessor) {
        webSocketService.pickerSelect(classCode, message, headerAccessor);
    }
}
