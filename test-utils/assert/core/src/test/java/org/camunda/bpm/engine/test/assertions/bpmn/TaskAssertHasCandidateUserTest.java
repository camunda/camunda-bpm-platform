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
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.complete;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;
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

public class TaskAssertHasCandidateUserTest extends ProcessAssertTestCase {

  private static final String CANDIDATE_USER = "candidateUser";
  private static final String ASSIGNEE = "assignee";

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  @Test
  @Deployment(resources = {"bpmn/TaskAssert-hasCandidateUser.bpmn"
  })
  public void testHasCandidateUserPreDefined_Success() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "TaskAssert-hasCandidateUser"
    );
    // Then
    assertThat(processInstance).task().hasCandidateUser(CANDIDATE_USER);
  }

  @Test
  @Deployment(resources = {"bpmn/TaskAssert-hasCandidateUser.bpmn"
  })
  public void testHasCandidateUser_PreDefined_Failure() {
    // Given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "TaskAssert-hasCandidateUser"
    );
    // When
    complete(taskQuery().singleResult());
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).task().hasCandidateUser(CANDIDATE_USER);
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/TaskAssert-hasCandidateUser.bpmn"
  })
  public void testHasCandidateUser_Predefined_Removed_Failure() {
    // Given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "TaskAssert-hasCandidateUser"
    );
    // When
    taskService().deleteCandidateUser(taskQuery().singleResult().getId(), CANDIDATE_USER);
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).task().hasCandidateUser(CANDIDATE_USER);
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/TaskAssert-hasCandidateUser.bpmn"
  })
  public void testHasCandidateUser_PreDefined_Other_Failure() {
    // Given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "TaskAssert-hasCandidateUser"
    );
    // When
    taskService().deleteCandidateUser(taskQuery().singleResult().getId(), CANDIDATE_USER);
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).task().hasCandidateUser("otherCandidateUser");
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/TaskAssert-hasCandidateUser.bpmn"
  })
  public void testHasCandidateUser_ExplicitelySet_Success() {
    // Given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "TaskAssert-hasCandidateUser"
    );
    // When
    complete(taskQuery().singleResult());
    // And
    taskService().addCandidateUser(taskQuery().singleResult().getId(), "explicitCandidateUserId");
    // Then
    assertThat(processInstance).task().hasCandidateUser("explicitCandidateUserId");
  }

  @Test
  @Deployment(resources = {"bpmn/TaskAssert-hasCandidateUser.bpmn"
  })
  public void testHasCandidateUser_ExplicitelySet_Failure() {
    // Given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "TaskAssert-hasCandidateUser"
    );
    // When
    complete(taskQuery().singleResult());
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).task().hasCandidateUser(CANDIDATE_USER);
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/TaskAssert-hasCandidateUser.bpmn"
  })
  public void testHasCandidateUser_ExplicitelySet_Removed_Failure() {
    // Given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "TaskAssert-hasCandidateUser"
    );
    // When
    complete(taskQuery().singleResult());
    // And
    taskService().addCandidateUser(taskQuery().singleResult().getId(), "explicitCandidateUserId");
    // When
    taskService().deleteCandidateUser(taskQuery().singleResult().getId(), "explicitCandidateUserId");
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).task().hasCandidateUser("explicitCandidateUserId");
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/TaskAssert-hasCandidateUser.bpmn"
  })
  public void testHasCandidateUser_ExplicitelySet_Other_Failure() {
    // Given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "TaskAssert-hasCandidateUser"
    );
    // When
    complete(taskQuery().singleResult());
    // And
    taskService().addCandidateUser(taskQuery().singleResult().getId(), "explicitCandidateUserId");
    // When
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).task().hasCandidateUser("otherCandidateUser");
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/TaskAssert-hasCandidateUser.bpmn"
  })
  public void testHasCandidateUser_MoreThanOne_Success() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "TaskAssert-hasCandidateUser"
    );
    // When
    taskService().addCandidateUser(taskQuery().singleResult().getId(), "explicitCandidateUserId");
    // Then
    assertThat(processInstance).task().hasCandidateUser(CANDIDATE_USER);
    // And
    assertThat(processInstance).task().hasCandidateUser("explicitCandidateUserId");
  }

  @Test
  @Deployment(resources = {"bpmn/TaskAssert-hasCandidateUser.bpmn"
  })
  public void testHasCandidateUser_MoreThanOne_Failure() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "TaskAssert-hasCandidateUser"
    );
    // When
    taskService().addCandidateUser(taskQuery().singleResult().getId(), "explicitCandidateUserId");
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).task().hasCandidateUser("otherCandidateUser");
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/TaskAssert-hasCandidateUser.bpmn"
  })
  public void testHasCandidateUser_Null_Failure() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "TaskAssert-hasCandidateUser"
    );
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).task().hasCandidateUser(null);
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/TaskAssert-hasCandidateUser.bpmn"
  })
  public void testHasCandidateUser_NonExistingTask_Failure() {
    // Given
    runtimeService().startProcessInstanceByKey(
      "TaskAssert-hasCandidateUser"
    );
    // When
    final Task task = taskQuery().singleResult();
    complete(task);
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(task).hasCandidateUser(CANDIDATE_USER);
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/TaskAssert-hasCandidateUser.bpmn"
  })
  public void testHasCandidateUser_Assigned_Failure() {
    // Given
    runtimeService().startProcessInstanceByKey(
      "TaskAssert-hasCandidateUser"
    );
    // When
    final Task task = taskQuery().singleResult();
    taskService().setAssignee(task.getId(), ASSIGNEE);
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(task).hasCandidateUser(CANDIDATE_USER);
      }
    });
  }

}
