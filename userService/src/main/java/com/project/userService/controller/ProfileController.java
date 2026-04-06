package com.project.userService.controller;

import com.project.userService.model.dto.UserDto;
import com.project.userService.model.entity.Profile;
import com.project.userService.repository.ProfileRepo;
import com.project.userService.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService service;

    @Autowired
    private ProfileRepo repo;

    @PostMapping("/register")
    public ResponseEntity<?> createProfile(@RequestBody UserDto userDto)  {
        return service.createProfile(userDto);
    }

//    @GetMapping("/profile")
//    public ResponseEntity<?> fetchProfile(HttpRequest request) {
//        Long id = request.getAttributes();
//    }

    @GetMapping("/by-user/{id}")
    public ResponseEntity<Profile> getProfileByUserId(@PathVariable Long id) {

        return ResponseEntity.ok(
                service.getByUserId(id)
        );
    }

    @PostMapping("/activate")
    public ResponseEntity<String> activateProfile(@RequestBody UserDto dto) {
        Profile profile = service.getByUserId(dto.getId());

        profile.setIsActive(dto.getIsActive());
        repo.save(profile);

        return ResponseEntity.ok("Profile activated too");
    }
}
