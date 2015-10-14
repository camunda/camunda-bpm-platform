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
package org.camunda.bpm.engine.impl.db;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.impl.db.CompositePermissionCheck;
import org.camunda.bpm.engine.impl.db.PermissionCheck;

/**
 * @author Thorben Lindhauer
 *
 */
public class PermissionCheckBuilder {

  protected List<PermissionCheck> atomicChecks = new ArrayList<PermissionCheck>();
  protected List<CompositePermissionCheck> compositeChecks = new ArrayList<CompositePermissionCheck>();
  protected boolean disjunctive = true;

  protected PermissionCheckBuilder parent;

  public PermissionCheckBuilder() {

  }

  public PermissionCheckBuilder(PermissionCheckBuilder parent) {
    this.parent = parent;
  }

  public PermissionCheckBuilder disjunctive() {
    this.disjunctive = true;
    return this;
  }

  public PermissionCheckBuilder conjunctive() {
    this.disjunctive = false;
    return this;
  }

  public PermissionCheckBuilder atomicCheck(Resource resource, String queryParam, Permission permission) {
    PermissionCheck permCheck = new PermissionCheck();
    permCheck.setResource(resource);
    permCheck.setResourceIdQueryParam(queryParam);
    permCheck.setPermission(permission);
    this.atomicChecks.add(permCheck);

    return this;
  }

  public PermissionCheckBuilder composite() {
    return new PermissionCheckBuilder(this);
  }

  public PermissionCheckBuilder done() {
    parent.compositeChecks.add(this.build());
    return parent;
  }

  public CompositePermissionCheck build() {
    validate();

    CompositePermissionCheck permissionCheck = new CompositePermissionCheck(disjunctive);
    permissionCheck.setAtomicChecks(atomicChecks);
    permissionCheck.setCompositeChecks(compositeChecks);

    return permissionCheck;
  }

  protected void validate() {
    if (!atomicChecks.isEmpty() && !compositeChecks.isEmpty()) {
      throw new ProcessEngineException("Mixed authorization checks of atomic and composite permissions are not supported");
    }
  }
}
