package com.enterprise.shop.backend.soap;

import com.enterprise.shop.backend.dto.*;
import com.enterprise.shop.backend.model.User;
import com.enterprise.shop.backend.service.UserService;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@WebService(serviceName = "UserService")
@Service
@RequiredArgsConstructor
public class UserSoapService {
    
    private final UserService userService;
    
    @WebMethod
    public User register(
            @WebParam(name = "email") String email,
            @WebParam(name = "password") String password,
            @WebParam(name = "firstName") String firstName,
            @WebParam(name = "lastName") String lastName,
            @WebParam(name = "phone") String phone,
            @WebParam(name = "address") String address) {
        
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .email(email)
                .password(password)
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .address(address)
                .build();
        
        return userService.register(request);
    }
    
    @WebMethod
    public LoginResponse login(
            @WebParam(name = "email") String email,
            @WebParam(name = "password") String password) {
        
        LoginRequest request = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();
        
        return userService.login(request);
    }
    
    @WebMethod
    public User updateProfile(
            @WebParam(name = "userId") Long userId,
            @WebParam(name = "firstName") String firstName,
            @WebParam(name = "lastName") String lastName,
            @WebParam(name = "phone") String phone,
            @WebParam(name = "address") String address) {
        
        UserProfileRequest request = UserProfileRequest.builder()
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .address(address)
                .build();
        
        return userService.updateProfile(userId, request);
    }
    
    @WebMethod
    public User getUser(@WebParam(name = "userId") Long userId) {
        return userService.getUserById(userId);
    }
}
