package com.project.authService.feign;

import com.project.authService.model.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "userService" ,url = "http://localhost:8082/profile")
public interface UserClient {

    @PostMapping("/register")
    ResponseEntity<?> createProfile(@RequestBody UserDto userDto);

    @PostMapping("/activate")
    public ResponseEntity<String> activateProfile(@RequestBody UserDto userDto);

}
