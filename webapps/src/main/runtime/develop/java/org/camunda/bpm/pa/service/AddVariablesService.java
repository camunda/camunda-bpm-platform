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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class AddVariablesService implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    Date now = new Date();
    List<String> serializable = new ArrayList<String>();
    serializable.add("one");
    serializable.add("two");
    serializable.add("three");
    byte[] bytes = "somebytes".getBytes();

    byte aByte = Byte.parseByte("1", 2); // 2 for binary;

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("shortVar", (short) 123);
    variables.put("longVar", 928374L);
    variables.put("integerVar", 1234);

    variables.put("floatVar", (float) Float.MAX_VALUE);
    variables.put("doubleVar", Double.MAX_VALUE);

    variables.put("trueBooleanVar", true);
    variables.put("falseBooleanVar", false);

    variables.put("stringVar", "coca-cola");

    variables.put("dateVar", now);

    variables.put("nullVar", null);

    variables.put("serializableVar", serializable);

    variables.put("bytesVar", bytes);
    variables.put("aByteVar", aByte);
    variables.put("value1", "xyz");

    int random = (int)(Math.random() * 100);
    variables.put("random", random);

    CockpitVariable cockpitVar = new CockpitVariable("test", "cockpitVariableValue");
    cockpitVar.getDates().add(new Date());
    variables.put("cockpitVar", cockpitVar);

    execution.setVariablesLocal(variables);
  }

}
