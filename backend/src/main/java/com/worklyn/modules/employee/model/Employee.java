package com.worklyn.modules.employee.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Document(collection="employees")
@CompoundIndex(name="department_status_idx", def="{'department':1,'status':1}")
public class Employee {
    @Id private String id;
    @Indexed(unique=true) private String employeeCode;
    @Indexed(unique=true) private String userId;
    private String firstName; private String lastName;
    @Indexed(unique=true) private String email;
    private Department department; private String designation; private LocalDate dateOfJoining;
    private EmployeeStatus status=EmployeeStatus.ACTIVE; private BigDecimal baseSalary; private String managerId;
    @CreatedDate private Instant createdAt; @LastModifiedDate private Instant updatedAt;
    public Employee(){}
    public String getId(){return id;} public void setId(String v){id=v;}
    public String getEmployeeCode(){return employeeCode;} public void setEmployeeCode(String v){employeeCode=v;}
    public String getUserId(){return userId;} public void setUserId(String v){userId=v;}
    public String getFirstName(){return firstName;} public void setFirstName(String v){firstName=v;}
    public String getLastName(){return lastName;} public void setLastName(String v){lastName=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;}
    public Department getDepartment(){return department;} public void setDepartment(Department v){department=v;}
    public String getDesignation(){return designation;} public void setDesignation(String v){designation=v;}
    public LocalDate getDateOfJoining(){return dateOfJoining;} public void setDateOfJoining(LocalDate v){dateOfJoining=v;}
    public EmployeeStatus getStatus(){return status;} public void setStatus(EmployeeStatus v){status=v;}
    public BigDecimal getBaseSalary(){return baseSalary;} public void setBaseSalary(BigDecimal v){baseSalary=v;}
    public String getManagerId(){return managerId;} public void setManagerId(String v){managerId=v;}
    public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant v){createdAt=v;}
    public Instant getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Instant v){updatedAt=v;}
}
