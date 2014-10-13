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

import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.history.HistoricCaseActivityInstance;
import org.camunda.bpm.engine.history.NativeHistoricCaseActivityInstanceQuery;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;


public class NativeHistoricCaseActivityInstanceQueryImpl extends AbstractNativeQuery<NativeHistoricCaseActivityInstanceQuery, HistoricCaseActivityInstance> implements NativeHistoricCaseActivityInstanceQuery {

  private static final long serialVersionUID = 1L;

  public NativeHistoricCaseActivityInstanceQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public NativeHistoricCaseActivityInstanceQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

 //results ////////////////////////////////////////////////////////////////
  
  public List<HistoricCaseActivityInstance> executeList(CommandContext commandContext, Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return commandContext
      .getHistoricCaseActivityInstanceManager()
      .findHistoricCaseActivityInstancesByNativeQuery(parameterMap, firstResult, maxResults);
  }
  
  public long executeCount(CommandContext commandContext, Map<String, Object> parameterMap) {
    return commandContext
      .getHistoricCaseActivityInstanceManager()
      .findHistoricCaseActivityInstanceCountByNativeQuery(parameterMap);
  }

}
