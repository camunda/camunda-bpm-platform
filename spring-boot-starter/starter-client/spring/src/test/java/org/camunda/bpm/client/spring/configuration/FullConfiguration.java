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
package org.camunda.bpm.client.spring.configuration;

import org.camunda.bpm.client.spring.annotation.EnableExternalTaskClient;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription.ProcessVariable;

@Configuration
@EnableExternalTaskClient(
    baseUrl = "url",
    maxTasks = 1111,
    workerId = "worker-id",
    usePriority = false,
    useCreateTime = false,
    orderByCreateTime = "asc",
    asyncResponseTimeout = 5555,
    disableAutoFetching = true,
    disableBackoffStrategy = true,
    lockDuration = 4444,
    dateFormat = "date-format",
    defaultSerializationFormat = "default-serialization-format"
)
public class FullConfiguration {

  @ExternalTaskSubscription(
      topicName = "topic-name",
      variableNames = {"variable-one", "variable-two"},
      lockDuration = 1111,
      localVariables = true,
      businessKey = "business-key",
      processDefinitionId = "process-definition-id",
      processDefinitionIdIn = {"id-one", "id-two"},
      processDefinitionKey = "key",
      processDefinitionKeyIn = {"key-one", "key-two"},
      processDefinitionVersionTag = "version-tag",
      processVariables = {
          @ProcessVariable(name = "var-name-foo", value = "var-val-foo"),
          @ProcessVariable(name = "var-name-bar", value = "var-val-bar")
      },
      withoutTenantId = true,
      tenantIdIn = {"tenant-id-one", "tenant-id-two"},
      includeExtensionProperties = true
  )
  @Bean
  public ExternalTaskHandler handler() {
    return (externalTask, externalTaskService) -> {

      // interact with the external task

    };
  }

}
