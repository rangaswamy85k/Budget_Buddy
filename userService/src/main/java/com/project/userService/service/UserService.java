package com.project.userService.service;


import com.project.userService.model.dto.UserDto;
import com.project.userService.model.entity.Profile;
import com.project.userService.repository.ProfileRepo;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private ProfileRepo repo;


    public ResponseEntity<?> createProfile(UserDto dto) {
        Profile profile = new Profile();

        profile.setEmail(dto.getEmail());
        profile.setIsActive(false);
        profile.setUserId(dto.getId());

        return ResponseEntity.ok(repo.save(profile));
    }

    public Profile getByUserId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
    }


}
