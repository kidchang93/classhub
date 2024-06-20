package kr.co.chunjae.backend.dto;

import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;

@Data
@ToString
public class Question {
    private int id;
    private int type;
    private String question;
    private ArrayList<String> choices;
    private String classroomId;

}