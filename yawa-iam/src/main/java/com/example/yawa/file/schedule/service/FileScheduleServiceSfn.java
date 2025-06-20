package com.example.yawa.file.schedule.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.ExecutionListItem;
import software.amazon.awssdk.services.sfn.model.ExecutionStatus;
import software.amazon.awssdk.services.sfn.model.StateMachineDeletingException;
import software.amazon.awssdk.services.sfn.model.StateMachineType;

import com.example.yawa.application.exception.NotFoundException;
import com.example.yawa.file.schedule.exception.NotFoundFileScheduleByFileIdException;

@Service
public class FileScheduleServiceSfn
    implements FileScheduleService, InitializingBean, DisposableBean {

  private static final int STATE_MACHINE_RECREATION_INTERVAL_MILLIS = 5000;

  private static final Logger logger = LoggerFactory.getLogger(FileScheduleServiceSfn.class);

  private final SfnClient sfnClient;
  private final String stateMachineName;
  private final String stateMachineRoleArn;
  private final Integer waitSeconds;
  private final String publishTopicArn;

  private String stateMachineArn;

  @Autowired
  public FileScheduleServiceSfn(
      SfnClient sfnClient,
      FileScheduleServiceSfnProperties properties
  ) {
    this.sfnClient = sfnClient;
    this.stateMachineName = properties.stateMachineName;
    this.stateMachineRoleArn = properties.stateMachineRoleArn;
    this.waitSeconds = properties.waitSeconds;
    this.publishTopicArn = properties.publishTopicArn;
  }

  @Override
  public void createDeletion(String fileId) {
    logger.debug("Schedule of delete for file with ID: {}", fileId);

    String executionName = Assembler.assembleExecutionName(fileId);
    String input = Assembler.assembleInput(waitSeconds, publishTopicArn, fileId);

    startExecution(executionName, input);
    logger.info("Delete has been scheduled for file with ID: {}", fileId);
  }

  @Override
  public void postponeDeletion(String fileId) throws NotFoundException {
    logger.debug("Postpone of delete for file with ID: {}", fileId);

    ExecutionListItem execution = findExecutionByFileId(fileId);
    String executionArn = execution.executionArn();

    stopExecution(executionArn, "File deletion has been postponed by user");
    redriveExecution(executionArn);

    logger.info("Delete has been postponed for file with ID: {}", fileId);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    try {
      logger.debug("Create of state machine: {}", stateMachineName);
      this.stateMachineArn = createStateMachine(stateMachineName, stateMachineRoleArn);
      logger.info("State machine has been created with ARN: {}", stateMachineArn);
    } catch (StateMachineDeletingException e) {
      logger.warn("State machine is being deleted, waiting before recreate...", e);
      Thread.sleep(STATE_MACHINE_RECREATION_INTERVAL_MILLIS);
      this.afterPropertiesSet();
    }
  }

  @Override
  public void destroy() {
    logger.debug("Stopping all executions by state machine with ARN: {}", stateMachineArn);

    for (ExecutionListItem execution : findAllExecutions(ExecutionStatus.RUNNING)) {
      String executionArn = execution.executionArn();
      logger.debug("Stopping of execution by ARN: {}", executionArn);
      stopExecution(executionArn, "Application stopped");
      logger.info("Execution has been stopped by ARN: {}", executionArn);
    }

    logger.debug("Delete of state machine by ARN: {}", stateMachineArn);
    deleteStateMachine(stateMachineArn);
    logger.info("State machine has been deleted by ARN: {}", stateMachineArn);
  }

  private String createStateMachine(String stateMachineName, String stateMachineRoleArn) {
    String definition = ""
        + "{"
        + "  \"StartAt\": \"Wait\","
        + "  \"States\": {"
        + "    \"Wait\": {"
        + "      \"Type\": \"Wait\","
        + "      \"SecondsPath\": \"$.waitSeconds\","
        + "      \"Next\": \"Publish\""
        + "    },"
        + "    \"Publish\": {"
        + "      \"Type\": \"Task\","
        + "      \"Resource\": \"arn:aws:states:::sns:publish\","
        + "      \"Parameters\": {"
        + "        \"TopicArn.$\": \"$.publishTopicArn\","
        + "        \"Message.$\": \"$.publishMessage\""
        + "      },"
        + "      \"End\": true"
        + "    }"
        + "  }"
        + "}";

    return sfnClient
        .createStateMachine(csmr -> csmr
            .name(stateMachineName)
            .definition(definition)
            .roleArn(stateMachineRoleArn)
            .type(StateMachineType.STANDARD))
        .stateMachineArn();
  }

  private void deleteStateMachine(String stateMachineArn) {
    sfnClient
        .deleteStateMachine(dsmr -> dsmr
            .stateMachineArn(stateMachineArn));
  }

  private void startExecution(String executionName, String input) {
    sfnClient
        .startExecution(ser -> ser
            .stateMachineArn(stateMachineArn)
            .name(executionName)
            .input(input));
  }

  private void stopExecution(String executionArn, String cause) {
    sfnClient
        .stopExecution(ser -> ser
            .executionArn(executionArn)
            .cause(cause));
  }

  private void redriveExecution(String executionArn) {
    sfnClient
        .redriveExecution(rer -> rer
            .executionArn(executionArn));
  }

  private ExecutionListItem findExecutionByFileId(String fileId) throws NotFoundException {
    String executionName = Assembler.assembleExecutionName(fileId);

    return findAllExecutions(ExecutionStatus.RUNNING).stream()
        .filter(execution -> execution.name().equals(executionName))
        .findAny()
        .orElseThrow(() -> new NotFoundFileScheduleByFileIdException(fileId));
  }

  private List<ExecutionListItem> findAllExecutions(ExecutionStatus status) {
    // Pagination has been omitted for simplicity.
    return sfnClient
        .listExecutions(ler -> ler
            .stateMachineArn(stateMachineArn)
            .statusFilter(status))
        .executions();
  }


  public static class FileScheduleServiceSfnProperties {

    private final String stateMachineName;
    private final String stateMachineRoleArn;
    private final Integer waitSeconds;
    private final String publishTopicArn;

    public FileScheduleServiceSfnProperties(
        String stateMachineName,
        String stateMachineRoleArn,
        Integer waitSeconds,
        String publishTopicArn
    ) {
      this.stateMachineName = stateMachineName;
      this.stateMachineRoleArn = stateMachineRoleArn;
      this.waitSeconds = waitSeconds;
      this.publishTopicArn = publishTopicArn;
    }

  }

  private static class Assembler {

    private static final int EXECUTION_NAME_FILE_ID_LENGTH = 12;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static String assembleInput(
        Integer waitSeconds,
        String publishTopicArn,
        String fileId
    ) {
      Map<String, String> input = new LinkedHashMap<>();
      input.put("waitSeconds", waitSeconds.toString());
      input.put("publishTopicArn", publishTopicArn);
      input.put("publishMessage", fileId);
      return writeAsString(input);
    }

    static String assembleExecutionName(String fileId) {
      return "deletion-" + fileId.substring(fileId.length() - EXECUTION_NAME_FILE_ID_LENGTH);
    }

    private static String writeAsString(Map<String, String> input) {
      try {
        return objectMapper.writeValueAsString(input);
      } catch (Exception e) {
        throw new RuntimeException("Unable to write input", e);
      }
    }

  }

}
