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
package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyEntity;

public class TelemetrySetupCommand implements Command<Void> {

  private final static EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  protected boolean telemetryEnabled;

  public TelemetrySetupCommand() {
  }
  
  public TelemetrySetupCommand(boolean enabled) {
    this.telemetryEnabled = enabled;
  }

  public Void execute(CommandContext commandContext) {

    checkTelemetryLockExists(commandContext);

    Boolean databaseTelemetryProperty = databaseTelemetryConfiguration(commandContext);

    if (databaseTelemetryProperty == null) {

      commandContext.getPropertyManager().acquireExclusiveLockForTelemetry();
      databaseTelemetryProperty = databaseTelemetryConfiguration(commandContext);

      if(databaseTelemetryProperty == null) {
        LOG.noTelemetryPropertyFound();
        createTelemetryProperty(commandContext);
      }
    } else if (databaseTelemetryProperty != null) {

      commandContext.getPropertyManager().acquireExclusiveLockForTelemetry();
      databaseTelemetryProperty = databaseTelemetryConfiguration(commandContext);

      if(databaseTelemetryProperty != null) {
        PropertyEntity telemetryProperty = fetchTelemetryProperty(commandContext);
        telemetryProperty.setValue(Boolean.toString(telemetryEnabled));
      } else {
        LOG.noTelemetryPropertyFound();
        createTelemetryProperty(commandContext);
      }
    }

    return null;
  }

  protected void checkTelemetryLockExists(CommandContext commandContext) {
    PropertyEntity telemetryLockProperty = commandContext.getPropertyManager().findPropertyById("telemetry.lock");
    if (telemetryLockProperty == null) {
      LOG.noTelemetryLockPropertyFound();
    }
  }

  private PropertyEntity fetchTelemetryProperty(CommandContext commandContext) {
    return commandContext.getPropertyManager().findPropertyById("camunda.telemetry.enabled");
  }

  protected Boolean databaseTelemetryConfiguration(CommandContext commandContext) {
    try {
      PropertyEntity telemetryPropetry =  fetchTelemetryProperty(commandContext);
      return telemetryPropetry != null ? Boolean.parseBoolean(telemetryPropetry.getValue()) : null;
    }
    catch (Exception e) {
      LOG.errorFetchingTelemetryPropertyInDatabase(e);
      return null;
    }
  }

  protected void createTelemetryProperty(CommandContext commandContext) {
    PropertyEntity property = new PropertyEntity("camunda.telemetry.enabled", Boolean.toString(telemetryEnabled));
    commandContext.getSession(DbEntityManager.class).insert(property);
    LOG.creatingTelemetryPropertyInDatabase(telemetryEnabled);
  }

}
