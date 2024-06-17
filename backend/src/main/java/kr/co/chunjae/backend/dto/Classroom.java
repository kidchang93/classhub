package kr.co.chunjae.backend.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Classroom {
    private int id;
    private String code;
    private int teacherId;
}
