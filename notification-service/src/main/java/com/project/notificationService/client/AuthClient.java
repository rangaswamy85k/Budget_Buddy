package com.project.notificationService.client;

import com.project.notificationService.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@FeignClient(name = "authService", url = "http://localhost:8081")
public interface AuthClient {

    @GetMapping("/auth/users")
    List<UserDto> getAllUsers();

}
