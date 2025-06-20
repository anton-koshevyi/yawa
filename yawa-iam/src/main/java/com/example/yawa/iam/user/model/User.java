package com.example.yawa.iam.user.model;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class User {

  private UUID id;
  private String email;
  private String passwordHash;

}
