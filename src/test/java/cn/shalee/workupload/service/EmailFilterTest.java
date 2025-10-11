package cn.shalee.workupload.service;

import cn.shalee.workupload.entity.User;
import cn.shalee.workupload.entity.Homework;
import cn.shalee.workupload.util.CourseUtils;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 邮件过滤逻辑测试
 */
public class EmailFilterTest {
    
    @Test
    public void testEmailFilterLogic() {
        // 创建测试用户
        User user1 = new User();
        user1.setStudentId("202303013056");
        user1.setCourses(3); // 选了软件工程(1) + 微机接口(2)
        
        User user2 = new User();
        user2.setStudentId("202303013057");
        user2.setCourses(5); // 选了软件工程(1) + 操作系统(4)
        
        User user3 = new User();
        user3.setStudentId("202303013058");
        user3.setCourses(0); // 未选课
        
        User user4 = new User();
        user4.setStudentId("202303013059");
        user4.setCourses(null); // 未选课
        
        List<User> allStudents = Arrays.asList(user1, user2, user3, user4);
        
        // 测试软件工程作业 (courseCode = 1)
        Homework softwareHomework = new Homework();
        softwareHomework.setCourseCode(1);
        
        List<User> softwareTargets = allStudents.stream()
                .filter(student -> {
                    Integer studentCourses = student.getCourses();
                    if (studentCourses == null || studentCourses == 0) {
                        return false;
                    }
                    return CourseUtils.isCourseSelected(studentCourses, softwareHomework.getCourseCode());
                })
                .toList();
        
        // 应该只有user1和user2收到软件工程作业通知
        assertEquals(2, softwareTargets.size());
        assertTrue(softwareTargets.contains(user1));
        assertTrue(softwareTargets.contains(user2));
        
        // 测试微机接口作业 (courseCode = 2)
        Homework microcomputerHomework = new Homework();
        microcomputerHomework.setCourseCode(2);
        
        List<User> microcomputerTargets = allStudents.stream()
                .filter(student -> {
                    Integer studentCourses = student.getCourses();
                    if (studentCourses == null || studentCourses == 0) {
                        return false;
                    }
                    return CourseUtils.isCourseSelected(studentCourses, microcomputerHomework.getCourseCode());
                })
                .toList();
        
        // 应该只有user1收到微机接口作业通知
        assertEquals(1, microcomputerTargets.size());
        assertTrue(microcomputerTargets.contains(user1));
        
        // 测试操作系统作业 (courseCode = 4)
        Homework osHomework = new Homework();
        osHomework.setCourseCode(4);
        
        List<User> osTargets = allStudents.stream()
                .filter(student -> {
                    Integer studentCourses = student.getCourses();
                    if (studentCourses == null || studentCourses == 0) {
                        return false;
                    }
                    return CourseUtils.isCourseSelected(studentCourses, osHomework.getCourseCode());
                })
                .toList();
        
        // 应该只有user2收到操作系统作业通知
        assertEquals(1, osTargets.size());
        assertTrue(osTargets.contains(user2));
        
        // 测试未指定课程的作业 (courseCode = null)
        Homework generalHomework = new Homework();
        generalHomework.setCourseCode(null);
        
        List<User> generalTargets = allStudents.stream()
                .filter(student -> {
                    Integer studentCourses = student.getCourses();
                    if (studentCourses == null || studentCourses == 0) {
                        return false;
                    }
                    // 未指定课程代码，发送给所有选课学生
                    return generalHomework.getCourseCode() == null || generalHomework.getCourseCode() == 0;
                })
                .toList();
        
        // 应该只有选课的学生收到通知
        assertEquals(2, generalTargets.size());
        assertTrue(generalTargets.contains(user1));
        assertTrue(generalTargets.contains(user2));
    }
}

