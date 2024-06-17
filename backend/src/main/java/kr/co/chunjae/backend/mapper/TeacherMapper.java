package kr.co.chunjae.backend.mapper;

import kr.co.chunjae.backend.dto.Teacher;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TeacherMapper {
    Teacher findTeacherByUsername(String username);

    void insertTeacher(Teacher teacher);
}
