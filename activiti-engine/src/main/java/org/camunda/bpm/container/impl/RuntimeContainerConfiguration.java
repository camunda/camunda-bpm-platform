package org.camunda.bpm.container.impl;

import org.camunda.bpm.container.impl.jmx.JmxRuntimeContainerDelegate;
import org.camunda.bpm.container.spi.RuntimeContainerDelegate;

/**
 * Allows to access the resources provided by the RuntimeContainer. 
 * 
 * @see RuntimeContainerDelegate
 * 
 * @author Daniel Meyer
 *
 */
public class RuntimeContainerConfiguration {
  
  private final static RuntimeContainerConfiguration INSTANCE = new RuntimeContainerConfiguration();
  
  // hide
  private RuntimeContainerConfiguration() {
  }
  
  private RuntimeContainerDelegate containerDelegate = new JmxRuntimeContainerDelegate();
  
  private String runtimeContainerName = "Embedded JMX Server";
  
  public String getRuntimeContainerName() {
    return runtimeContainerName;
  }
  
  public RuntimeContainerDelegate getContainerDelegate() {
    return containerDelegate;
  }
  
  public void setContainerDelegate(RuntimeContainerDelegate containerDelegate) {
    this.containerDelegate = containerDelegate;
  }
  
  public void setRuntimeContainerName(String runtimeContainerName) {
    this.runtimeContainerName = runtimeContainerName;
  }
  
  public static RuntimeContainerConfiguration getINSTANCE() {
    return INSTANCE;
  }

}
