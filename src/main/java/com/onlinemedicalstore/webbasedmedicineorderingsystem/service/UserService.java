package com.onlinemedicalstore.webbasedmedicineorderingsystem.service;

import com.onlinemedicalstore.webbasedmedicineorderingsystem.entity.Role;
import com.onlinemedicalstore.webbasedmedicineorderingsystem.entity.User;
import com.onlinemedicalstore.webbasedmedicineorderingsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public User updateUserProfile(String currentUsername, String newUsername, String newEmail) {
        User user = findUserByUsername(currentUsername);

        if (newUsername != null && !newUsername.isBlank() && !Objects.equals(currentUsername, newUsername)) {
            if (userRepository.findByUsername(newUsername).isPresent()) {
                throw new RuntimeException("New username is already taken.");
            }
            user.setUsername(newUsername);
        }

        if (newEmail != null && !newEmail.isBlank() && !Objects.equals(user.getEmail(), newEmail)) {
            Optional<User> existingUserByEmail = userRepository.findAll().stream()
                    .filter(u -> !Objects.equals(u.getId(), user.getId()) && u.getEmail().equalsIgnoreCase(newEmail))
                    .findFirst();
            if (existingUserByEmail.isPresent()) {
                throw new RuntimeException("New email is already registered.");
            }
            user.setEmail(newEmail);
        }

        return userRepository.save(user);
    }

    @Transactional
    public void changeUserPassword(String username, String oldPassword, String newPassword) {
        User user = findUserByUsername(username);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Incorrect old password.");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new RuntimeException("New password cannot be empty.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(String username) {
        User user = findUserByUsername(username);
        userRepository.delete(user);
    }

    // --- Admin Methods ---

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User createUserByAdmin(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username is already taken.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public User updateUserByAdmin(Long id, User userDetails) {
        User existingUser = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        // Check if username is being changed and if it's already taken by someone else
        if (!existingUser.getUsername().equals(userDetails.getUsername()) && userRepository.findByUsername(userDetails.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username is already taken.");
        }

        existingUser.setUsername(userDetails.getUsername());
        existingUser.setEmail(userDetails.getEmail());
        existingUser.setRole(userDetails.getRole());

        return userRepository.save(existingUser);
    }

    @Transactional
    public void deleteUserByAdmin(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }
}
