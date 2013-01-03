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

package org.activiti.engine.impl.rules;

import java.util.Map;
import java.util.logging.Logger;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.deploy.Deployer;
import org.activiti.engine.impl.persistence.deploy.DeploymentCache;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;


/**
 * @author Tom Baeyens
 */
public class RulesDeployer implements Deployer {
  
  private static Logger log = Logger.getLogger(RulesDeployer.class.getName());

  public void deploy(DeploymentEntity deployment) {
    KnowledgeBuilder knowledgeBuilder = null;

    DeploymentCache deploymentCache = Context
      .getProcessEngineConfiguration()
      .getDeploymentCache();
    
    Map<String, ResourceEntity> resources = deployment.getResources();
    for (String resourceName : resources.keySet()) {
      log.info("Processing resource " + resourceName);
      if (resourceName.endsWith(".drl")) { // is only parsing .drls sufficient? what about other rule dsl's? (@see ResourceType)
        if (knowledgeBuilder==null) {
          knowledgeBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        }
        ResourceEntity resourceEntity = resources.get(resourceName);
        byte[] resourceBytes = resourceEntity.getBytes();
        Resource droolsResource = ResourceFactory.newByteArrayResource(resourceBytes);
        knowledgeBuilder.add(droolsResource, ResourceType.DRL);
      }
    }
    
    if (knowledgeBuilder!=null) {
      KnowledgeBase knowledgeBase = knowledgeBuilder.newKnowledgeBase();
      deploymentCache.addKnowledgeBase(deployment.getId(), knowledgeBase);
    }
  }
}
