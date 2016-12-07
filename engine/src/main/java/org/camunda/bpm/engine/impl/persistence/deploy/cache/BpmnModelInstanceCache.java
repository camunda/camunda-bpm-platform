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
package org.camunda.bpm.engine.impl.persistence.deploy.cache;

import org.camunda.bpm.engine.impl.ProcessDefinitionQueryImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author: Johannes Heinemann
 */
public class BpmnModelInstanceCache extends ModelInstanceCache<BpmnModelInstance, ProcessDefinitionEntity> {

  public BpmnModelInstanceCache(CacheFactory factory, int cacheCapacity, ResourceDefinitionCache<ProcessDefinitionEntity> definitionCache) {
    super(factory, cacheCapacity, definitionCache);
  }

  @Override
  protected void throwLoadModelException(String definitionId, Exception e) {
    throw LOG.loadModelException("BPMN", "process", definitionId, e);
  }

  @Override
  protected BpmnModelInstance readModelFromStream(InputStream bpmnResourceInputStream) {
    return Bpmn.readModelFromStream(bpmnResourceInputStream);
  }

  @Override
  protected void logRemoveEntryFromDeploymentCacheFailure(String definitionId, Exception e) {
    LOG.removeEntryFromDeploymentCacheFailure("process", definitionId, e);
  }

  @Override
  protected List<ProcessDefinition> getAllDefinitionsForDeployment(final String deploymentId) {
    final CommandContext commandContext = Context.getCommandContext();
    List<ProcessDefinition> allDefinitionsForDeployment = commandContext.runWithoutAuthorization(new Callable<List<ProcessDefinition>>() {
      public List<ProcessDefinition> call() throws Exception {
        return new ProcessDefinitionQueryImpl()
            .deploymentId(deploymentId)
            .list();
      }
    });
    return allDefinitionsForDeployment;
  }
}
