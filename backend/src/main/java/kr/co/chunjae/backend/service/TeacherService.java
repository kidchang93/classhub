package kr.co.chunjae.backend.service;


import kr.co.chunjae.backend.dto.Teacher;
import kr.co.chunjae.backend.mapper.TeacherMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherService {

    private final TeacherMapper teacherMapper;
    private final PasswordEncoder passwordEncoder;

    public String login(Teacher teacher) {
        Teacher existingTeacher = teacherMapper.findTeacherByUsername(teacher.getUsername());
        log.info(teacher.getPassword());
        log.info(existingTeacher.getPassword());
        if (existingTeacher != null && passwordEncoder.matches(teacher.getPassword(), existingTeacher.getPassword())) {
            return Integer.toString(existingTeacher.getId());
        } else {
            throw new RuntimeException("Invalid credentials");
        }
    }

    public Teacher register(Teacher teacher) {
        teacher.setPassword(passwordEncoder.encode(teacher.getPassword()));
        teacherMapper.insertTeacher(teacher);
        return teacher;
    }
}
