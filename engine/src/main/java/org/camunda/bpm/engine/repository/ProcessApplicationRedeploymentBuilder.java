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
package org.camunda.bpm.engine.repository;

import org.camunda.bpm.application.ProcessApplication;

import java.util.List;

/**
 * <p>Builder for a re-deployment of {@link ProcessApplication} deployment</p>
 *
 * @see RedeploymentBuilder
 * @see ProcessApplicationDeploymentBuilder
 *
 * @author Roman Smirnov
 *
 */
public interface ProcessApplicationRedeploymentBuilder extends RedeploymentBuilder {

  /**
   * <p>If this method is called, additional registrations will be created for
   * previous versions of the deployment.</p>
   *
   * @see ProcessApplicationDeploymentBuilder#resumePreviousVersions()
   */
  ProcessApplicationRedeploymentBuilder resumePreviousVersions();

  /**
   * <p>This method defines on what additional registrations will be based.
   * The value will only be recognized if {@link #resumePreviousVersions()} is set.
   * </p>
   *
   * @see ResumePreviousBy
   * @see #resumePreviousVersions()
   * @see ProcessApplicationDeploymentBuilder#resumePreviousVersionsBy(String)
   *
   * @param resumeByProcessDefinitionKey one of the constants from {@link ResumePreviousBy}
   */
  ProcessApplicationRedeploymentBuilder resumePreviousVersionsBy(String resumePreviousVersionsBy);

  /* {@inheritDoc} */
  ProcessApplicationDeployment redeploy();

  /* {@inheritDoc} */
  ProcessApplicationRedeploymentBuilder source(String source);

  /* {@inheritDoc} */
  ProcessApplicationRedeploymentBuilder addResourceId(String resourceId);

  /* {@inheritDoc} */
  ProcessApplicationRedeploymentBuilder addResourceIds(List<String> resourceIds);

  /* {@inheritDoc} */
  ProcessApplicationRedeploymentBuilder addResourceName(String resourceName);

  /* {@inheritDoc} */
  ProcessApplicationRedeploymentBuilder addResourceNames(List<String> resourceNames);

}
