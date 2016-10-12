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

import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.util.EnsureUtil;

import java.io.Serializable;
import java.util.List;

/**
 * @author Askar Akhmerov
 */
public class SetJobsRetriesCmd extends AbstractSetJobRetriesCmd implements Command<Void>, Serializable {
  protected final List<String> jobIds;
  protected final int retries;

  public SetJobsRetriesCmd(List<String> jobIds, int retries) {
    EnsureUtil.ensureNotEmpty("Job ID's", jobIds);
    EnsureUtil.ensureGreaterThanOrEqual("Retries count", retries, 0);

    this.jobIds = jobIds;
    this.retries = retries;
  }

  @Override
  public Void execute(CommandContext commandContext) {
    for (String id : jobIds) {
      setJobRetriesByJobId(id, retries, commandContext);
    }
    return null;
  }
}
