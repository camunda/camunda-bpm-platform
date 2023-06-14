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
package org.camunda.bpm.qa;

import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.example.invoice.InvoiceProcessApplication;
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.camunda.bpm.spring.boot.starter.event.PostDeployEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EnableProcessApplication("myProcessApplication")
public class Application {

  @Autowired
  protected ProcessEngine processEngine;

  protected InvoiceProcessApplication invoicePa = new InvoiceProcessApplication();

  public static void main(String[] args) {
    // Avoid resetting URL stream handler factory
    TomcatURLStreamHandlerFactory.disable();

    SpringApplication.run(Application.class, args);
  }

  @PostConstruct
  public void deployInvoice() {
    ClassLoader classLoader = invoicePa.getClass().getClassLoader();

    processEngine.getRepositoryService()
      .createDeployment()
        .addInputStream("invoice.v1.bpmn", classLoader.getResourceAsStream("invoice.v1.bpmn"))
        .addInputStream("reviewInvoice.bpmn", classLoader.getResourceAsStream("reviewInvoice.bpmn"))
      .deploy();
  }

  @EventListener
  public void onPostDeploy(PostDeployEvent event) {
    invoicePa.startFirstProcess(event.getProcessEngine());
  }

}
