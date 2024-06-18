package kr.co.chunjae.backend.controller;


import kr.co.chunjae.backend.dto.Classroom;
import kr.co.chunjae.backend.service.ClassroomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/classrooms")
@RequiredArgsConstructor
@Slf4j
public class ClassroomController {

    private final ClassroomService classroomService;

    @GetMapping("/teacher/{teacherId}")
    public Classroom getClassroomByTeacherId(@PathVariable("teacherId") int teacherId) {
        return classroomService.getClassroomByTeacherId(teacherId);
    }

    @GetMapping("/{code}")
    public Classroom getClassroomByCode(@PathVariable("code") String code) {
        return classroomService.getClassroomByCode(code);
    }
}
