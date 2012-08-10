package com.camunda.fox.platform.qa.deployer.test.demo;

import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;

public class DemoDelegate implements JavaDelegate {

  Expression fail;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    System.out.println("Do Something.");
    execution.setVariable("stringVar", "Demo-Value");
    
    execution.setVariable("longVar", 1L);
    execution.setVariable("longObjectVar", new Long(1));
    execution.setVariable("intVar", 1);
    execution.setVariable("intObjectVar", new Integer(1));
    execution.setVariable("shortVar", (short) 1);
    execution.setVariable("shortObjectVar", new Short("1"));
    execution.setVariable("byteVar", (byte) 1);
    execution.setVariable("byteObjectVar", new Byte("1"));
    execution.setVariable("booleanVar", true);
    execution.setVariable("booleanObjectVar", Boolean.TRUE);
    execution.setVariable("floatVar", 1.5f);
    execution.setVariable("floatObjectVar", new Float(1.5f));
    execution.setVariable("doubleVar", 1.5d);
    execution.setVariable("doubleObjectVar", new Double(1.5d));
    execution.setVariable("charVar", 'a');
    execution.setVariable("charObjectVar", new Character('a'));
    execution.setVariable("dateObjectVar", new Date() );
    execution.setVariable("nullable", null);
    
    execution.setVariable("random", new Double(Math.random() * 100).intValue());

    char[] charArray = {'a','b','c','D'};
    execution.setVariable("charArrayVar", charArray);
    Character[] characterObjectArray = { new Character('a'), new Character('b'), new Character('c'), new Character('D') };
    execution.setVariable("characterObjectArray", characterObjectArray);
    
    String byteString = "mycooltextcontentasbyteyesyes!!!";
    execution.setVariable("byteArrayVar", byteString.getBytes("UTF-8"));
    Byte[] ByteArray = new Byte[byteString.length()];
    byte[] bytes = byteString.getBytes("UTF-8");

    for (int i = 0; i < bytes.length; i++) {
      byte b = bytes[i];
      ByteArray[i] = new Byte(b);
    }
    execution.setVariable("ByteArrayVariable", ByteArray);
    
    DemoVariableClass demoVariableClass = new DemoVariableClass();
    
    demoVariableClass.setBooleanObjectProperty(new Boolean(true));
    demoVariableClass.setBooleanProperty(false);
    demoVariableClass.setByteObjectProperty(new Byte(Byte.MAX_VALUE));
    demoVariableClass.setCharProperty('z');
    demoVariableClass.setDoubleObjectProperty(new Double(2.25d));
    demoVariableClass.setDoubleProperty(1.75d);
    demoVariableClass.setFloatObjectProperty(new Float(4.34f));
    demoVariableClass.setFloatProperty(100.005f);
    demoVariableClass.setIntArrayProperty(new int[]{1,2,3});
    demoVariableClass.setIntegerObjectProperty(null);
    demoVariableClass.setIntProperty(-10);
    demoVariableClass.setLongObjectProperty(new Long(Long.MIN_VALUE));
    demoVariableClass.setLongProperty(Long.MAX_VALUE);
    
    HashMap<Object,Object> demoHashMap= new HashMap<Object,Object>();
    demoHashMap.put("key1", "value1");
    demoHashMap.put("key2", "value2");
    demoVariableClass.setMapProperty(demoHashMap);
    
    demoVariableClass.setShortObjectProperty(new Short(Short.MAX_VALUE));
    demoVariableClass.setShortProperty(Short.MIN_VALUE);
    demoVariableClass.setStringProperty("cockpit rulez");
    
    execution.setVariable("demoVariableClass", demoVariableClass);
    
    if (null != fail) {
      String failString = (String) fail.getValue(execution);
      if (null != failString && failString.equals("true")) {
        System.out.println("I'm failing now!.");
        throw new RuntimeException("I'm supposed to fail"+new Random().nextInt(5));
      }
    }
  }
}