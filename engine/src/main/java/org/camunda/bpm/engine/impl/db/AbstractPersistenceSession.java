/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.engine.impl.db;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.WrongDbException;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbBulkOperation;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbEntityOperation;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;

/**
 * @author Sebastian Menski
 */
public abstract class AbstractPersistenceSession implements PersistenceSession {

  public void executeDbOperation(DbOperation operation) {
    switch(operation.getOperationType()) {

      case INSERT:
        insertEntity((DbEntityOperation) operation);
        break;

      case DELETE:
        deleteEntity((DbEntityOperation) operation);
        break;
      case DELETE_BULK:
        deleteBulk((DbBulkOperation) operation);
        break;

      case UPDATE:
        updateEntity((DbEntityOperation) operation);
        break;
      case UPDATE_BULK:
        updateBulk((DbBulkOperation) operation);
        break;

    }
  }

  protected abstract void insertEntity(DbEntityOperation operation);

  protected abstract void deleteEntity(DbEntityOperation operation);

  protected abstract void deleteBulk(DbBulkOperation operation);

  protected abstract void updateEntity(DbEntityOperation operation);

  protected abstract void updateBulk(DbBulkOperation operation);

  protected abstract String getDbVersion();

  public void dbSchemaCreate() {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();

    int configuredHistoryLevel = processEngineConfiguration.getHistoryLevel();
    if ( (!processEngineConfiguration.isDbHistoryUsed())
         && (configuredHistoryLevel>ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE)
       ) {
      throw new ProcessEngineException("historyLevel config is higher then 'none' and dbHistoryUsed is set to false");
    }

    if (isEngineTablePresent()) {
      String dbVersion = getDbVersion();
      if (!ProcessEngine.VERSION.equals(dbVersion)) {
        throw new WrongDbException(ProcessEngine.VERSION, dbVersion);
      }
    } else {
      dbSchemaCreateEngine();
    }

    if (processEngineConfiguration.isDbHistoryUsed()) {
      dbSchemaCreateHistory();
    }

    if (processEngineConfiguration.isDbIdentityUsed()) {
      dbSchemaCreateIdentity();
    }

    if (processEngineConfiguration.isCmmnEnabled()) {
      dbSchemaCreateCmmn();
    }
  }

  protected abstract void dbSchemaCreateIdentity();

  protected abstract void dbSchemaCreateHistory();

  protected abstract void dbSchemaCreateEngine();

  protected abstract void dbSchemaCreateCmmn();

  public void dbSchemaDrop() {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();

    if (processEngineConfiguration.isCmmnEnabled()) {
      dbSchemaDropCmmn();
    }

    dbSchemaDropEngine();

    if (processEngineConfiguration.isDbHistoryUsed()) {
      dbSchemaDropHistory();
    }
    if (processEngineConfiguration.isDbIdentityUsed()) {
      dbSchemaDropIdentity();
    }
  }

  protected abstract void dbSchemaDropIdentity();

  protected abstract void dbSchemaDropHistory();

  protected abstract void dbSchemaDropEngine();

  protected abstract void dbSchemaDropCmmn();

  public void dbSchemaPrune() {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    if (isHistoryTablePresent() && !processEngineConfiguration.isDbHistoryUsed()) {
      dbSchemaDropHistory();
    }
    if (isIdentityTablePresent() && !processEngineConfiguration.isDbIdentityUsed()) {
      dbSchemaDropIdentity();
    }
    if (isCaseDefinitionTablePresent() && !processEngineConfiguration.isCmmnEnabled()) {
      dbSchemaDropCmmn();
    }
  }

  public abstract boolean isEngineTablePresent();

  public abstract boolean isHistoryTablePresent();

  public abstract boolean isIdentityTablePresent();

  public abstract boolean isCaseDefinitionTablePresent();

  public void dbSchemaUpdate() {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();

    if (!isEngineTablePresent()) {
      dbSchemaCreateEngine();
    }

    if (!isHistoryTablePresent() && processEngineConfiguration.isDbHistoryUsed()) {
      dbSchemaCreateHistory();
    }

    if (!isIdentityTablePresent() && processEngineConfiguration.isDbIdentityUsed()) {
      dbSchemaCreateIdentity();
    }

    if (!isCaseDefinitionTablePresent() && processEngineConfiguration.isCmmnEnabled()) {
      dbSchemaCreateCmmn();
    }

  }
}
