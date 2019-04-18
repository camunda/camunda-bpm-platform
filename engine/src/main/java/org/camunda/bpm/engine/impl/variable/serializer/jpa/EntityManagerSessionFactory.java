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
package org.camunda.bpm.engine.impl.variable.serializer.jpa;

import javax.persistence.EntityManagerFactory;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.interceptor.Session;
import org.camunda.bpm.engine.impl.interceptor.SessionFactory;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * @author Frederik Heremans
 */
public class EntityManagerSessionFactory implements SessionFactory {

  protected EntityManagerFactory entityManagerFactory;
  protected boolean handleTransactions;
  protected boolean closeEntityManager;

  public EntityManagerSessionFactory(Object entityManagerFactory, boolean handleTransactions, boolean closeEntityManager) {
    ensureNotNull("entityManagerFactory", entityManagerFactory);
    if (!(entityManagerFactory instanceof EntityManagerFactory)) {
      throw new ProcessEngineException("EntityManagerFactory must implement 'javax.persistence.EntityManagerFactory'");
    }

    this.entityManagerFactory = (EntityManagerFactory) entityManagerFactory;
    this.handleTransactions = handleTransactions;
    this.closeEntityManager = closeEntityManager;
  }

  public Class< ? > getSessionType() {
    return EntityManagerSession.class;
  }

  public Session openSession() {
    return new EntityManagerSessionImpl(entityManagerFactory, handleTransactions, closeEntityManager);
  }

  public EntityManagerFactory getEntityManagerFactory() {
    return entityManagerFactory;
  }
}
