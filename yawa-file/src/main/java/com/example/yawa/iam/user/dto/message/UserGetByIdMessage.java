package com.example.yawa.iam.user.dto.message;

import java.util.UUID;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserGetByIdMessage {

  private final UUID id;

}
