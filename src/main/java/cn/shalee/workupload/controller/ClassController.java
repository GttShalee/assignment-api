package cn.shalee.workupload.controller;

import cn.shalee.workupload.dto.response.ClassResponse;
import cn.shalee.workupload.entity.Class;
import cn.shalee.workupload.repository.ClassRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 31930
 */
@Slf4j
@RestController
@RequestMapping("/api/class")
@RequiredArgsConstructor
public class ClassController {
    
    private final ClassRepository classRepository;
    
    @GetMapping
    public ResponseEntity<List<ClassResponse>> getClassList() {
        log.info("收到获取班级列表请求");
        
        List<Class> classes = classRepository.findAll();
        List<ClassResponse> response = classes.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    private ClassResponse convertToResponse(Class clazz) {
        return ClassResponse.builder()
                .classCode(clazz.getClassCode())
                .className(clazz.getClassName())
                .build();
    }
} 