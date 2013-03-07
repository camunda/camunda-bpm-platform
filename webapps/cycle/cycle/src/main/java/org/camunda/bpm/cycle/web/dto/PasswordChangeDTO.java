package org.camunda.bpm.cycle.web.dto;

/**
 *
 * @author nico.rehwaldt
 */
public class PasswordChangeDTO {

  private String oldPassword;
  
  private String newPassword;

  public PasswordChangeDTO() {} 
  
  public PasswordChangeDTO(String oldPassword, String newPassword) {
    this.oldPassword = oldPassword;
    this.newPassword = newPassword;
  }

  public String getNewPassword() {
    return newPassword;
  }

  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }

  public String getOldPassword() {
    return oldPassword;
  }

  public void setOldPassword(String oldPassword) {
    this.oldPassword = oldPassword;
  }
}
