package com.example.yawa.iam.user.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.yawa.application.exception.AuthenticationException;
import com.example.yawa.application.exception.ConflictException;
import com.example.yawa.application.exception.NotFoundException;
import com.example.yawa.iam.user.exception.AuthenticationUserByEmailAndPasswordException;
import com.example.yawa.iam.user.exception.ConflictUserEmailException;
import com.example.yawa.iam.user.exception.NotFoundUserByIdException;
import com.example.yawa.iam.user.model.User;
import com.example.yawa.iam.user.repository.UserRepository;

@Service
public class UserService {

  private static final Logger logger = LoggerFactory.getLogger(UserService.class);

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Autowired
  public UserService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder
  ) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public User create(String email, String password) throws ConflictException {
    logger.debug("Create of user by email: {}", email);

    if (userRepository.existsByEmail(email)) {
      throw new ConflictUserEmailException(email);
    }

    String passwordHash = passwordEncoder.encode(password);

    User user = new User();
    user.setEmail(email);
    user.setPasswordHash(passwordHash);
    UUID createdUserId = userRepository.create(user);

    logger.info("User has been created by email: {}", email);
    return userRepository.findById(createdUserId)
        .orElseThrow(() -> new IllegalStateException(
            "Unable to find created user for ID: " + createdUserId));
  }

  public User getById(UUID id) throws NotFoundException {
    return userRepository.findById(id)
        .orElseThrow(() -> new NotFoundUserByIdException(id));
  }

  public User getByEmailAndPassword(String email, String password) throws AuthenticationException {
    return userRepository.findByEmail(email)
        .filter(user -> passwordEncoder.matches(password, user.getPasswordHash()))
        .orElseThrow(() -> new AuthenticationUserByEmailAndPasswordException(email, password));
  }

}
