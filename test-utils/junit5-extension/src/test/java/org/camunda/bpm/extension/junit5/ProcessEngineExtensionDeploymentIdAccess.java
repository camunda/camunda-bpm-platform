package org.camunda.bpm.extension.junit5;

import org.camunda.bpm.extension.junit5.test.ProcessEngineExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * @author Martin Schimak
 */
public class ProcessEngineExtensionDeploymentIdAccess {

  @Test
  public void testDeploymentIdWriteableForExtensions() {

    class ProcessEngineExtensionExtension extends org.camunda.bpm.extension.junit5.test.ProcessEngineExtension {

      @Override
      public void beforeTestExecution(ExtensionContext context) {
        // here I could decide to override the way the deployment is created, e.g.
        // add mocked call activities etc, therefore I need deploymentId accessible
        // Note: in case the write access is removed, this will not compile anymore
        this.deploymentId = "xyz";
      }

    }

  }


  @Test
  public void testDeploymentIdReadableForExtensionsAndWrappers() {

    class ProcessEngineExtensionExtension extends org.camunda.bpm.extension.junit5.test.ProcessEngineExtension {

      @Override
      public void afterTestExecution(ExtensionContext context) {
        // here I could decide to override the way the deployment is removed
        // Note: in case the write access is removed, this will not compile anymore
        String deploymentId = this.deploymentId;
      }

    }

    class ProcessEngineExtensionWrapper {

      ProcessEngineExtension wrapped = ProcessEngineExtension.builder().build();

      {
        // here I could decide to access the deployment. e.g. to look at the
        // runtime or history data of the current deployment before it is removed
        // Note: in case this read access is removed, this will not compile anymore
        String deploymentId = wrapped.getDeploymentId();
      }

    }

  }

}
