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

package org.camunda.bpm.engine.impl.persistence;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.history.HistoryLevel;


/**
 * @author Tom Baeyens
 */
public class AbstractHistoricManager extends AbstractManager {

  protected HistoryLevel historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();

  protected boolean isHistoryEnabled = !historyLevel.equals(HistoryLevel.HISTORY_LEVEL_NONE);
  protected boolean isHistoryLevelFullEnabled = historyLevel.equals(HistoryLevel.HISTORY_LEVEL_FULL);

  protected void checkHistoryEnabled() {
    if (!isHistoryEnabled) {
      throw new ProcessEngineException("history is not enabled");
    }
  }

  public boolean isHistoryEnabled() {
    return isHistoryEnabled;
  }

  public boolean isHistoryLevelFullEnabled() {
    return isHistoryLevelFullEnabled;
  }
}
