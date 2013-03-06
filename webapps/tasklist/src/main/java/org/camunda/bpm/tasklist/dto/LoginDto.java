/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.camunda.bpm.tasklist.dto;

/**
 *
 * @author nico.rehwaldt
 */
public class LoginDto {

  private String username;
  private String password;

  public void setPassword(String password) {
    this.password = password;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public String getUsername() {
    return username;
  }
}
