package com.example.yawa.email;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNSRecord;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import software.amazon.awssdk.services.ses.SesClient;

public class EmailHandler implements RequestHandler<SNSEvent, Boolean> {

  private final SesClient sesClient = SesClient.create();
  private final ObjectMapper snsMessageMapper = new ObjectMapper()
      .registerModule(new JavaTimeModule())
      .setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CASE)
      .setDefaultPropertyInclusion(Include.NON_NULL)
      .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .disable(SerializationFeature.INDENT_OUTPUT);

  @Override
  public Boolean handleRequest(SNSEvent event, Context context) {
    LambdaLogger logger = context.getLogger();

    for (SNSRecord snsRecord : event.getRecords()) {
      try {
        String message = snsRecord.getSNS().getMessage();
        logger.log("Send of email from SNS message: " + message);

        Map<String, String> email = readMessage(message);
        String sender = System.getenv("APPLICATION_EMAIL_SENDER");
        String recipient = email.get("recipient");
        String subject = email.get("subject");
        String body = email.get("body");

        sendEmail(sender, recipient, subject, body);
        logger.log("Email has been sent from SNS message: " + message);
      } catch (Throwable e) {
        logger.log("Error while processing SNS: " + e.getMessage());
      }
    }

    return Boolean.TRUE;
  }

  private void sendEmail(
      String sender,
      String recipient,
      String subject,
      String body
  ) {
    sesClient
        .sendEmail(ser -> ser
            .source(sender)
            .destination(d -> d
                .toAddresses(recipient))
            .message(m -> m
                .subject(c -> c
                    .data(subject))
                .body(b -> b
                    .text(c -> c
                        .data(body)))));
  }

  private Map<String, String> readMessage(String message) {
    try {
      return snsMessageMapper.readValue(message, new TypeReference<Map<String, String>>() {});
    } catch (Exception e) {
      throw new RuntimeException("Unable to read SNS message", e);
    }
  }

}
