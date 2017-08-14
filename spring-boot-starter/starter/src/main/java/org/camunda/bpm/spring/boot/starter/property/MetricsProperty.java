package org.camunda.bpm.spring.boot.starter.property;

import static org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties.joinOn;

public class MetricsProperty {

  private boolean enabled = Defaults.INSTANCE.isMetricsEnabled();
  private boolean dbReporterActivate = Defaults.INSTANCE.isDbMetricsReporterActivate();

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isDbReporterActivate() {
    return dbReporterActivate;
  }

  public void setDbReporterActivate(boolean dbReporterActivate) {
    this.dbReporterActivate = dbReporterActivate;
  }

  @Override
  public String toString() {
    return joinOn(this.getClass())
      .add("enabled=" + enabled)
      .add("dbReporterActivate=" + dbReporterActivate)
      .toString();
  }

}
