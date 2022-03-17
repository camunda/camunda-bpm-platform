package org.camunda.bpm.extension.junit5;

import org.camunda.bpm.engine.test.Deployment;

@Deployment(resources = {"processes/superProcess.bpmn", "processes/subProcess.bpmn"})
public class ProcessEngineExtensionSuperClass {

}
