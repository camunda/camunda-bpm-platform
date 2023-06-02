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
package org.camunda.bpm.qa.upgrade.variables;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.variable.serializer.jpa.EntityManagerSession;
import org.camunda.bpm.engine.impl.variable.serializer.jpa.EntityManagerSessionFactory;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.qa.upgrade.DescribesScenario;
import org.camunda.bpm.qa.upgrade.ScenarioSetup;

public class JpaEntitiesScenario {
  static org.camunda.bpm.qa.upgrade.variables.FieldAccessJPAEntity  simpleEntityFieldAccess;
//  static org.camunda.bpm.qa.upgrade.variables.PropertyAccessJPAEntity simpleEntityPropertyAccess;
//  static org.camunda.bpm.qa.upgrade.variables.SubclassFieldAccessJPAEntity subclassFieldAccess;
//  static org.camunda.bpm.qa.upgrade.variables.SubclassPropertyAccessJPAEntity subclassPropertyAccess;
  static EntityManagerFactory entityManagerFactory;

  @Deployment
  public static String deployOneTask() {
    return "org/camunda/bpm/qa/upgrade/variables/JpaEntitiesScenario.oneTaskProcess.bpmn20.xml";
  }

  @DescribesScenario("createJpaVariables")
  public static ScenarioSetup createJpaVariables() {
    return (engine, scenarioName) -> {
      EntityManagerSessionFactory entityManagerSessionFactory = (EntityManagerSessionFactory) ((ProcessEngineConfigurationImpl) engine.getProcessEngineConfiguration())
          .getSessionFactories()
          .get(EntityManagerSession.class);
      entityManagerFactory = entityManagerSessionFactory.getEntityManagerFactory();

      setupJPAEntities();

      Map<String, Object> variables = new HashMap<String, Object>();
      variables.put("simpleEntityFieldAccess", simpleEntityFieldAccess);
//      variables.put("simpleEntityPropertyAccess", simpleEntityPropertyAccess);
//      variables.put("subclassFieldAccess", subclassFieldAccess);
//      variables.put("subclassPropertyAccess", subclassPropertyAccess);

      //
      String id = engine.getRuntimeService().startProcessInstanceByKey("JPAVariableProcess", variables).getId();
      engine.getManagementService().setProperty("JpaEntitiesScenario.processInstanceId", id);

      entityManagerFactory.close();
      entityManagerFactory = null;
    };
  }

  public static void setupJPAEntities() {

    EntityManager manager = entityManagerFactory.createEntityManager();
    manager.getTransaction().begin();

    // Simple test data
    simpleEntityFieldAccess = new FieldAccessJPAEntity();
    simpleEntityFieldAccess.setId(1L);
    simpleEntityFieldAccess.setMyValue("value1");
    manager.persist(simpleEntityFieldAccess);

//    simpleEntityPropertyAccess = new PropertyAccessJPAEntity();
//    simpleEntityPropertyAccess.setId(1L);
//    simpleEntityPropertyAccess.setMyValue("value2");
//    manager.persist(simpleEntityPropertyAccess);
//
//    subclassFieldAccess = new SubclassFieldAccessJPAEntity();
//    subclassFieldAccess.setId(1L);
//    subclassFieldAccess.setValue("value3");
//    manager.persist(subclassFieldAccess);
//
//    subclassPropertyAccess = new SubclassPropertyAccessJPAEntity();
//    subclassPropertyAccess.setId(1L);
//    subclassPropertyAccess.setValue("value4");
//    manager.persist(subclassPropertyAccess);

    manager.flush();
    manager.getTransaction().commit();
    manager.close();
  }
}
