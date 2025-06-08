package com.example.yawa.iam.user.service;

import java.util.UUID;

import com.example.yawa.application.exception.NotFoundException;
import com.example.yawa.iam.user.model.User;

public interface UserService {

  User findById(UUID id) throws NotFoundException;

}
