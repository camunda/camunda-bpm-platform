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
package org.camunda.bpm.engine.impl.cfg;

import javax.naming.NamingException;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;

/**
 * @author Daniel Meyer
 *
 */
public class ConfigurationLogger extends ProcessEngineLogger {

  public ProcessEngineException invalidConfigTransactionManagerIsNull() {
    return new ProcessEngineException(exceptionMessage(
        "001",
        "Property 'transactionManager' is null and 'transactionManagerJndiName' is not set. "
        + "Please set either the 'transactionManager' property or the 'transactionManagerJndiName' property."));
  }

  public ProcessEngineException invalidConfigCannotFindTransactionManger(String jndiName, NamingException e) {
    return new ProcessEngineException(exceptionMessage(
        "002",
        "Cannot lookup instance of Jta Transaction manager in JNDI using name '{}'", jndiName), e);
  }

  public void pluginActivated(String pluginName, String processEngineName) {
    logInfo(
        "003", "Plugin '{}' activated on process engine '{}'", pluginName, processEngineName);
  }

  public void debugDatabaseproductName(String databaseProductName) {
    logDebug(
        "004", "Database product name {}", databaseProductName);
  }

  public void debugDatabaseType(String databaseType) {
    logDebug(
        "005", "Database type {}", databaseType);
  }

  public void usingDeprecatedHistoryLevelVariable() {
    logWarn(
        "006", "Using deprecated history level 'variable'. " +
            "This history level is deprecated and replaced by 'activity'. " +
            "Consider using 'ACTIVITY' instead.");
  }

  public ProcessEngineException invalidConfigDefaultUserPermissionNameForTask(String defaultUserPermissionNameForTask, String[] validPermissionNames) {
    return new ProcessEngineException(exceptionMessage(
        "007",
        "Invalid value '{}' for configuration property 'defaultUserPermissionNameForTask'. Valid values are: '{}'", defaultUserPermissionNameForTask, validPermissionNames));
  }

  public ProcessEngineException invalidPropertyValue(String propertyName, String propertyValue) {
    return new ProcessEngineException(exceptionMessage(
        "008",
        "Invalid value '{}' for configuration property '{}'.", propertyValue, propertyName));
  }

  public ProcessEngineException invalidPropertyValue(String propertyName, String propertyValue, String reason) {
    return new ProcessEngineException(exceptionMessage(
      "009",
      "Invalid value '{}' for configuration property '{}': {}.", propertyValue, propertyName, reason));
  }

  public void invalidBatchOperation(String operation, String historyTimeToLive) {
    logWarn(
      "010", "Invalid batch operation name '{}' with history time to live set to'{}'" , operation, historyTimeToLive);
  }

  public ProcessEngineException invalidPropertyValue(String propertyName, String propertyValue, Exception e) {
    return new ProcessEngineException(exceptionMessage(
      "011",
      "Invalid value '{}' for configuration property '{}'.", propertyValue, propertyName), e);
  }


}
