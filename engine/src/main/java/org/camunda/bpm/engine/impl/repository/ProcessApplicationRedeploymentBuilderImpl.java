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
package org.camunda.bpm.engine.impl.repository;

import java.util.List;

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.engine.impl.RepositoryServiceImpl;
import org.camunda.bpm.engine.repository.ProcessApplicationDeployment;
import org.camunda.bpm.engine.repository.ProcessApplicationRedeploymentBuilder;

/**
 * @author Roman Smirnov
 *
 */
public class ProcessApplicationRedeploymentBuilderImpl extends RedeploymentBuilderImpl implements ProcessApplicationRedeploymentBuilder {

  public ProcessApplicationRedeploymentBuilderImpl(RepositoryServiceImpl repositoryService, String deploymentId, ProcessApplicationReference reference) {
    super(repositoryService, deploymentId, new ProcessApplicationDeploymentBuilderImpl(repositoryService, reference));
  }

  @Override
  public ProcessApplicationRedeploymentBuilder source(String source) {
    super.source(source);
    return this;
  }

  @Override
  public ProcessApplicationRedeploymentBuilder addResourceId(String resourceId) {
    super.addResourceId(resourceId);
    return this;
  }

  @Override
  public ProcessApplicationRedeploymentBuilder addResourceIds(List<String> resourceIds) {
    super.addResourceIds(resourceIds);
    return this;
  }

  @Override
  public ProcessApplicationRedeploymentBuilder addResourceName(String resourceName) {
    super.addResourceName(resourceName);
    return this;
  }

  @Override
  public ProcessApplicationRedeploymentBuilder addResourceNames(List<String> resourceNames) {
    super.addResourceNames(resourceNames);
    return this;
  }

  public ProcessApplicationRedeploymentBuilder resumePreviousVersions() {
    getDeploymentBuilder().resumePreviousVersions();
    return this;
  }

  public ProcessApplicationRedeploymentBuilder resumePreviousVersionsBy(String resumePreviousVersionsBy) {
    getDeploymentBuilder().resumePreviousVersionsBy(resumePreviousVersionsBy);
    return this;
  }

  @Override
  public ProcessApplicationDeployment redeploy() {
    return (ProcessApplicationDeployment) super.redeploy();
  }

  // getter ///////////////////////////////////////

  @Override
  public ProcessApplicationDeploymentBuilderImpl getDeploymentBuilder() {
    return (ProcessApplicationDeploymentBuilderImpl) super.getDeploymentBuilder();
  }

}
