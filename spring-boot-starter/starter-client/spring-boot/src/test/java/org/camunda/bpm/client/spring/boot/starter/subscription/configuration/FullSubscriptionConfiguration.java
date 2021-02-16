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
package org.camunda.bpm.client.spring.boot.starter.subscription.configuration;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription.ProcessVariable;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FullSubscriptionConfiguration {

  @ExternalTaskSubscription(
      autoOpen = true,
      topicName = "topic-one",
      variableNames = {"annotated-variable-one", "annotated-variable-two"},
      lockDuration = 1111,
      localVariables = true,
      businessKey = "annotated-business-key",
      processDefinitionId = "annotated-process-definition-id",
      processDefinitionIdIn = {"annotated-id-one", "annotated-id-two"},
      processDefinitionKey = "annotated-key",
      processDefinitionKeyIn = {"annotated-key-one", "annotated-key-two"},
      processDefinitionVersionTag = "annotated-version-tag",
      processVariables = {
          @ProcessVariable(name = "annotated-var-name-foo", value = "annotated-var-val-foo"),
          @ProcessVariable(name = "annotated-var-name-bar", value = "annotated-var-val-bar")
      },
      withoutTenantId = true,
      tenantIdIn = {"annotated-tenant-id-one", "annotated-tenant-id-two"},
      includeExtensionProperties = true
  )
  @Bean
  public ExternalTaskHandler handler() {
    return (externalTask, externalTaskService) -> {

      // interact with the external task

    };
  }

}