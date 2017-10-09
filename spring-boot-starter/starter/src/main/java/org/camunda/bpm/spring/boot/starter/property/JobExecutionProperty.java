package org.camunda.bpm.spring.boot.starter.property;

import static org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties.joinOn;

public class JobExecutionProperty {

  /**
   * enables job execution
   */
  private boolean enabled;

  /**
   * if job execution is deployment aware
   */
  private boolean deploymentAware;

  private int corePoolSize = 3;
  private int maxPoolSize = 10;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isDeploymentAware() {
    return deploymentAware;
  }

  public void setDeploymentAware(boolean deploymentAware) {
    this.deploymentAware = deploymentAware;
  }

  public int getCorePoolSize() {
    return corePoolSize;
  }

  public void setCorePoolSize(int corePoolSize) {
    this.corePoolSize = corePoolSize;
  }

  public int getMaxPoolSize() {
    return maxPoolSize;
  }

  public void setMaxPoolSize(int maxPoolSize) {
    this.maxPoolSize = maxPoolSize;
  }

  @Override
  public String toString() {
    return joinOn(this.getClass())
      .add("enabled=" + enabled)
      .add("deploymentAware=" + deploymentAware)
      .add("corePoolSize=" + corePoolSize)
      .add("maxPoolSize=" + maxPoolSize)
      .toString();
  }

}
