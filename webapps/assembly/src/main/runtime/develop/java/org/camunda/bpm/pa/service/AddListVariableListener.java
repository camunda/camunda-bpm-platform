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
package org.camunda.bpm.pa.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

/**
 * @author Daniel Meyer
 *
 */
public class AddListVariableListener implements TaskListener {
  
  public void notify(DelegateTask task) {
    List<String> list = new ArrayList<String>();
    list.add("demo");
    list.add("john");
    list.add("peter");
    list.add("mary");
    task.setVariable("namesList", list);
    task.setVariable("selectedName", "peter");
    task.setVariable("selectedNumber", 3);
    task.setVariable("selectedDate", new Date());
  }

}
