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
package org.camunda.bpm.engine.impl.persistence;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.history.HistoryLevel;


/**
 * @author Tom Baeyens
 */
public class AbstractHistoricManager extends AbstractManager {

  protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  protected HistoryLevel historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();

  protected boolean isHistoryEnabled = !historyLevel.equals(HistoryLevel.HISTORY_LEVEL_NONE);
  protected boolean isHistoryLevelFullEnabled = historyLevel.equals(HistoryLevel.HISTORY_LEVEL_FULL);

  protected void checkHistoryEnabled() {
    if (!isHistoryEnabled) {
      throw LOG.disabledHistoryException();
    }
  }

  public boolean isHistoryEnabled() {
    return isHistoryEnabled;
  }

  public boolean isHistoryLevelFullEnabled() {
    return isHistoryLevelFullEnabled;
  }
}
