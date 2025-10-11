package cn.shalee.workupload.util;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 课程工具类测试
 */
public class CourseUtilsTest {
    
    @Test
    public void testParseCourseCodesFromMask() {
        // 测试 courses = 3 (1 + 2 = 软件工程 + 微机接口)
        List<Integer> courses = CourseUtils.parseCourseCodesFromMask(3);
        assertEquals(2, courses.size());
        assertTrue(courses.contains(1)); // 软件工程
        assertTrue(courses.contains(2)); // 微机接口
        
        // 测试 courses = 5 (1 + 4 = 软件工程 + 操作系统)
        courses = CourseUtils.parseCourseCodesFromMask(5);
        assertEquals(2, courses.size());
        assertTrue(courses.contains(1)); // 软件工程
        assertTrue(courses.contains(4)); // 操作系统
        
        // 测试 courses = 127 (所有课程)
        courses = CourseUtils.parseCourseCodesFromMask(127);
        assertEquals(7, courses.size());
        
        // 测试 courses = 0 (没有选课)
        courses = CourseUtils.parseCourseCodesFromMask(0);
        assertEquals(0, courses.size());
    }
    
    @Test
    public void testIsCourseSelected() {
        // 测试 courses = 3 (1 + 2)
        assertTrue(CourseUtils.isCourseSelected(3, 1)); // 软件工程
        assertTrue(CourseUtils.isCourseSelected(3, 2)); // 微机接口
        assertFalse(CourseUtils.isCourseSelected(3, 4)); // 操作系统
        assertFalse(CourseUtils.isCourseSelected(3, 8)); // 人工智能导论
    }
    
    @Test
    public void testGetCourseName() {
        assertEquals("软件工程", CourseUtils.getCourseName(1));
        assertEquals("微机接口", CourseUtils.getCourseName(2));
        assertEquals("操作系统", CourseUtils.getCourseName(4));
        assertEquals("人工智能导论", CourseUtils.getCourseName(8));
        assertEquals("组成原理", CourseUtils.getCourseName(16));
        assertEquals("神经网络", CourseUtils.getCourseName(32));
        assertEquals("大数据分析", CourseUtils.getCourseName(64));
        assertEquals("未知课程", CourseUtils.getCourseName(999));
    }
}

