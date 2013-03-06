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

import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.management.DeploymentStatistics;
import org.camunda.bpm.engine.management.DeploymentStatisticsQuery;

public class DeploymentStatisticsQueryImpl extends AbstractQuery<DeploymentStatisticsQuery, DeploymentStatistics>
implements DeploymentStatisticsQuery {

  protected static final long serialVersionUID = 1L;
  protected boolean includeFailedJobs = false;
  
  public DeploymentStatisticsQueryImpl(CommandExecutor executor) {
    super(executor);
  }

  @Override
  public DeploymentStatisticsQuery includeFailedJobs() {
    includeFailedJobs = true;
    return this;
  }

  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return 
        commandContext
          .getStatisticsManager()
          .getStatisticsCountGroupedByDeployment(this);
  }

  @Override
  public List<DeploymentStatistics> executeList(CommandContext commandContext,
      Page page) {
    checkQueryOk();
    return 
      commandContext
        .getStatisticsManager()
        .getStatisticsGroupedByDeployment(this, page);
  }
  
  public boolean isFailedJobsToInclude() {
    return includeFailedJobs;
  }
  

}
