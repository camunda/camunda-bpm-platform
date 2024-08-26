package org.camunda.bpm.run.property;

public class CamundaBpmRunRestProperties {
  public static final String PREFIX = CamundaBpmRunProperties.PREFIX + ".rest";

  protected boolean disableWadl;

  public boolean isDisableWadl() {
    return disableWadl;
  }

  public void setDisableWadl(boolean disableWadl) {
    this.disableWadl = disableWadl;
  }

  @Override
  public String toString() {
    return "CamundaBpmRunRestProperties[" + "disableWadl=" + disableWadl + ']';
  }
}
