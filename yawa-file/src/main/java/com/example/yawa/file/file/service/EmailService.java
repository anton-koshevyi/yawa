package com.example.yawa.file.file.service;

import com.example.yawa.file.file.model.File;

public interface EmailService {

  void sendFileUpload(String email, File file);

  void sendFileDelete(String email, File file);

}
