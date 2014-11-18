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
package org.camunda.bpm.engine.rest.hal.caseDefinition;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.rest.hal.HalResource;
import org.camunda.bpm.engine.rest.hal.cache.HalIdResourceCacheLinkResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Meyer
 *
 */
public class HalCaseDefinitionResolver extends HalIdResourceCacheLinkResolver {

  protected Class<?> getHalResourceClass() {
    return HalCaseDefinition.class;
  }

  protected List<HalResource<?>> resolveNotCachedLinks(String[] linkedIds, ProcessEngine processEngine) {
    RepositoryService repositoryService = processEngine.getRepositoryService();

    List<CaseDefinition> caseDefinitions = repositoryService.createCaseDefinitionQuery()
      .caseDefinitionIdIn(linkedIds)
      .listPage(0, linkedIds.length);

    List<HalResource<?>> resolved = new ArrayList<HalResource<?>>();
    for (CaseDefinition caseDefinition : caseDefinitions) {
      resolved.add(HalCaseDefinition.fromCaseDefinition(caseDefinition, processEngine));
    }

    return resolved;
  }

}
