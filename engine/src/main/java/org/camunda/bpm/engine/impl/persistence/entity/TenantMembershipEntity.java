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

import org.camunda.bpm.engine.impl.db.DbEntity;

/**
 * A relationship between a tenant and an user or a group.
 */
public class TenantMembershipEntity implements Serializable, DbEntity {

  private static final long serialVersionUID = 1L;

  protected TenantEntity tenant;
  protected UserEntity user;
  protected GroupEntity group;

  protected String id;

  public Object getPersistentState() {
    // entity is not updatable
    return TenantMembershipEntity.class;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public UserEntity getUser() {
    return user;
  }

  public void setUser(UserEntity user) {
    this.user = user;
  }

  public GroupEntity getGroup() {
    return group;
  }

  public void setGroup(GroupEntity group) {
    this.group = group;
  }

  public String getTenantId() {
    return tenant.getId();
  }

  public String getUserId() {
    if (user != null) {
      return user.getId();
    } else {
      return null;
    }
  }

  public String getGroupId() {
    if (group != null) {
      return group.getId();
    } else {
      return null;
    }
  }

  public TenantEntity getTenant() {
    return tenant;
  }

  public void setTenant(TenantEntity tenant) {
    this.tenant = tenant;
  }

  @Override
  public String toString() {
    return "TenantMembershipEntity [id=" + id + ", tenant=" + tenant + ", user=" + user + ", group=" + group + "]";
  }

}
