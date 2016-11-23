/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.SchemaOperationsCommand;
import org.camunda.bpm.engine.impl.bpmn.deployer.BpmnDeployer;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParser;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.DetermineHistoryLevelCmd;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.db.PersistenceSession;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.parser.HistoryParseListener;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.Deployer;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyEntity;

import java.util.List;

/**
 * @author Tom Baeyens
 * @author Roman Smirnov
 * @author Sebastian Menski
 * @author Daniel Meyer
 */
public final class SchemaOperationsProcessEngineBuild implements SchemaOperationsCommand {

  private final static EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  @Override
  public Void execute(CommandContext commandContext) {
    String databaseSchemaUpdate = Context.getProcessEngineConfiguration().getDatabaseSchemaUpdate();
    PersistenceSession persistenceSession = commandContext.getSession(PersistenceSession.class);
    if (ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_DROP_CREATE.equals(databaseSchemaUpdate)) {
      try {
        persistenceSession.dbSchemaDrop();
      } catch (RuntimeException e) {
        // ignore
      }
    }
    if (ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP.equals(databaseSchemaUpdate)
      || ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_DROP_CREATE.equals(databaseSchemaUpdate)
      || ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_CREATE.equals(databaseSchemaUpdate)
      ) {
      persistenceSession.dbSchemaCreate();
    } else if (ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE.equals(databaseSchemaUpdate)) {
      persistenceSession.dbSchemaCheckVersion();
    } else if (ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE.equals(databaseSchemaUpdate)) {
      persistenceSession.dbSchemaUpdate();
    }


    DbEntityManager entityManager = commandContext.getSession(DbEntityManager.class);
    checkHistoryLevel(entityManager);
    checkDeploymentLockExists(entityManager);

    return null;
  }

  public static void dbCreateHistoryLevel(DbEntityManager entityManager) {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    HistoryLevel configuredHistoryLevel = processEngineConfiguration.getHistoryLevel();

    insertHistoryLevel(entityManager, configuredHistoryLevel);
  }

  public static void insertHistoryLevel(DbEntityManager entityManager, HistoryLevel historyLevel) {
    PropertyEntity property = new PropertyEntity("historyLevel", Integer.toString(historyLevel.getId()));
    entityManager.insert(property);
    LOG.creatingHistoryLevelPropertyInDatabase(historyLevel);
  }


  /**
   * @param entityManager entoty manager for db query
   * @return Integer value representing the history level or <code>null</code> if none found
   */
  public static Integer databaseHistoryLevel(DbEntityManager entityManager) {

    try {
      PropertyEntity historyLevelProperty = entityManager.selectById(PropertyEntity.class, "historyLevel");
      return historyLevelProperty != null ? new Integer(historyLevelProperty.getValue()) : null;
    } catch (Exception e) {
      LOG.couldNotSelectHistoryLevel(e.getMessage());
      return null;
    }

  }

  public void checkHistoryLevel(DbEntityManager entityManager) {
    ProcessEngineConfigurationImpl engineConfiguration = Context.getProcessEngineConfiguration();


    HistoryLevel databaseHistoryLevel = new DetermineHistoryLevelCmd(engineConfiguration.getHistoryLevels())
      .execute(Context.getCommandContext());
    determineAutoHistoryLevel(engineConfiguration, databaseHistoryLevel);

    HistoryLevel configuredHistoryLevel = engineConfiguration.getHistoryLevel();

    if (databaseHistoryLevel == null) {
      LOG.noHistoryLevelPropertyFound();
      dbCreateHistoryLevel(entityManager);
    } else {
      if (!((Integer) configuredHistoryLevel.getId()).equals(databaseHistoryLevel.getId())) {
        throw new ProcessEngineException("historyLevel mismatch: configuration says " + configuredHistoryLevel
          + " and database says " + databaseHistoryLevel);
      }
    }


    // add ParseListener depending on history level
    for (Deployer deployer : engineConfiguration.getDeployers()) {
      if (deployer instanceof BpmnDeployer) {
        BpmnParser parser = ((BpmnDeployer) deployer).getBpmnParser();
        BpmnParseListener historyParseListener = findHistoryParseListener(parser.getParseListeners());
        if (historyParseListener == null) {
          parser.getParseListeners().add(new HistoryParseListener(engineConfiguration.getHistoryLevel(), engineConfiguration.getHistoryEventProducer()));
        }
      }
    }

    // init dmn engine again for historyLevel
    engineConfiguration.setDmnEngine(null);
    engineConfiguration.initDmnEngine();
  }

  protected void determineAutoHistoryLevel(ProcessEngineConfigurationImpl engineConfiguration, HistoryLevel databaseHistoryLevel) {
    HistoryLevel configuredHistoryLevel = engineConfiguration.getHistoryLevel();

    if (configuredHistoryLevel == null
      && ProcessEngineConfiguration.HISTORY_AUTO.equals(engineConfiguration.getHistory())) {

      // automatically determine history level or use default AUDIT
      if (databaseHistoryLevel != null) {
        engineConfiguration.setHistoryLevel(databaseHistoryLevel);
      } else {
        engineConfiguration.setHistoryLevel(engineConfiguration.getDefaultHistoryLevel());
      }
    }

  }

  private static BpmnParseListener findHistoryParseListener(List<BpmnParseListener> parseListeners) {
    for (BpmnParseListener listener : parseListeners) {
      if (listener instanceof HistoryParseListener) {
        return listener;
      }
    }
    return null;
  }

  public void checkDeploymentLockExists(DbEntityManager entityManager) {
    PropertyEntity deploymentLockProperty = entityManager.selectById(PropertyEntity.class, "deployment.lock");
    if (deploymentLockProperty == null) {
      LOG.noDeploymentLockPropertyFound();
    }
  }
}
