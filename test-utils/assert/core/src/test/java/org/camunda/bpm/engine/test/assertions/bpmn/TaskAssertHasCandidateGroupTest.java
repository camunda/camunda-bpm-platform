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
package org.camunda.bpm.engine.test.assertions.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.claim;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.complete;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.taskQuery;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.taskService;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.assertions.helpers.Failure;
import org.camunda.bpm.engine.test.assertions.helpers.ProcessAssertTestCase;
import org.junit.Rule;
import org.junit.Test;

public class TaskAssertHasCandidateGroupTest extends ProcessAssertTestCase {

  private static final String CANDIDATE_GROUP = "candidateGroup";
  private static final String ASSIGNEE = "assignee";

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  @Test
  @Deployment(resources = {"bpmn/TaskAssert-hasCandidateGroup.bpmn"
  })
  public void testHasCandidateGroup_PreDefined_Success() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "TaskAssert-hasCandidateGroup"
    );
    // Then
    assertThat(processInstance).task().hasCandidateGroup(CANDIDATE_GROUP);
  }

  @Test
  @Deployment(resources = {"bpmn/TaskAssert-hasCandidateGroup.bpmn"
  })
  public void testHasCandidateGroup_PreDefined_Failure() {
    // Given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "TaskAssert-hasCandidateGroup"
    );
    // When
    complete(taskQuery().singleResult());
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).task().hasCandidateGroup(CANDIDATE_GROUP);
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/TaskAssert-hasCandidateGroup.bpmn"
  })
  public void testHasCandidateGroup_Predefined_Removed_Failure() {
    // Given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "TaskAssert-hasCandidateGroup"
    );
    // When
    taskService().deleteCandidateGroup(taskQuery().singleResult().getId(), CANDIDATE_GROUP);
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).task().hasCandidateGroup(CANDIDATE_GROUP);
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/TaskAssert-hasCandidateGroup.bpmn"
  })
  public void testHasCandidateGroup_PreDefined_Other_Failure() {
    // Given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "TaskAssert-hasCandidateGroup"
    );
    // When
    taskService().deleteCandidateGroup(taskQuery().singleResult().getId(), CANDIDATE_GROUP);
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).task().hasCandidateGroup("otherCandidateGroup");
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/TaskAssert-hasCandidateGroup.bpmn"
  })
  public void testHasCandidateGroup_ExplicitelySet_Success() {
    // Given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "TaskAssert-hasCandidateGroup"
    );
    // When
    complete(taskQuery().singleResult());
    // And
    taskService().addCandidateGroup(taskQuery().singleResult().getId(), "explicitCandidateGroupId");
    // Then
    assertThat(processInstance).task().hasCandidateGroup("explicitCandidateGroupId");
  }

  @Test
  @Deployment(resources = {"bpmn/TaskAssert-hasCandidateGroup.bpmn"
  })
  public void testHasCandidateGroup_ExplicitelySet_Failure() {
    // Given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "TaskAssert-hasCandidateGroup"
    );
    // When
    complete(taskQuery().singleResult());
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).task().hasCandidateGroup(CANDIDATE_GROUP);
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/TaskAssert-hasCandidateGroup.bpmn"
  })
  public void testHasCandidateGroup_ExplicitelySet_Removed_Failure() {
    // Given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "TaskAssert-hasCandidateGroup"
    );
    // When
    complete(taskQuery().singleResult());
    // And
    taskService().addCandidateGroup(taskQuery().singleResult().getId(), "explicitCandidateGroupId");
    // When
    taskService().deleteCandidateGroup(taskQuery().singleResult().getId(), "explicitCandidateGroupId");
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).task().hasCandidateGroup("explicitCandidateGroupId");
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/TaskAssert-hasCandidateGroup.bpmn"
  })
  public void testHasCandidateGroup_ExplicitelySet_Other_Failure() {
    // Given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "TaskAssert-hasCandidateGroup"
    );
    // When
    complete(taskQuery().singleResult());
    // And
    taskService().addCandidateGroup(taskQuery().singleResult().getId(), "explicitCandidateGroupId");
    // When
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).task().hasCandidateGroup("otherCandidateGroup");
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/TaskAssert-hasCandidateGroup.bpmn"
  })
  public void testHasCandidateGroup_MoreThanOne_Success() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "TaskAssert-hasCandidateGroup"
    );
    // When
    taskService().addCandidateGroup(taskQuery().singleResult().getId(), "explicitCandidateGroupId");
    // Then
    assertThat(processInstance).task().hasCandidateGroup(CANDIDATE_GROUP);
    // And
    assertThat(processInstance).task().hasCandidateGroup("explicitCandidateGroupId");
  }

  @Test
  @Deployment(resources = {"bpmn/TaskAssert-hasCandidateGroup.bpmn"
  })
  public void testHasCandidateGroup_MoreThanOne_Failure() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "TaskAssert-hasCandidateGroup"
    );
    // When
    taskService().addCandidateGroup(taskQuery().singleResult().getId(), "explicitCandidateGroupId");
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).task().hasCandidateGroup("otherCandidateGroup");
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/TaskAssert-hasCandidateGroup.bpmn"
  })
  public void testHasCandidateGroup_Null_Failure() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "TaskAssert-hasCandidateGroup"
    );
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).task().hasCandidateGroup(null);
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/TaskAssert-hasCandidateGroup.bpmn"
  })
  public void testHasCandidateGroup_NonExistingTask_Failure() {
    // Given
    runtimeService().startProcessInstanceByKey(
      "TaskAssert-hasCandidateGroup"
    );
    // When
    final Task task = taskQuery().singleResult();
    complete(task);
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(task).hasCandidateGroup(CANDIDATE_GROUP);
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/TaskAssert-hasCandidateGroup.bpmn"
  })
  public void testHasCandidateGroup_Assigned_Failure() {
    // Given
    final ProcessInstance pi = runtimeService().startProcessInstanceByKey(
        "TaskAssert-hasCandidateGroup"
    );
    // When
    claim(task(pi), ASSIGNEE);
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(task(pi)).hasCandidateGroup(CANDIDATE_GROUP);
      }
    });
  }

}
