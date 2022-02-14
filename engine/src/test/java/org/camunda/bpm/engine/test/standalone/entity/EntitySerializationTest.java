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
package org.camunda.bpm.engine.test.standalone.entity;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Date;

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.engine.task.DelegationState;
import org.junit.Test;

public class EntitySerializationTest {

  @Test
  public void testTaskEntitySerialization() throws Exception {
    TaskEntity task = new TaskEntity();
    task.setDelegationState(DelegationState.RESOLVED);
    task.setExecution(new ExecutionEntity());
    task.setProcessInstance(new ExecutionEntity());
    task.setTaskDefinition(new TaskDefinition(null));

    task.setAssignee("kermit");
    task.setCreateTime(new Date());
    task.setDescription("Test description");
    task.setDueDate(new Date());
    task.setName("myTask");
    task.setEventName("end");
    task.setDeleted(false);
    task.setDelegationStateString(DelegationState.RESOLVED.name());

    byte[] data = writeObject(task);
    task = (TaskEntity) readObject(data);

    assertEquals("kermit", task.getAssignee());
    assertEquals("myTask", task.getName());
    assertEquals("end", task.getEventName());
  }

  @Test
  public void testExecutionEntitySerialization() throws Exception {
   ExecutionEntity execution = new ExecutionEntity();

   ActivityImpl activityImpl = new ActivityImpl("test", null);
   activityImpl.getExecutionListeners().put("start", Collections.<ExecutionListener>singletonList(new TestExecutionListener()));
   execution.setActivity(activityImpl);

   ProcessDefinitionImpl processDefinitionImpl = new ProcessDefinitionImpl("test");
   processDefinitionImpl.getExecutionListeners().put("start", Collections.<ExecutionListener>singletonList(new TestExecutionListener()));
   execution.setProcessDefinition(processDefinitionImpl);

   TransitionImpl transitionImpl = new TransitionImpl("test", new ProcessDefinitionImpl("test"));
   transitionImpl.addExecutionListener(new TestExecutionListener());
   execution.setTransition(transitionImpl);

   execution.setSuperExecution(new ExecutionEntity());

   execution.setActive(true);
   execution.setCanceled(false);
   execution.setBusinessKey("myBusinessKey");
   execution.setDeleteReason("no reason");
   execution.setActivityInstanceId("123");
   execution.setScope(false);

   byte[] data = writeObject(execution);
   execution = (ExecutionEntity) readObject(data);

   assertEquals("myBusinessKey", execution.getBusinessKey());
   assertEquals("no reason", execution.getDeleteReason());
   assertEquals("123", execution.getActivityInstanceId());

  }

  private byte[] writeObject(Object object) throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    ObjectOutputStream outputStream = new ObjectOutputStream(buffer);
    outputStream.writeObject(object);
    outputStream.flush();
    outputStream.close();

    return buffer.toByteArray();
  }

  private Object readObject(byte[] data) throws IOException, ClassNotFoundException {
    InputStream buffer = new ByteArrayInputStream(data);
    ObjectInputStream inputStream = new ObjectInputStream(buffer);
    Object object = inputStream.readObject();

    return object;
  }

}
