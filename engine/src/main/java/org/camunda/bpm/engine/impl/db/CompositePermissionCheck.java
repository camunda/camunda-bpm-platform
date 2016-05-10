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

/**
 * @author Thorben Lindhauer
 *
 */
public class CompositePermissionCheck {

  protected boolean disjunctive;

  protected List<CompositePermissionCheck> compositeChecks = new ArrayList<CompositePermissionCheck>();

  protected List<PermissionCheck> atomicChecks = new ArrayList<PermissionCheck>();

  public CompositePermissionCheck() {
    this(true);
  }

  public CompositePermissionCheck(boolean disjunctive) {
    this.disjunctive = disjunctive;
  }

  public void addAtomicCheck(PermissionCheck permissionCheck) {
    this.atomicChecks.add(permissionCheck);
  }

  public void setAtomicChecks(List<PermissionCheck> atomicChecks) {
    this.atomicChecks = atomicChecks;
  }

  public void setCompositeChecks(List<CompositePermissionCheck> subChecks) {
    this.compositeChecks = subChecks;
  }

  public void addCompositeCheck(CompositePermissionCheck subCheck) {
    this.compositeChecks.add(subCheck);
  }

  /**
   * conjunctive else
   */
  public boolean isDisjunctive() {
    return disjunctive;
  }

  public List<CompositePermissionCheck> getCompositeChecks() {
    return compositeChecks;
  }

  public List<PermissionCheck> getAtomicChecks() {
    return atomicChecks;
  }

  public void clear() {
    compositeChecks.clear();
    atomicChecks.clear();
  }

  public List<PermissionCheck> getAllPermissionChecks() {
    List<PermissionCheck> allChecks = new ArrayList<PermissionCheck>();

    allChecks.addAll(atomicChecks);

    for (CompositePermissionCheck compositePermissionCheck : compositeChecks) {
      allChecks.addAll(compositePermissionCheck.getAllPermissionChecks());
    }

    return allChecks;

  }
}
