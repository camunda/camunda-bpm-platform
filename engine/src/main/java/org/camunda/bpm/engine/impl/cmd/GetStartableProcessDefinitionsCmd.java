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

package org.camunda.bpm.engine.impl.cmd;

import java.io.Serializable;
import java.util.List;

import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.repository.ProcessDefinition;

/**
 * Query for 'startable' process definitions in Tasklist
 * The required Permissions are:
 * <ul>
 *    <li> {@link Permissions#CREATE} permission on {@link Resources#PROCESS_INSTANCE} </li>
 *    <li> {@link Permissions#CREATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION} </li>
 *    <li> {@link Permissions#READ} permission on {@link Resources#PROCESS_DEFINITION} </li>
 * </ul>
 *
 * @author Yana Vasileva
 *
 */
public class GetStartableProcessDefinitionsCmd implements Command<List<ProcessDefinition>>, Serializable {

  private static final long serialVersionUID = 1L;
  protected int firstResult;
  protected int maxResults;

  public GetStartableProcessDefinitionsCmd() {

  }

  public GetStartableProcessDefinitionsCmd(int firstResult, int maxResults) {
    this.firstResult = firstResult;
    this.maxResults = maxResults;
  }

  public List<ProcessDefinition> execute(final CommandContext commandContext) {
    return commandContext.getProcessDefinitionManager().findStartableInTasklistProcessDefinitions(firstResult, maxResults);
  }

}
