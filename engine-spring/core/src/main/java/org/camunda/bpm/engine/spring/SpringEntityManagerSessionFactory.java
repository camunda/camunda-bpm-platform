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
package org.camunda.bpm.engine.spring;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.camunda.bpm.engine.impl.interceptor.Session;
import org.camunda.bpm.engine.impl.interceptor.SessionFactory;
import org.camunda.bpm.engine.impl.variable.serializer.jpa.EntityManagerSession;
import org.camunda.bpm.engine.impl.variable.serializer.jpa.EntityManagerSessionImpl;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;


/**
 * Session Factory for {@link EntityManagerSession}.
 *
 * Must be used when the {@link EntityManagerFactory} is managed by Spring.
 * This implementation will retrieve the {@link EntityManager} bound to the
 * thread by Spring in case a transaction already started.
 *
 * @author Joram Barrez
 */
public class SpringEntityManagerSessionFactory implements SessionFactory {

  protected EntityManagerFactory entityManagerFactory;
  protected boolean handleTransactions;
  protected boolean closeEntityManager;

  public SpringEntityManagerSessionFactory(Object entityManagerFactory, boolean handleTransactions, boolean closeEntityManager) {
    this.entityManagerFactory = (EntityManagerFactory) entityManagerFactory;
    this.handleTransactions = handleTransactions;
    this.closeEntityManager = closeEntityManager;
  }

  public Class< ? > getSessionType() {
    return EntityManagerFactory.class;
  }

  public Session openSession() {
    EntityManager entityManager = EntityManagerFactoryUtils.getTransactionalEntityManager(entityManagerFactory);
    if (entityManager == null) {
      return new EntityManagerSessionImpl(entityManagerFactory, handleTransactions, closeEntityManager);
    }
    return new EntityManagerSessionImpl(entityManagerFactory, entityManager, false, false);
  }

}
