package vn.edu.fpt.cinemamanagement.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Customer")
public class Customer {
    @Id
    @Column(name = "user_id", length = 10)
    private String user_id;
    private String username;
    private String password;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;
    private Boolean sex;
    private String email;
    private String phone;
    private String verify;
    @Column(name = "reset_requested_at")
    private LocalDateTime resetRequestedAt;

    public Customer() {
    }

    public Customer(String user_id, String username, String password, LocalDate dob, Boolean sex, String email, String phone, String verify, LocalDateTime resetRequestedAt) {
        this.user_id = user_id;
        this.username = username;
        this.password = password;
        this.dob = dob;
        this.sex = sex;
        this.email = email;
        this.phone = phone;
        this.verify = verify;
        this.resetRequestedAt = resetRequestedAt;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public Boolean getSex() {
        return sex;
    }

    public void setSex(Boolean sex) {
        this.sex = sex;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getVerify() {
        return verify;
    }

    public void setVerify(String verify) {
        this.verify = verify;
    }

    public LocalDateTime getResetRequestedAt() {
        return resetRequestedAt;
    }

    public void setResetRequestedAt(LocalDateTime resetRequestedAt) {
        this.resetRequestedAt = resetRequestedAt;
    }
}
