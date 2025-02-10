package com.example.clouddisk.controller;

import com.example.clouddisk.model.User;
import com.example.clouddisk.service.UserService;
import com.example.clouddisk.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestParam String username, @RequestParam String password) {
        try{
            String response = userService.register(username, password);
            return ResponseEntity.ok(response);
        }
        catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password,
                                   HttpServletResponse response) {
        // Login and get user object
        User user;
        try {
            user = userService.login(username, password);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

        // Get user details for token generation
        Long userId = user.getId();
        String userRole = user.getRole();

        // Generate tokens
        String accessJWT = jwtUtil.generateJWT(userId, userRole);
        String refreshJWT = jwtUtil.generateRefreshJwt(userId, userRole);

        // Use HttpOnly Cookie to protect refresh token
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshJWT)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/user")
                .maxAge(jwtUtil.getRemainingTime(refreshJWT))
                .build();
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());

        return ResponseEntity.ok(accessJWT);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refresh(@CookieValue(name = "refreshToken", required = false) String refreshJWT,
                                     @RequestHeader("Authorization") String accessHeader,
                                     HttpServletResponse response) {
        // Validate refresh token
        if (refreshJWT == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Empty refresh token");
        }
        Claims refreshJWTPayload = jwtUtil.parsePayload(refreshJWT);
        if (refreshJWTPayload == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }

        // Get user details from old refresh token
        // Add old refresh token to blacklist
        Long userId = jwtUtil.parseSubject(refreshJWTPayload);
        String userRole = jwtUtil.parseRole(refreshJWTPayload);
        String refreshJti = refreshJWTPayload.getId();
        jwtUtil.addBlackList(refreshJti, jwtUtil.getRemainingTime(refreshJWTPayload));

        // Add old access token to blacklist
        if (accessHeader != null && accessHeader.startsWith("Bearer ")) {
            jwtUtil.addBlackList(accessHeader.substring(7));
        }

        // Generate new tokens
        String accessJWT = jwtUtil.generateJWT(userId, userRole);
        refreshJWT = jwtUtil.generateRefreshJwt(userId, userRole);

        // Use HttpOnly Cookie to protect new refresh token
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshJWT)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/user")
                .maxAge(jwtUtil.getRemainingTime(refreshJWT))
                .build();
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());

        return ResponseEntity.ok(accessJWT);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(name = "refreshToken", required = false) String refreshJWT,
                                    @RequestHeader("Authorization") String accessHeader,
                                    HttpServletResponse response)  {
        // clear user ID and role cache
        userService.logout();
        // Add old tokens to blacklist
        jwtUtil.addBlackList(refreshJWT);
        if (accessHeader != null && accessHeader.startsWith("Bearer ")) {
            jwtUtil.addBlackList(accessHeader.substring(7));
        }

        // Delete Cookie
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshJWT)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/user")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());

        return ResponseEntity.ok("User logged out, tokens invalidated");
    }
}
