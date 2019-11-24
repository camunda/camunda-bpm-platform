/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm;

import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.camunda.bpm.client.variable.ClientValues;
import org.camunda.bpm.client.variable.value.JsonValue;
import org.camunda.bpm.client.variable.value.XmlValue;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class App {
  
  public static void main(String... args) throws JAXBException {
    ExternalTaskClient client = ExternalTaskClient.create()
        .baseUrl("http://localhost:8080/engine-rest/")
        .asyncResponseTimeout(10000)
        .disableBackoffStrategy()
        .disableAutoFetching()
        .maxTasks(1)
        .build();
    
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    
    Marshaller customerMarshaller = JAXBContext.newInstance(Customer.class).createMarshaller();
    customerMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    
    TopicSubscriptionBuilder xmlSubscriptionBuilder = client.subscribe("xmlCustomerCreation")
      .lockDuration(20000)
      .handler((externalTask, externalTaskService) -> {
        Customer customer = createCustomerFromVariables(externalTask);
        try {
          StringWriter stringWriter = new StringWriter();
          customerMarshaller.marshal(customer, stringWriter);
          String customerXml = stringWriter.toString();
          VariableMap variables = Variables.createVariables().putValue("customer", ClientValues.xmlValue(customerXml));
          externalTaskService.complete(externalTask, variables);
        } catch (JAXBException e) {
          e.printStackTrace();
        }
      });
    
    TopicSubscriptionBuilder jsonSubscriptionBuilder = client.subscribe("jsonCustomerCreation")
      .lockDuration(20000)
      .handler((externalTask, externlTaskService) -> {
        Customer customer = createCustomerFromVariables(externalTask);
        try {
          String customerJson = objectMapper.writeValueAsString(customer);
          VariableMap variables = Variables.createVariables().putValue("customer", ClientValues.jsonValue(customerJson));
          externlTaskService.complete(externalTask, variables);
        } catch (JsonProcessingException e) {
          e.printStackTrace();
        }
        
      });
    
    TopicSubscriptionBuilder readSubscrptionBuilder = client.subscribe("customerReading")
      .lockDuration(20000)
      .handler((externalTask, externalTaskService) -> {
        String dataformat = externalTask.getVariable("dataFormat");
        if ("json".equals(dataformat)) {
          JsonValue jsonCustomer = externalTask.getVariableTyped("customer");
          System.out.println("Customer json: " + jsonCustomer.getValue());
        } else if ("xml".equals(dataformat)) {
          XmlValue xmlCustomer = externalTask.getVariableTyped("customer");
          System.out.println("Customer xml: " + xmlCustomer.getValue());
        }
        externalTaskService.complete(externalTask);
      });
    
    client.start();
    xmlSubscriptionBuilder.open();
    jsonSubscriptionBuilder.open();
    readSubscrptionBuilder.open();
  }

  private static Customer createCustomerFromVariables(ExternalTask externalTask) {
    Customer customer = new Customer();
    customer.setFirstName(externalTask.getVariable("firstname"));
    customer.setLastName(externalTask.getVariable("lastname"));
    customer.setGender(externalTask.getVariable("gender"));
    Long age = externalTask.getVariable("age");
    customer.setAge(age.intValue());
    customer.setIsValid(externalTask.getVariable("isValid"));
    customer.setValidationDate(externalTask.getVariable("validationDate"));
    return customer;
  }
}
