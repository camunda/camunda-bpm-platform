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
package org.camunda.bpm.qa.upgrade.scenarios7190.variables;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.variable.serializer.VariableSerializers;
import org.camunda.bpm.engine.variable.type.ValueType;

public class JPAVariablesSerializerPlugin extends AbstractProcessEnginePlugin {

  @Override
  public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    EntityManagerFactory jpaEntityManagerFactory = Persistence.createEntityManagerFactory("activiti-jpa-pu");
    processEngineConfiguration.getSessionFactories().put(EntityManagerSession.class,
        new EntityManagerSessionFactory(jpaEntityManagerFactory, true, true));

    VariableSerializers variableSerializers = processEngineConfiguration.getVariableSerializers();
    int index = variableSerializers.getSerializerIndexByName(ValueType.BYTES.getName());
    if (index > -1) {
      variableSerializers.addSerializer(new JPAVariableSerializer(), index);
    } else {
      variableSerializers.addSerializer(new JPAVariableSerializer());
    }

//    EntityManager manager = jpaEntityManagerFactory.createEntityManager();
//    manager.getTransaction().begin();
//
//    FieldAccessJPAEntity entityToQuery = new FieldAccessJPAEntity();
//    entityToQuery.setId(1L);
//    entityToQuery.setMyValue("value1");
//    manager.persist(entityToQuery);
//
//    manager.flush();
//    manager.getTransaction().commit();
//    manager.close();
  }
}
