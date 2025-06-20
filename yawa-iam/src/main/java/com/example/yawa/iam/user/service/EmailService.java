package com.example.yawa.iam.user.service;

public interface EmailService {

  void sendSignUpInit(String email, String verificationToken);

  void sendSignUpComplete(String email);

}
