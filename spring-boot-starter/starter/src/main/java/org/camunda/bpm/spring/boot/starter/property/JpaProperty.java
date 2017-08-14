package org.camunda.bpm.spring.boot.starter.property;

import static org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties.joinOn;

public class JpaProperty {
  /**
   * enables JPA
   */
  private boolean enabled;

  /**
   * the JPA persistence unit name
   */
  private String persistenceUnitName;

  /**
   * close JPA entity manager
   */
  private boolean closeEntityManager = true;

  /**
   * handle transactions by JPA
   */
  private boolean handleTransaction = true;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getPersistenceUnitName() {
    return persistenceUnitName;
  }

  public void setPersistenceUnitName(String persistenceUnitName) {
    this.persistenceUnitName = persistenceUnitName;
  }

  public boolean isCloseEntityManager() {
    return closeEntityManager;
  }

  public void setCloseEntityManager(boolean closeEntityManager) {
    this.closeEntityManager = closeEntityManager;
  }

  public boolean isHandleTransaction() {
    return handleTransaction;
  }

  public void setHandleTransaction(boolean handleTransaction) {
    this.handleTransaction = handleTransaction;
  }

  @Override
  public String toString() {
    return joinOn(this.getClass())
      .add("enabled=" + enabled)
      .add("persistenceUnitName=" + persistenceUnitName)
      .add("closeEntityManager=" + closeEntityManager)
      .add("handleTransaction=" + handleTransaction)
      .toString();
  }

}
