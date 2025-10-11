package cn.shalee.workupload.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 课程位掩码工具类
 * @author shalee
 */
public class CourseUtils {
    
    /**
     * 课程代码定义
     */
    public static final int SOFTWARE_ENGINEERING = 1;    // 软件工程
    public static final int MICROCOMPUTER_INTERFACE = 2; // 微机接口
    public static final int OPERATING_SYSTEM = 4;        // 操作系统
    public static final int AI_INTRODUCTION = 8;         // 人工智能导论
    public static final int COMPUTER_ORGANIZATION = 16;  // 组成原理
    public static final int NEURAL_NETWORK = 32;         // 神经网络
    public static final int BIG_DATA_ANALYSIS = 64;      // 大数据分析
    
    /**
     * 从位掩码中解析出所有选中的课程代码
     * 
     * @param coursesMask 课程位掩码（所有选中课程代码的和）
     * @return 选中的课程代码列表
     */
    public static List<Integer> parseCourseCodesFromMask(int coursesMask) {
        List<Integer> selectedCourses = new ArrayList<>();
        
        // 检查每个课程位是否被设置
        if ((coursesMask & SOFTWARE_ENGINEERING) != 0) {
            selectedCourses.add(SOFTWARE_ENGINEERING);
        }
        if ((coursesMask & MICROCOMPUTER_INTERFACE) != 0) {
            selectedCourses.add(MICROCOMPUTER_INTERFACE);
        }
        if ((coursesMask & OPERATING_SYSTEM) != 0) {
            selectedCourses.add(OPERATING_SYSTEM);
        }
        if ((coursesMask & AI_INTRODUCTION) != 0) {
            selectedCourses.add(AI_INTRODUCTION);
        }
        if ((coursesMask & COMPUTER_ORGANIZATION) != 0) {
            selectedCourses.add(COMPUTER_ORGANIZATION);
        }
        if ((coursesMask & NEURAL_NETWORK) != 0) {
            selectedCourses.add(NEURAL_NETWORK);
        }
        if ((coursesMask & BIG_DATA_ANALYSIS) != 0) {
            selectedCourses.add(BIG_DATA_ANALYSIS);
        }
        
        return selectedCourses;
    }
    
    /**
     * 检查某个课程是否被选中
     * 
     * @param coursesMask 课程位掩码
     * @param courseCode 要检查的课程代码
     * @return 是否选中该课程
     */
    public static boolean isCourseSelected(int coursesMask, int courseCode) {
        return (coursesMask & courseCode) != 0;
    }
    
    /**
     * 获取课程名称
     * 
     * @param courseCode 课程代码
     * @return 课程名称
     */
    public static String getCourseName(int courseCode) {
        switch (courseCode) {
            case SOFTWARE_ENGINEERING:
                return "软件工程";
            case MICROCOMPUTER_INTERFACE:
                return "微机接口";
            case OPERATING_SYSTEM:
                return "操作系统";
            case AI_INTRODUCTION:
                return "人工智能导论";
            case COMPUTER_ORGANIZATION:
                return "组成原理";
            case NEURAL_NETWORK:
                return "神经网络";
            case BIG_DATA_ANALYSIS:
                return "大数据分析";
            default:
                return "未知课程";
        }
    }
}

