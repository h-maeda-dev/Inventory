// src/main/java/com/example/inventory/domain/model/Employee.java
package com.example.inventory.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "employees")
@Getter @Setter @NoArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Request> requests;

    // --- ここからアカウントロック用のフィールドを追加 ---
    // 既存レコードのために columnDefinition でデフォルト値を指定
    @Column(nullable = false, columnDefinition = "integer default 0")
    private int failedAttempt = 0;

    // 既存レコードのために columnDefinition でデフォルト値を指定
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean accountNonLocked = true;

    private LocalDateTime lockTime;
    // --- ここまで ---

    public String getDisplayName() {
        String deptName = department != null ? department.getName() : "未所属";
        return name + "（" + id + "）: " + deptName;
    }
}