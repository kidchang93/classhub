package kr.co.chunjae.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {
    private Map<String, Map<String, String>> classroomStudents = new HashMap<>();


    public Map<String, String> joinClassroom(SimpMessageHeaderAccessor headerAccessor, Map<String, String> message) {
        String classCode = message.get("classCode");
        String studentName = message.get("studentName");

        classroomStudents.putIfAbsent(classCode, new HashMap<>());
        classroomStudents.get(classCode).put(headerAccessor.getSessionId(), studentName);

        Map<String, String> joinMessage = new HashMap<>();
        joinMessage.put("studentName", studentName);
        joinMessage.put("sessionId", headerAccessor.getSessionId());
        joinMessage.put("classCode", classCode);

        return joinMessage;
    }


    public Map<String, String> webSocketService(SimpMessageHeaderAccessor headerAccessor, Map<String, String> message) {
        String classCode = message.get("classCode");

        String studentName = "";
        if (classroomStudents.containsKey(classCode)) {
            studentName = classroomStudents.get(classCode).remove(headerAccessor.getSessionId());
        }

        Map<String, String> leaveMessage = new HashMap<>();
        leaveMessage.put("sessionId", headerAccessor.getSessionId());
        leaveMessage.put("classCode", classCode);

        return leaveMessage;
    }


}
