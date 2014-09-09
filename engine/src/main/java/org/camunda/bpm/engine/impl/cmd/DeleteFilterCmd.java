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

package org.camunda.bpm.engine.impl.cmd;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.Serializable;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.FilterEntity;

/**
 * @author Sebastian Menski
 */
public class DeleteFilterCmd implements Command<Filter>, Serializable {

  private static final long serialVersionUID = 1L;

  protected String filterId;

  public DeleteFilterCmd(String filterId) {
    this.filterId = filterId;
  }

  public Filter execute(CommandContext commandContext) {
    FilterEntity filter = commandContext
      .getDbEntityManager()
      .selectById(FilterEntity.class, filterId);

    ensureNotNull("No filter found for filter id '" + filterId + "'", "filter", filter);

    commandContext
      .getDbEntityManager()
      .delete(filter);

    return filter;
  }
}
