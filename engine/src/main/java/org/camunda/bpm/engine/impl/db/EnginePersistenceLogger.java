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
package org.camunda.bpm.engine.impl.db;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.SuspendedEntityInteractionException;
import org.camunda.bpm.engine.WrongDbException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.db.entitymanager.cache.CachedDbEntity;
import org.camunda.bpm.engine.impl.db.entitymanager.cache.DbEntityState;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.util.ClassNameUtil;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

/**
 * @author Stefan Hentschel.
 */
public class EnginePersistenceLogger extends ProcessEngineLogger {

  protected static final String HINT_TEXT = "Hint: Set <property name=\"databaseSchemaUpdate\" to value=\"true\" or " +
                                            "value=\"create-drop\" (use create-drop for testing only!) in bean " +
                                            "processEngineConfiguration in camunda.cfg.xml for automatic schema creation";

  protected String buildStringFromList(Collection<?> list, Boolean isSQL) {
    StringBuilder message = new StringBuilder();
    message.append("[");
    message.append("\n");
    for( Object object : list ) {
      message.append("  ");
      if(isSQL) {
        message.append("SQL: ");
      }
      message.append(object.toString());
      message.append("\n");
    }
    message.append("]");

    return message.toString();
  }

  private String buildStringFromMap(Map<String, ?> map) {
    StringBuilder message = new StringBuilder();
    message.append("[");
    message.append("\n");
    for( Map.Entry<String, ?> entry : map.entrySet() ) {
      message.append("  ");
      message.append(entry.getKey());
      message.append(": ");
      message.append(entry.getValue().toString());
      message.append("\n");
    }
    message.append("]");
    return message.toString();
  }

  public <T extends DbEntity> ProcessEngineException entityCacheLookupException(Class<T> type, String id,
      Class<? extends DbEntity> entity, Throwable cause) {
    return new ProcessEngineException(exceptionMessage(
      "001",
      "Could not lookup entity of type '{}' and id '{}': found entity of type '{}'.",
      type,
      id,
      entity
    ), cause);
  }

  public ProcessEngineException entityCacheDuplicateEntryException(String currentState, String id,
      Class<? extends DbEntity> entityClass, DbEntityState foundState) {
    return new ProcessEngineException(exceptionMessage(
      "002",
      "Cannot add {} entity with id '{}' and type '{}' into cache. An entity with the same id and type is already in state '{}'",
      currentState,
      id,
      entityClass,
      foundState
    ));
  }

  public ProcessEngineException alreadyMarkedEntityInEntityCacheException(String id,
      Class<? extends DbEntity> entityClass, DbEntityState state) {

    return new ProcessEngineException(exceptionMessage(
      "003",
      "Inserting an entity with Id '{}' and type '{}' which is already marked with state '{}'",
      id,
      entityClass,
      state
    ));
  }

  public ProcessEngineException flushDbOperationException(List<DbOperation> operationsToFlush, DbOperation operation,
      Throwable cause) {

    return new ProcessEngineException(exceptionMessage(
      "004",
      "Exception while executing Database Operation '{}' with message '{}'. Flush summary: \n {}",
      operation.toString(),
      cause.getMessage(),
      buildStringFromList(operationsToFlush, false)
    ), cause);
  }

  public OptimisticLockingException concurrentUpdateDbEntityException(DbOperation operation) {
    return new OptimisticLockingException(exceptionMessage(
      "005",
      "Execution of '{}' failed. Entity was updated by another transaction concurrently.",
      operation
    ));
  }

  public void flushedCacheState(List<CachedDbEntity> cachedEntities) {
    if(isDebugEnabled()) {
      logDebug("006", "Cache state after flush: {}", buildStringFromList(cachedEntities, false));
    }

  }

  public ProcessEngineException mergeDbEntityException(DbEntity entity) {
    return new ProcessEngineException(exceptionMessage("007", "Cannot merge DbEntity '{}' without id", entity));
  }

  public void databaseFlushSummary(Collection<DbOperation> operations) {
   if(isDebugEnabled()) {
     logDebug("008", "Flush Summary: {}", buildStringFromList(operations, false));
   }
  }

  public void executeDatabaseOperation(String operationType, Object parameter) {
    if(isDebugEnabled()) {

      String message;
      if(parameter != null) {
        message = parameter.toString();
      } else {
        message = "null";
      }

      if(parameter instanceof DbEntity) {
        DbEntity dbEntity = (DbEntity) parameter;
        message = ClassNameUtil.getClassNameWithoutPackage(dbEntity) + "[id=" + dbEntity.getId() + "]";
      }

      logDebug("009", "SQL operation: '{}'; Entity: '{}'", operationType, message);
    }
  }

  public void executeDatabaseBulkOperation(String operationType, String statement, Object parameter) {
    logDebug("010", "SQL bulk operation: '{}'; Statement: '{}'; Parameter: '{}'", operationType, statement, parameter);
  }

  public void fetchDatabaseTables(String source, List<String> tableNames) {
    if(isDebugEnabled()) {
      logDebug(
        "011",
        "Retrieving process engine tables from: '{}'. Retrieved tables: {}",
        source,
        buildStringFromList(tableNames, false)
      );
    }
  }

  public void missingSchemaResource(String resourceName, String operation) {
    logDebug("012", "There is no schema resource '{}' for operation '{}'.", resourceName, operation);
  }

  public ProcessEngineException missingSchemaResourceException(String resourceName, String operation) {
    return new ProcessEngineException(
      exceptionMessage("013", "There is no schema resource '{}' for operation '{}'.", resourceName, operation));
  }

  public ProcessEngineException missingSchemaResourceFileException(String fileName, Throwable cause) {
    return new ProcessEngineException(
      exceptionMessage("014", "Cannot find schema resource file with name '{}'",fileName), cause);
  }

  public void failedDatabaseOperation(String operation, String statement, Throwable cause) {
    logError(
      "015",
      "Problem during schema operation '{}' with statement '{}'. Cause: '{}'",
      operation,
      statement,
      cause.getMessage()
    );
  }

  public void performedDatabaseOperation(String operation, String component, String resourceName, List<String> logLines) {
    logInfo(
      "016",
      "Performed operation '{}' on component '{}' with resource '{}': {}",
      operation,
      component,
      resourceName,
      buildStringFromList(logLines, true));
  }

  public void successfulDatabaseOperation(String operation, String component) {
    logDebug("Database schema operation '{}' for component '{}' was successful.", operation, component);
  }

  public ProcessEngineException performDatabaseOperationException(String operation, String sql, Throwable cause) {
    return new ProcessEngineException(exceptionMessage(
      "017",
      "Could not perform operation '{}' on database schema for SQL Statement: '{}'.",
      operation,
      sql
    ), cause);
  }

  public ProcessEngineException checkDatabaseTableException(Throwable cause) {
    return new ProcessEngineException(
      exceptionMessage("018", "Could not check if tables are already present using metadata."), cause);
  }

  public ProcessEngineException getDatabaseTableNameException(Throwable cause) {
    return new ProcessEngineException(exceptionMessage("019", "Unable to fetch process engine table names."), cause);
  }

  public ProcessEngineException missingRelationMappingException(String relation) {
    return new ProcessEngineException(
      exceptionMessage("020", "There is no mapping for the relation '{}' registered.", relation));
  }

  public ProcessEngineException databaseHistoryLevelException(String level) {
    return new ProcessEngineException(
      exceptionMessage("021", "historyLevel '{}' is higher then 'none' and dbHistoryUsed is set to false.", level));
  }

  public ProcessEngineException invokeSchemaResourceToolException(int length) {
    return new ProcessEngineException(exceptionMessage(
      "022",
      "Schema resource tool was invoked with '{}' parameters." +
      "Schema resource tool must be invoked with exactly 2 parameters:" +
      "\n - 1st parameter is the process engine configuration file," +
      "\n - 2nd parameter is the schema resource file name",
      length
    ));
  }

  public ProcessEngineException loadModelException(String type, String modelName, String id, Throwable cause) {
    return new ProcessEngineException(exceptionMessage(
      "023",
      "Could not load {} Model for {} definition with id '{}'.",
      type,
      modelName,
      id
    ), cause);
  }

  public void removeEntryFromDeploymentCacheFailure(String modelName, String id, Throwable cause) {
    logWarn(
      "024",
      "Could not remove {} definition with id '{}' from the cache. Reason: '{}'",
      modelName,
      id,
      cause.getMessage()
    );
  }


  public ProcessEngineException engineAuthorizationTypeException(int usedType, int global, int grant, int revoke) {
    return new ProcessEngineException(exceptionMessage(
      "025",
      "Unrecognized authorization type '{}'. Must be one of ['{}', '{}', '{}']",
      usedType,
      global,
      grant,
      revoke
    ));
  }

  public IllegalStateException permissionStateException(String methodName, String type) {
    return new IllegalStateException(
      exceptionMessage("026", "Method '{}' cannot be used for authorization with type '{}'.", methodName, type));
  }

  public ProcessEngineException notUsableGroupIdForGlobalAuthorizationException() {
    return new ProcessEngineException(exceptionMessage("027", "Cannot use 'groupId' for GLOBAL authorization"));
  }

  public ProcessEngineException illegalValueForUserIdException(String id, String expected) {
    return new ProcessEngineException(
      exceptionMessage("028", "Illegal value '{}' for userId for GLOBAL authorization. Must be '{}'", id, expected));
  }

  public AuthorizationException notAMemberException(String id, String group) {
    return new AuthorizationException(
      exceptionMessage("029", "The user with id '{}' is not a member of the group with id '{}'", id, group));
  }

  public void createChildExecution(ExecutionEntity child, ExecutionEntity parent) {
    if(isDebugEnabled()) {
      logDebug("030", "Child execution '{}' created with parent '{}'.", child.toString(), parent.toString());
    }
  }

  public void initializeExecution(ExecutionEntity entity) {
    logDebug("031", "Initializing execution '{}'", entity.toString());
  }

  public void initializeTimerDeclaration(ExecutionEntity entity) {
    logDebug("032", "Initializing timer declaration '{}'", entity.toString());
  }

  public ProcessEngineException requiredAsyncContinuationException(String id) {
    return new ProcessEngineException(exceptionMessage(
      "033",
      "Asynchronous Continuation for activity with id '{}' requires a message job declaration",
      id
    ));
  }

  public ProcessEngineException restoreProcessInstanceException(ExecutionEntity entity) {
    return new ProcessEngineException(exceptionMessage(
      "034",
      "Can only restore process instances. This method must be called on a process instance execution but was called on '{}'",
      entity.toString()
    ));

  }

  public ProcessEngineException executionNotFoundException(String id) {
    return new ProcessEngineException(exceptionMessage("035", "Unable to find execution for id '{}'", id));
  }

  public ProcessEngineException castModelInstanceException(ModelElementInstance instance, String toElement, String type,
      String namespace, Throwable cause) {

    return new ProcessEngineException(exceptionMessage(
      "036",
      "Cannot cast '{}' to '{}'. Element is of type '{}' with namespace '{}'.",
      instance,
      toElement,
      type,
      namespace
    ), cause);
  }

  public BadUserRequestException requestedProcessInstanceNotFoundException(String id) {
    return new BadUserRequestException(exceptionMessage("037", "No process instance found for id '{}'", id));
  }

  public NotValidException queryExtensionException(String extendedClassName, String extendingClassName) {
    return new NotValidException(exceptionMessage(
      "038",
      "Unable to extend a query of class '{}' by a query of class '{}'.",
      extendedClassName,
      extendingClassName
    ));
  }

  public ProcessEngineException unsupportedResourceTypeException(String type) {
    return new ProcessEngineException(exceptionMessage("039", "Unsupported resource type '{}'", type));
  }

  public ProcessEngineException serializerNotDefinedException(Object entity) {
    return new ProcessEngineException(exceptionMessage("040", "No serializer defined for variable instance '{}'", entity));
  }

  public ProcessEngineException serializerOutOfContextException() {
    return new ProcessEngineException(exceptionMessage("041", "Cannot work with serializers outside of command context."));
  }

  public ProcessEngineException taskIsAlreadyAssignedException(String usedId, String foundId) {
    return new ProcessEngineException(
      exceptionMessage("042", "Cannot assign '{}' to a task assignment that has already '{}' set.", usedId, foundId));
  }

  public SuspendedEntityInteractionException suspendedEntityException(String type, String id) {
    return new SuspendedEntityInteractionException(exceptionMessage("043", "{} with id '{}' is suspended.", type, id));
  }

  public ProcessEngineException updateUnrelatedProcessDefinitionEntityException() {
    return new ProcessEngineException(exceptionMessage("044", "Cannot update entity from an unrelated process definition"));
  }

  public ProcessEngineException toManyProcessDefinitionsException(int count, String key, Integer version) {
    return new ProcessEngineException(exceptionMessage(
      "045",
      "There are '{}' results for a process definition with key '{}' and version '{}'.",
      count,
      key,
      version
    ));
  }

  public ProcessEngineException notAllowedIdException(String id) {
    return new ProcessEngineException(
      exceptionMessage("046", "Cannot set id '{}'. Only the provided id generation is allowed for properties.", id));
  }

  public void countRowsPerProcessEngineTable(Map<String, Long> map) {
    if(isDebugEnabled()) {
      logDebug("047", "Number of rows per process engine table: {}", buildStringFromMap(map));
    }
  }

  public ProcessEngineException countTableRowsException(Throwable cause) {
    return new ProcessEngineException(exceptionMessage("048", "Could not fetch table counts."), cause);
  }

  public void selectTableCountForTable(String name) {
    logDebug("049", "Selecting table count for table with name '{}'", name);
  }

  public ProcessEngineException retrieveMetadataException(Throwable cause) {
    return new ProcessEngineException(
      exceptionMessage("050", "Could not retrieve database metadata. Reason: '{}'", cause.getMessage()), cause);
  }

  public ProcessEngineException invokeTaskListenerException(Throwable cause) {
    return new ProcessEngineException(exceptionMessage(
      "051",
      "There was an exception while invoking the TaskListener. Message: '{}'",
      cause.getMessage()
    ), cause);
  }

  public BadUserRequestException uninitializedFormKeyException() {
    return new BadUserRequestException(exceptionMessage(
      "052",
      "The form key is not initialized. You must call initializeFormKeys() on the task query before you can " +
      "retrieve the form key."
    ));
  }

  public ProcessEngineException disabledHistoryException() {
    return new ProcessEngineException(exceptionMessage("053", "History is not enabled."));
  }

  public ProcessEngineException instantiateSessionException(String name, Throwable cause) {
    return new ProcessEngineException(exceptionMessage(
      "054",
      "Could not instantiate class '{}'. Message: '{}'",
      name,
      cause.getMessage()
    ), cause);
  }

  public WrongDbException wrongDbVersionException(String version, String dbVersion) {
    return new WrongDbException(exceptionMessage(
      "055",
      "Version mismatch: activiti library version is '{}' and db version is '{}'. " +
      HINT_TEXT,
      version,
      dbVersion
    ), version, dbVersion);
  }

  public ProcessEngineException missingTableException(List<String> components) {
    return new ProcessEngineException(exceptionMessage(
      "056",
      "Tables are missing for the following components: {}",
      buildStringFromList(components, false)
    ));
  }

  public ProcessEngineException missingActivitiTablesException() {
    return new ProcessEngineException(exceptionMessage(
      "057",
      "There are no activiti tables in the database." +
        HINT_TEXT
    ));
  }

  public ProcessEngineException unableToFetchDbSchemaVersion(Throwable cause) {
    return new ProcessEngineException(exceptionMessage("058", "Could not fetch the database schema version."), cause);
  }

  public void failedTofetchVariableValue(Throwable cause) {
    logDebug("059", "Could not fetch value for variable.", cause);
  }

  public ProcessEngineException historicDecisionInputInstancesNotFetchedException() {
    return new ProcessEngineException(exceptionMessage(
        "060",
        "The input instances for the historic decision instance are not fetched. You must call 'includeInputs()' on the query to enable fetching."
        ));
  }

  public ProcessEngineException historicDecisionOutputInstancesNotFetchedException() {
    return new ProcessEngineException(exceptionMessage(
        "061",
        "The output instances for the historic decision instance are not fetched. You must call 'includeOutputs()' on the query to enable fetching."
        ));
  }
}
