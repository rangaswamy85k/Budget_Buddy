package com.project.authService.controller;

import com.project.authService.feign.UserClient;
import com.project.authService.jwt.JwtUtil;
import com.project.authService.model.dto.LoginRequest;
import com.project.authService.model.dto.LoginResponse;
import com.project.authService.model.dto.UserDto;
import com.project.authService.model.entity.User;
import com.project.authService.model.entity.VerificationToken;
import com.project.authService.repository.UserRepo;
import com.project.authService.repository.VerificationTokenRepo;
import com.project.authService.service.EmailService;
import com.project.authService.service.MyUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepo userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    private final UserClient userClient;

    private final VerificationTokenRepo verificationTokenRepo;

    private final EmailService emailService;

    @PostMapping("/register")
    @CircuitBreaker(name = "authService", fallbackMethod = "registerFallback")
    public ResponseEntity<?> register(@RequestBody User user) {

        // 1️⃣ Check email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body("Email already registered");
        }

        // 2️⃣ Assign role (default USER)
        String role = (user.getRole() == null || user.getRole().isBlank())
                ? "USER"
                : user.getRole().toUpperCase();

        // 3️⃣ Create user
        User user1 = User.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(passwordEncoder.encode(user.getPassword()))
                .isActive(false)
                .name(user.getName())
                .role(role)
                .build();

        UserDto userDto=new UserDto();


        userRepository.save(user1);

        userDto.setId(user1.getId());
        userDto.setName(user1.getName());
        userDto.setEmail(user1.getEmail());
        userDto.setRole(user1.getRole());
        userDto.setIsActive(user1.getIsActive())    ;


        System.out.println("user registered");

        userClient.createProfile(userDto);

        // Generate a random token
        String token = UUID.randomUUID().toString();

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user1);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));

        verificationTokenRepo.save(verificationToken);

        emailService.sendVerificationEmail(user.getEmail(), token);

        return ResponseEntity.ok("User Created");
    }


        @PostMapping("/login")
        public LoginResponse login(@RequestBody LoginRequest request) {

            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.getEmail(),
                                    request.getPassword()
                            )
                    );

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if(!user.getIsActive()){
                throw new BadCredentialsException("User is not active");
            }

            String token = jwtUtil.generateToken(new MyUserDetails(user));

            return new LoginResponse(token);
        }

    // Token Verification Endpoint
    @GetMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam String token) {

        VerificationToken verificationToken =
                verificationTokenRepo.findByToken(token)
                        .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Token expired");
        }

        User user = verificationToken.getUser();
        user.setIsActive(true);
        userRepository.save(user);

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setIsActive(user.getIsActive());
        userClient.activateProfile(dto);

        return ResponseEntity.ok("Email verified successfully");
    }


//    @GetMapping("/category")
//    public UserDto userDto(@AuthenticationPrincipal MyUserDetails myUserDetails){
//
//        System.out.println(myUserDetails);
//
//        User user=userRepository.findByEmail(myUserDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
//
//        UserDto userDto=new UserDto();
//        userDto.setId(user.getId());
//        userDto.setEmail(user.getEmail());
//        userDto.setName(user.getName());
//        userDto.setRole(userDto.getRole());
//
//        return userDto;
//    }

    @GetMapping("/users")
    public ResponseEntity<java.util.List<UserDto>> getAllUsers() {
        java.util.List<User> users = userRepository.findAll();
        java.util.List<UserDto> dtos = new java.util.ArrayList<>();
        for (User u : users) {
            UserDto dto = new UserDto();
            dto.setId(u.getId());
            dto.setName(u.getName());
            dto.setEmail(u.getEmail());
            dto.setRole(u.getRole());
            dto.setIsActive(u.getIsActive());
            dtos.add(dto);
        }
        return ResponseEntity.ok(dtos);
    }

    public ResponseEntity<?> registerFallback(User user, Throwable t) {
        // Find and delete the phantom/ghost user by email if it was created during this failed transaction
        userRepository.findByEmail(user.getEmail()).ifPresent(userRepository::delete);
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body("Registration temporarily unavailable due to downstream service outage. Please try again later. Error: " + t.getMessage());
    }
}
