package org.camunda.bpm.qa.upgrade.json.beans;

/**
 * @author Svetlana Dorokhova.
 */
public class SpecialCustomer extends RegularCustomer {

  private int personalNumber;

  public SpecialCustomer() {
  }

  public SpecialCustomer(String name, int personalNumber) {
    super(name, 0);
    this.personalNumber = personalNumber;
  }

  public int getPersonalNumber() {
    return personalNumber;
  }
  public void setPersonalNumber(int personalNumber) {
    this.personalNumber = personalNumber;
  }

}
