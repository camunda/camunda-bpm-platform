package org.camunda.bpm.spring.boot.starter.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.io.ByteArrayInputStream;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import my.own.custom.spring.boot.project.SampleCamundaRestApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SampleCamundaRestApplication.class, webEnvironment = RANDOM_PORT)
public class SampleCamundaRestApplicationIT {

  @Autowired
  private TestRestTemplate testRestTemplate;

  @Autowired
  private RuntimeService runtimeService;

  @Autowired
  private CamundaBpmProperties camundaBpmProperties;

  @Test
  public void restApiIsAvailable() throws Exception {
    ResponseEntity<String> entity = testRestTemplate.getForEntity("/rest/engine/", String.class);
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    assertEquals("[{\"name\":\"testEngine\"}]", entity.getBody());
  }

  @Test
  public void startProcessInstanceByCustomResource() throws Exception {
    ResponseEntity<ProcessInstanceDto> entity = testRestTemplate.postForEntity("/rest/process/start", HttpEntity.EMPTY, ProcessInstanceDto.class);
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    assertNotNull(entity.getBody());

    // find the process instance
    final ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(entity.getBody().getId()).singleResult();
    assertEquals(processInstance.getProcessInstanceId(), entity.getBody().getId());
  }

  @Test
  public void multipartFileUploadCamundaRestIsWorking() throws Exception {
    final String variableName = "testvariable";
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("TestProcess");
    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    map.add("data", new ClassPathResource("/bpmn/test.bpmn"));
    map.add("valueType", "File");
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    headers.setContentDispositionFormData("data", "test.bpmn");

    HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
    ResponseEntity<String> exchange = testRestTemplate.exchange("/rest/engine/{enginename}/process-instance/{id}/variables/{variableName}/data",
        HttpMethod.POST, requestEntity, String.class, camundaBpmProperties.getProcessEngineName(), processInstance.getId(), variableName);

    assertEquals(HttpStatus.NO_CONTENT, exchange.getStatusCode());

    VariableInstance variableInstance = runtimeService.createVariableInstanceQuery().processInstanceIdIn(processInstance.getId()).variableName(variableName)
        .singleResult();
    ByteArrayInputStream byteArrayInputStream = (ByteArrayInputStream) variableInstance.getValue();
    assertTrue(byteArrayInputStream.available() > 0);
  }

  @Test
  public void fetchAndLockExternalTaskWithLongPollingIsRunning() throws Exception {

    String requestJson = "{"
      + "  \"workerId\":\"aWorkerId\","
      + "  \"maxTasks\":2,"
      + "  \"topics\":"
      + "      [{\"topicName\": \"aTopicName\","
      + "      \"lockDuration\": 10000"
      + "      }]"
      + "}";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> requestEntity = new HttpEntity<String>(requestJson, headers);
    ResponseEntity<String> entity = testRestTemplate.postForEntity("/rest/engine/{enginename}/external-task/fetchAndLock", requestEntity, String.class,
      camundaBpmProperties.getProcessEngineName());
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    assertEquals("[]", entity.getBody());
  }

}
