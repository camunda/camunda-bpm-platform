package org.camunda.bpm.application.impl;

import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.ProcessApplicationReference;

/**
 * <p>An embedded process application is a ProcessApplication that uses an embedded
 * process engine. An embedded process engine is loaded by the same classloader as 
 * the process application which usually means that the <code>camunda-engine.jar</code> 
 * is deployed as a web application library (in case of WAR deployments) or as an 
 * application library (in case of EAR deployments).</p>
 * 
 * @author Daniel Meyer
 * 
 */
public class EmbeddedProcessApplication extends ProcessApplication {

  protected String autodetectProcessApplicationName() {
    return "Process Application";
  }

  public ProcessApplicationReference getReference() {
    return new ProcessApplicationReferenceImpl(this);
  }

}
