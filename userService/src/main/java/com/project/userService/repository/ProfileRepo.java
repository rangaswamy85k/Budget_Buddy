package com.project.userService.repository;

import com.project.userService.model.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepo extends JpaRepository<Profile, Long> {
    Optional<Profile> findById(Long id);
    Optional<Profile> findByUserId(Long id);
}
