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
package org.camunda.bpm.spring.boot.starter.configuration.impl;

import javax.persistence.EntityManagerFactory;

import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.configuration.CamundaJpaConfiguration;
import org.camunda.bpm.spring.boot.starter.property.JpaProperty;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultJpaConfiguration extends AbstractCamundaConfiguration implements CamundaJpaConfiguration {

  @Autowired(required = false)
  private EntityManagerFactory jpaEntityManagerFactory;

  @Override
  public void preInit(SpringProcessEngineConfiguration configuration) {
    final JpaProperty jpa = camundaBpmProperties.getJpa();

    configuration.setJpaPersistenceUnitName(jpa.getPersistenceUnitName());
    if (jpaEntityManagerFactory != null) {
      configuration.setJpaEntityManagerFactory(jpaEntityManagerFactory);
    }
    configuration.setJpaCloseEntityManager(jpa.isCloseEntityManager());
    configuration.setJpaHandleTransaction(jpa.isHandleTransaction());
  }
}
