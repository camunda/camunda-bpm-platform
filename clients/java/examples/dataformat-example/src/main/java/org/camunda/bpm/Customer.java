package org.camunda.bpm;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Customer {
  
  private String firstName;
  private String lastName;
  private String gender;
  private Integer age;
  private Boolean isValid;
  private Date validationDate;
  
  
  public String getFirstName() {
    return firstName;
  }
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }
  public String getLastName() {
    return lastName;
  }
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }
  public String getGender() {
    return gender;
  }
  public void setGender(String gender) {
    this.gender = gender;
  }
  public Integer getAge() {
    return age;
  }
  public void setAge(Integer age) {
    this.age = age;
  }
  public Boolean getIsValid() {
    return isValid;
  }
  public void setIsValid(Boolean isValid) {
    this.isValid = isValid;
  }
  public Date getValidationDate() {
    return validationDate;
  }
  public void setValidationDate(Date validationDate) {
    this.validationDate = validationDate;
  }
}
