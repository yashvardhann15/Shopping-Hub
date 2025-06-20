package com.project.userservicejwt.Service;

import com.project.userservicejwt.DTO.LoginDTO;
import com.project.userservicejwt.DTO.UserRegisterDTO;
import com.project.userservicejwt.DTO.UserRegisterVerifyDTO;
import com.project.userservicejwt.Projections.UserProjection;
import com.project.userservicejwt.models.User;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserService {
    public ResponseEntity<?> registerUser(UserRegisterDTO userRegisterDTO);
    public ResponseEntity<List<User>> getAllUsers();
    public ResponseEntity<List<User>> getAllActiveUsers();
    public ResponseEntity<String> addRole(String role);
    public ResponseEntity<UserProjection> getUser(String email);
    public ResponseEntity<?> login(LoginDTO user);

    ResponseEntity<?> registerUserComp(UserRegisterVerifyDTO user);
}
