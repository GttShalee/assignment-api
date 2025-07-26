package cn.shalee.workupload.entity;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * @author shalee
 */

@Entity
@Table(name = "user")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String realName;

    @Column(nullable = false)
    private String avatarUrl;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column
    private String role;

    @Column(name = "student_id", nullable = false, unique = true)
    private String studentId;

    private String classCode;

    @Column(nullable = false)
    private Integer roleType = 1; // 1-学生 2-学委 3-教师 4-管理员

    @Column(nullable = false)
    private Boolean status = true; // 账户状态

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}