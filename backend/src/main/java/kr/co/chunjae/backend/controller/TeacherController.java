package kr.co.chunjae.backend.controller;


import kr.co.chunjae.backend.dto.Teacher;
import kr.co.chunjae.backend.service.TeacherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@Slf4j
public class TeacherController {

    private final TeacherService teacherService;

    @PostMapping("/login")
    public String login(@RequestBody Teacher teacher) {
        return teacherService.login(teacher);
    }

    //회원가입.. 넣어야하나..?
    @PostMapping("/register")
    public Teacher register(@RequestBody Teacher teacher) {
        return teacherService.register(teacher);
    }
}
