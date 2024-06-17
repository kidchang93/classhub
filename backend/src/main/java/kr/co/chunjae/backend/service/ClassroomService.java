package kr.co.chunjae.backend.service;

import kr.co.chunjae.backend.dto.Classroom;
import kr.co.chunjae.backend.mapper.ClassroomMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassroomService {

    private final ClassroomMapper classroomMapper;

    public void createClassroom(int teacherId) {

        Random random = new Random();
        int code = random.nextInt(900000) + 100000; // 100000에서 999999 사이의 숫자를 생성

        Classroom classroom = new Classroom();
        classroom.setCode(Integer.toString(code));
        classroom.setTeacherId(teacherId);

        classroomMapper.insertClassroom(classroom);
    }

    public Classroom getClassroomByTeacherId(int teacherId) {
        Classroom classroom = classroomMapper.findClassroomByTeacherId(teacherId);
        if (Objects.isNull(classroom)){
            createClassroom(teacherId);
        }

        return classroomMapper.findClassroomByTeacherId(teacherId);
    }

    public Classroom getClassroomByCode(String code) {
        return classroomMapper.findClassroomByCode(code);
    }

}
