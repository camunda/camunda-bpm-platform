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
package org.camunda.bpm.run.example.invoice;

import jakarta.annotation.PostConstruct;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.example.invoice.InvoiceApplicationHelper;
import org.camunda.bpm.example.invoice.InvoiceProcessApplication;
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.camunda.bpm.spring.boot.starter.event.PostDeployEvent;
import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@ConditionalOnProperty(name = "enabled", havingValue = "true", prefix = CamundaBpmProperties.PREFIX + ".run.example")
@Configuration
@EnableProcessApplication("invoiceProcessApplicationSpringBoot")
public class Application implements WebMvcConfigurer {

  private static final Logger LOG = LoggerFactory.getLogger(Application.class);

  @Autowired
  protected ProcessEngine processEngine;

  protected InvoiceProcessApplication invoicePa = new InvoiceProcessApplication();

  @PostConstruct
  public void deployInvoice() {
    LOG.info("Invoice example started, creating deployment");
    InvoiceApplicationHelper.createDeployment("invoiceProcessApplicationSpringBoot", processEngine, invoicePa.getClass().getClassLoader(), invoicePa.getReference());
  }

  @EventListener
  public void onPostDeploy(PostDeployEvent event) {
    LOG.info("Starting invoice example instance");
    invoicePa.startFirstProcess(event.getProcessEngine());
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/forms/**").addResourceLocations("classpath:/forms/");
    registry.addResourceHandler("/camunda-invoice/**").addResourceLocations("classpath:/camunda-invoice/");
  }

}
