package com.jay.habit_tracker.service;

import com.jay.habit_tracker.dto.AuthRequest;
import com.jay.habit_tracker.dto.user.UserResponse;
import com.jay.habit_tracker.dto.user.UserRegistration;
import com.jay.habit_tracker.entity.EmailVerification;
import com.jay.habit_tracker.entity.User;
import com.jay.habit_tracker.mapper.UserMapper;
import com.jay.habit_tracker.repository.EmailVerificationRepository;
import com.jay.habit_tracker.repository.UserRepository;
import com.jay.habit_tracker.util.JwtUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final EmailVerificationRepository verificationRepository;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    @Transactional
    public UserResponse signup(UserRegistration dto) {
        // 1️⃣ Check email verification status using EntityManager
        Boolean isVerified = (Boolean) entityManager.createNativeQuery(
                        "SELECT verified FROM email_verifications " +
                                "WHERE email = :email " +
                                "ORDER BY created_at DESC " + // Get the most recent verification attempt
                                "LIMIT 1")
                .setParameter("email", dto.getEmail())
                .getResultStream()
                .findFirst()
                .orElse(false);

        if (!isVerified) {
            throw new RuntimeException("Email not verified. Please verify before signing up.");
        }

        // 2️⃣ Check if user already exists
        boolean userExists = entityManager.createNativeQuery(
                        "SELECT 1 FROM users WHERE email = :email")
                .setParameter("email", dto.getEmail())
                .getResultStream()
                .findFirst()
                .isPresent();

        if (userExists) {
            throw new RuntimeException("User already exists with this email.");
        }

        // 3️⃣ Save the user
        User user = userMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Hash password
        userRepository.save(user);

        return userMapper.toDto(user);
    }


    @Override
    public String login(AuthRequest request) {
        Optional<Object[]> result = entityManager.createNativeQuery(
                        "SELECT id, password FROM users WHERE email = :email LIMIT 1")
                .setParameter("email", request.getEmail())
                .getResultStream()
                .map(r -> (Object[]) r)
                .findFirst();

        if (result.isEmpty()) {
            throw new RuntimeException("Invalid email or password");
        }

        Object[] row = result.get();
        Long userId = ((Number) row[0]).longValue(); // safe conversion for PostgreSQL numeric types
        String hashedPassword = (String) row[1];

        if (!passwordEncoder.matches(request.getPassword(), hashedPassword)) {
            throw new RuntimeException("Invalid email or password");
        }

        User user = User.builder()
                .id(userId)
                .email(request.getEmail())
                .build();

        return jwtUtil.generateToken(user);
    }

    // ------------------- Email Verification Methods -------------------
    @Override
    public void sendVerificationCode(String email) {
        // Generate 6-digit code
        String code = String.valueOf((int) (Math.random() * 900000) + 100000);

        // Save to DB with 10 minutes expiry
        EmailVerification verification = EmailVerification.builder()
                .email(email)
                .code(code)
                .verified(false)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(600))
                .build();
        verificationRepository.save(verification);

        // Send email asynchronously
        try {
            sendEmailAsync(email, code);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @Async
    public void sendEmailAsync(String email, String code) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(email);
        helper.setSubject("Habit Tracker - Email Verification Code");

        String htmlContent = "<!DOCTYPE html>" +
                "<html>" +
                "<body style='font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 20px;'>" +
                "<div style='max-width: 600px; margin: auto; background: #ffffff; padding: 20px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1);'>" +
                "<h2 style='color: #333;'>Habit Tracker</h2>" +
                "<p>Hello,</p>" +
                "<p>You requested a verification code for your Habit Tracker account.</p>" +
                "<h3 style='color: #0077ff;'>" + code + "</h3>" +
                "<p>This code will expire in 10 minutes.</p>" +
                "<p>If you did not request this code, please ignore this email.</p>" +
                "<hr style='border: none; border-top: 1px solid #eee;' />" +
                "<p style='font-size: 0.9rem; color: #555;'>Habit Tracker Team</p>" +
                "</div>" +
                "</body>" +
                "</html>";

        helper.setText(htmlContent, true); // true = HTML
        mailSender.send(message);
    }


    @Override
    @Transactional
    public boolean verifyCode(String email, String code) {
        // Update the verification to verified in DB only if it exists, not expired, and matches code
        int updated = entityManager.createNativeQuery(
                        "UPDATE email_verifications " +
                                "SET verified = TRUE " +
                                "WHERE email = :email " +
                                "AND code = :code " +
                                "AND expires_at > now() " +
                                "AND verified = FALSE")
                .setParameter("email", email)
                .setParameter("code", code)
                .executeUpdate();

        // executeUpdate returns number of rows updated; if 1 => success
        return updated == 1;
    }


}
