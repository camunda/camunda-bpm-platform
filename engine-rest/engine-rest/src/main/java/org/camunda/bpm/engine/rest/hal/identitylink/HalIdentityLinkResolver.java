/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.rest.hal.identitylink;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.hal.HalResource;
import org.camunda.bpm.engine.rest.hal.cache.HalCachingLinkResolver;
import org.camunda.bpm.engine.task.IdentityLink;

public class HalIdentityLinkResolver extends HalCachingLinkResolver {

  protected Class<?> getHalResourceClass() {
    return HalIdentityLink.class;
  }

  @Override
  public List<HalResource<?>> resolveLinks(String[] linkedIds, ProcessEngine processEngine) {
    if (linkedIds.length > 1) {
      throw new InvalidRequestException(Response.Status.INTERNAL_SERVER_ERROR, "The identity link resolver can only handle one task id");
    }

    return super.resolveLinks(linkedIds, processEngine);
  }

  protected List<HalResource<?>> resolveNotCachedLinks(String[] linkedIds, ProcessEngine processEngine) {
    TaskService taskService = processEngine.getTaskService();

    List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(linkedIds[0]);

    List<HalResource<?>> resolvedIdentityLinks = new ArrayList<HalResource<?>>();
    for (IdentityLink identityLink : identityLinks) {
      resolvedIdentityLinks.add(HalIdentityLink.fromIdentityLink(identityLink));
    }

    return resolvedIdentityLinks;
  }

  protected void putIntoCache(List<HalResource<?>> notCachedResources) {
    // this resolver only can handle a single task and resolves a list of hal resources for this task
    if (notCachedResources != null && !notCachedResources.isEmpty()) {
      String taskId = getResourceId(notCachedResources.get(0));
      getCache().put(taskId, notCachedResources);
    }
  }

  protected String getResourceId(HalResource<?> resource) {
    return ((HalIdentityLink) resource).getTaskId();
  }

}
