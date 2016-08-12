/*
 * Copyright 2016 camunda services GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
package org.camunda.bpm.engine.test.standalone.entity;

import java.util.ArrayList;
import java.util.List;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class ExecutionEntityTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testRestoreProcessInstance() {
    //given parent execution
    List<ExecutionEntity> entities = new ArrayList<ExecutionEntity>();
    ExecutionEntity parent = new ExecutionEntity();
    parent.setId("parent");
    entities.add(parent);
    //when restore process instance is called
    parent.restoreProcessInstance(entities, null, null, null, null, null, null);
    //then no problem should occure

    //when child is added and restore is called again
    ExecutionEntity entity = new ExecutionEntity();
    entity.setId("child");
    entity.setParentId(parent.getId());
    entities.add(entity);

    parent.restoreProcessInstance(entities, null, null, null, null, null, null);
    //then again no problem should occure

    //when parent is deleted from the list
    entities.remove(parent);

    //then exception is thrown because child reference to parent which does not exist anymore
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot resolve parent with id 'parent' of execution 'child', perhaps it was deleted in the meantime");
    parent.restoreProcessInstance(entities, null, null, null, null, null, null);
  }
}
