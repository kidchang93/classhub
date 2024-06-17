package kr.co.chunjae.backend.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Teacher {
    private int id;
    private String username;
    private String password;
}
