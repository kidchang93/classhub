package kr.co.chunjae.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class Message {
    private String type;
    private String sender;
    private String sessionId;
    private String clientId;
    private Map<String, Object> data;

}
