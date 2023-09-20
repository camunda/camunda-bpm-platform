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
package org.camunda.bpm.springboot.project.qa.simple;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({ OutputCaptureExtension.class, SpringExtension.class })
@SpringBootTest(classes = { Application.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RequestScopeIT {

  @Autowired
  protected RepositoryService repositoryService;

  @Autowired
  protected RuntimeService runtimeService;

  @Test
  public void shouldEvaluateScript(CapturedOutput logs) {
    // given
    repositoryService.createDeployment()
        .addModelInstance("scriptTaskProcess.bpmn", Bpmn.createExecutableProcess("scriptTaskProcess")
            .camundaHistoryTimeToLive(5)
            .startEvent()
            .scriptTask()
              .scriptFormat("javascript")
              .scriptText("testBean.getName()")
            .endEvent()
            .done())
        .deploy();

    // when
    runtimeService.startProcessInstanceByKey("scriptTaskProcess");

    // then
    assertThat(logs.getOut())
        .contains("Scope 'request' is not active for the current thread",
            "Bean 'scopedTarget.requestScopedTestBean' cannot be accessed since scope is not active. Instead, null is returned.");
  }

}
