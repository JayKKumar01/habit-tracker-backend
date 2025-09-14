package com.jay.habit_tracker.controller;

import com.jay.habit_tracker.entity.User;
import com.jay.habit_tracker.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@RestController
@RequestMapping("/api/master")
@RequiredArgsConstructor
public class MasterController {

    private final JavaMailSender mailSender;

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    /**
     * Simple test endpoint to send an email.
     *
     * Example POST body in Postman:
     * {
     *   "to": "test@example.com",
     *   "subject": "Hello from Habit Tracker",
     *   "body": "This is a test email sent using Spring Boot + Gmail SMTP!"
     * }
     */
    @PostMapping("/send-test-email")
    public ResponseEntity<String> sendTestEmail(@RequestBody EmailRequest request) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(request.getTo());
            message.setSubject(request.getSubject());
            message.setText(request.getBody());
            mailSender.send(message);
            return ResponseEntity.ok("✅ Email sent successfully to " + request.getTo());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("❌ Failed to send email: " + e.getMessage());
        }
    }

    /**
     * Simple DTO for request body.
     */
    @Getter
    @Setter
    public static class EmailRequest {
        private String to;
        private String subject;
        private String body;
    }


//    private final UserRepository userRepository;
//
//    @PersistenceContext
//    private final EntityManager entityManager;

//    /**
//     * Delete all users except the ones whose emails are passed in the request body.
//     */
//    @DeleteMapping("/delete-users-except")
//    public ResponseEntity<String> deleteAllUsersExcept(@RequestBody List<String> emailsToKeep) {
//        if (emailsToKeep == null || emailsToKeep.isEmpty()) {
//            return ResponseEntity.badRequest().body("emailsToKeep list cannot be empty.");
//        }
//
//        // Fetch users to delete
//        List<User> usersToDelete = entityManager.createQuery(
//                        "SELECT u FROM User u WHERE u.email NOT IN :emails", User.class)
//                .setParameter("emails", emailsToKeep)
//                .getResultList();
//
//        // Log deleted users
//        for (User user : usersToDelete) {
//            System.out.println("Deleting user: " + user.getEmail() + " - " + user.getName());
//        }
//
//        userRepository.deleteAll(usersToDelete);
//        return ResponseEntity.ok("Deleted " + usersToDelete.size() + " users successfully.");
//    }
//
//    /**
//     * Get all users.
//     */
//    @GetMapping("/users")
//    public ResponseEntity<List<User>> getAllUsers() {
//        List<User> users = userRepository.findAll();
//        return ResponseEntity.ok(users);
//    }
}
