package org.jenga.dantong.user.model.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class StudentInfo {

    private String name;
    private String studentId;
    private String majorName;
    private String departmentName;

    public StudentInfo(String name, String studentId, String majorName, String departmentName) {
        this.name = name;
        this.studentId = studentId;
        this.majorName = majorName;
        this.departmentName = departmentName;
    }


}
