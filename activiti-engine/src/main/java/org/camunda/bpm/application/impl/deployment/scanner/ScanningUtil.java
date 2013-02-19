package org.camunda.bpm.application.impl.deployment.scanner;

import org.activiti.engine.impl.bpmn.deployer.BpmnDeployer;

public class ScanningUtil {
  
  public static boolean isDeployable(String filename) {
    for (String bpmnResourceSuffix : BpmnDeployer.BPMN_RESOURCE_SUFFIXES) {
      if (filename.endsWith(bpmnResourceSuffix)) {
        return true;
      }
    }
    return false; 
  }
  
  public static boolean isDiagramForProcess(String diagramFileName, String processFileName) {
    for (String bpmnResourceSuffix : BpmnDeployer.BPMN_RESOURCE_SUFFIXES) {
      if (processFileName.endsWith(bpmnResourceSuffix)) {
        String processFilePrefix = processFileName.substring(0, processFileName.length() - bpmnResourceSuffix.length());
        if (diagramFileName.startsWith(processFilePrefix)) {
          for (String diagramResourceSuffix : BpmnDeployer.DIAGRAM_SUFFIXES) {
            if (diagramFileName.endsWith(diagramResourceSuffix)) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }
}