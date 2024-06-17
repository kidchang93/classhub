package kr.co.chunjae.backend.mapper;

import kr.co.chunjae.backend.dto.Classroom;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClassroomMapper {
    Classroom findClassroomByCode(String code);

    void insertClassroom(Classroom classroom);

    Classroom findClassroomByTeacherId(int teacherId);
}
