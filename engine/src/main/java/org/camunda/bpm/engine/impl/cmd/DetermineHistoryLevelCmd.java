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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.HistoryLevelSetupCommand;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

import java.util.List;

/**
 * Read the already configured historyLevel from DB and map to given list of total levels.
 */
public class DetermineHistoryLevelCmd implements Command<HistoryLevel> {

  private final List<HistoryLevel> historyLevels;

  public DetermineHistoryLevelCmd(final List<HistoryLevel> historyLevels) {
    this.historyLevels = historyLevels;
  }

  @Override
  public HistoryLevel execute(final CommandContext commandContext) {
    final Integer databaseHistoryLevel = HistoryLevelSetupCommand.databaseHistoryLevel(commandContext);

    HistoryLevel result = null;

    if (databaseHistoryLevel != null) {
      for (final HistoryLevel historyLevel : historyLevels) {
        if (historyLevel.getId() == databaseHistoryLevel) {
          result = historyLevel;
          break;
        }
      }

      if (result != null) {
        return result;
      }
      else {
        // if a custom non-null value is not registered, throw an exception.
        throw new ProcessEngineException(String.format("The configured history level with id='%s' is not registered in this config.", databaseHistoryLevel));
      }
    }
    else {
      return null;
    }
  }


}
