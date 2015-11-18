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
package org.camunda.bpm.engine.rest.helper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mockito.ArgumentMatcher;

public class EqualsList extends ArgumentMatcher<List<String>> {

  private List<String> listToCompare;

  public EqualsList(List<String> listToCompare) {
    this.listToCompare = listToCompare;
  }

  @Override
  public boolean matches(Object list) {
    if ((list == null && listToCompare != null) ||
        (list != null && listToCompare == null)) {
      return false;
    }

    if (list == null && listToCompare == null) {
      return true;
    }

    List<String> argumentList = (List<String>) list;

    Set<String> setToCompare = new HashSet<String>(listToCompare);
    Set<String> argumentSet = new HashSet<String>(argumentList);

    return setToCompare.equals(argumentSet);
  }

}
