/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.persistence.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.db.DbEntity;

import static org.camunda.bpm.engine.impl.util.EncryptionUtil.saltPassword;


/**
 * @author Tom Baeyens
 */
public class UserEntity implements User, Serializable, DbEntity, HasDbRevision {

  private static final long serialVersionUID = 1L;

  protected String id;
  protected int revision;
  protected String firstName;
  protected String lastName;
  protected String email;
  protected String password;
  protected String newPassword;
  protected String salt;
  protected Date lockExpirationTime;
  protected int attempts;

  public UserEntity() {
  }

  public UserEntity(String id) {
    this.id = id;
  }

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("firstName", firstName);
    persistentState.put("lastName", lastName);
    persistentState.put("email", email);
    persistentState.put("password", password);
    persistentState.put("salt", salt);
    return persistentState;
  }

  public int getRevisionNext() {
    return revision+1;
  }

  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
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
  public String getEmail() {
    return email;
  }
  public void setEmail(String email) {
    this.email = email;
  }
  public String getPassword() {
    return password;
  }
  public void setPassword(String password) {
    this.newPassword = password;
  }

  public String getSalt() {
    return this.salt;
  }
  public void setSalt(String salt) {
    this.salt = salt;
  }

  /**
   * Special setter for MyBatis.
   */
  public void setDbPassword(String password) {
    this.password = password;
  }

  public int getRevision() {
    return revision;
  }
  public void setRevision(int revision) {
    this.revision = revision;
  }

  public Date getLockExpirationTime() {
    return lockExpirationTime;
  }

  public void setLockExpirationTime(Date lockExpirationTime) {
    this.lockExpirationTime = lockExpirationTime;
  }

  public int getAttempts() {
    return attempts;
  }

  public void setAttempts(int attempts) {
    this.attempts = attempts;
  }

  public void encryptPassword() {
    if (newPassword != null) {
      salt = generateSalt();
      setDbPassword(encryptPassword(newPassword, salt));
    }
  }

  protected String encryptPassword(String password, String salt) {
    if (password == null) {
      return null;
    } else {
      String saltedPassword = saltPassword(password, salt);
      return Context.getProcessEngineConfiguration()
        .getPasswordManager()
        .encrypt(saltedPassword);
    }
  }

  protected String generateSalt() {
    return Context.getProcessEngineConfiguration()
      .getSaltGenerator()
      .generateSalt();
  }

  public String toString() {
    return this.getClass().getSimpleName()
           + "[id=" + id
           + ", revision=" + revision
           + ", firstName=" + firstName
           + ", lastName=" + lastName
           + ", email=" + email
           + ", password=" + password
           + ", salt=" + salt
           + ", lockExpirationTime=" + lockExpirationTime
           + ", attempts=" + attempts
           + "]";
  }

}
