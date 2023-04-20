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
package org.camunda.bpm.integrationtest.deployment.ear.beans;

import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.ProcessApplicationInterface;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

/**
 * @author Tassilo Weidner
 */
@Singleton
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@ProcessApplication(deploymentDescriptors = {"deployment-descriptor-with-custom-filename.xml"})
@Local(ProcessApplicationInterface.class)
// Using fully-qualified class name instead of import statement to allow for automatic Jakarta transformation
public class AnnotatedEjbPa extends org.camunda.bpm.application.impl.EjbProcessApplication {

  @PostConstruct
  public void start() {
    deploy();
  }

  @PreDestroy
  public void stop() {
    undeploy();
  }

}
