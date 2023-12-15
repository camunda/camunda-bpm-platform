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
package org.camunda.bpm.integrationtest.jobexecutor.beans;

import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Logger;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class DemoDelegate implements JavaDelegate {

  Logger log = Logger.getLogger(DemoDelegate.class.getName());
  Expression fail;
  DelegateExecution execution;
  
  @Override
  public void execute(DelegateExecution execution) throws Exception {
    this.execution = execution;
    
    log.info("Do Something.");
    
    insertVariable("stringVar", "Demo-Value");
    insertVariable("longVar", 1L);
    insertVariable("longObjectVar", Long.valueOf(1));
    insertVariable("intVar", 1);
    insertVariable("intObjectVar", Integer.valueOf(1));
    insertVariable("shortVar", (short) 1);
    insertVariable("shortObjectVar", Short.valueOf("1"));
    insertVariable("byteVar", (byte) 1);
    insertVariable("byteObjectVar", Byte.valueOf("1"));
    insertVariable("booleanVar", true);
    insertVariable("booleanObjectVar", Boolean.TRUE);
    insertVariable("floatVar", 1.5f);
    insertVariable("floatObjectVar", Float.valueOf(1.5f));
    insertVariable("doubleVar", 1.5d);
    insertVariable("doubleObjectVar", Double.valueOf(1.5d));
    insertVariable("charVar", 'a');
    insertVariable("charObjectVar", Character.valueOf('a'));
    insertVariable("dateObjectVar", new Date());
    insertVariable("nullable", null);
    insertVariable("random", Double.valueOf(Math.random() * 100).intValue());
    
    char[] charArray = {'a','b','c','D'};
    insertVariable("charArrayVar", charArray);
    Character[] characterObjectArray = { Character.valueOf('a'), Character.valueOf('b'), Character.valueOf('c'), Character.valueOf('D') };
    insertVariable("characterObjectArray", characterObjectArray);
    
    String byteString = "mycooltextcontentasbyteyesyes!!!";
    insertVariable("byteArrayVar", byteString.getBytes("UTF-8"));
    Byte[] ByteArray = new Byte[byteString.length()];
    byte[] bytes = byteString.getBytes("UTF-8");

    for (int i = 0; i < bytes.length; i++) {
      byte b = bytes[i];
      ByteArray[i] = Byte.valueOf(b);
    }
    insertVariable("ByteArrayVariable", ByteArray);
    
    DemoVariableClass demoVariableClass = new DemoVariableClass();
    
    demoVariableClass.setBooleanObjectProperty(Boolean.valueOf(true));
    demoVariableClass.setBooleanProperty(false);
    demoVariableClass.setByteObjectProperty(Byte.valueOf(Byte.MAX_VALUE));
    demoVariableClass.setCharProperty('z');
    demoVariableClass.setDoubleObjectProperty(Double.valueOf(2.25d));
    demoVariableClass.setDoubleProperty(1.75d);
    demoVariableClass.setFloatObjectProperty(Float.valueOf(4.34f));
    demoVariableClass.setFloatProperty(100.005f);
    demoVariableClass.setIntArrayProperty(new int[]{1,2,3});
    demoVariableClass.setIntegerObjectProperty(null);
    demoVariableClass.setIntProperty(-10);
    demoVariableClass.setLongObjectProperty(Long.valueOf(Long.MIN_VALUE));
    demoVariableClass.setLongProperty(Long.MAX_VALUE);
    
    HashMap<Object,Object> demoHashMap= new HashMap<Object,Object>();
    demoHashMap.put("key1", "value1");
    demoHashMap.put("key2", "value2");
    demoVariableClass.setMapProperty(demoHashMap);
    
    demoVariableClass.setShortObjectProperty(Short.valueOf(Short.MAX_VALUE));
    demoVariableClass.setShortProperty(Short.MIN_VALUE);
    demoVariableClass.setStringProperty("cockpit rulez");
    
    insertVariable("demoVariableClass", demoVariableClass);
    
    if (null != fail) {
      String failString = (String) fail.getValue(execution);
      if (null != failString && failString.equals("true")) {
        log.info("I'm failing now!.");
        throw new RuntimeException("I'm supposed to fail" + new Random().nextInt(5));
      }
    }
  }
  
  private void insertVariable(String varName, Object value) {
    execution.setVariable(varName + Double.valueOf(Math.random() * 10).intValue(), value);
  }
}