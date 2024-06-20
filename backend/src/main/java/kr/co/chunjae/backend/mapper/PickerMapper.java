package kr.co.chunjae.backend.mapper;

import kr.co.chunjae.backend.dto.Question;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface PickerMapper {
    
    void insertQuestion(Question question);

    void insertChoice(int questionId, String choiceText);

    List<Map<String, Object>> selectQuestionsByClassroomIdAndType(int classroomId, int type);
}
