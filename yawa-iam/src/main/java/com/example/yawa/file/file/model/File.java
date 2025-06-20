package com.example.yawa.file.file.model;

import java.net.URL;
import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(of = "id")
public class File {

  private String id;
  private UUID userId;
  private String name;
  private String contentType;
  private URL url;
  private OffsetDateTime urlExpiredAt;
  private OffsetDateTime createdAt;

}
