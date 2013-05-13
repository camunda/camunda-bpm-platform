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

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.mockito.ArgumentMatcher;

public class EqualsMap extends ArgumentMatcher<Map<String, Object>> {

  private Map<String, Object> mapToCompare;
  
  public EqualsMap(Map<String, Object> mapToCompare) {
    this.mapToCompare = mapToCompare;
  }
  
  @Override
  public boolean matches(Object argument) {
    if (argument == null) {
      if (mapToCompare == null) {
        return true;
      } else {
        return false;
      }
    }
    
    Map<String, Object> argumentMap = (Map<String, Object>) argument;
    
    Set<Entry<String, Object>> setToCompare = mapToCompare.entrySet();
    int initialSize = setToCompare.size();
    
    Set<Entry<String, Object>> argumentSet = argumentMap.entrySet();
    
    // intersection of the two sets
    setToCompare.retainAll(argumentSet);
    
    if (initialSize == setToCompare.size()) {
      return true;
    } else {
      return false;
    }
  }

}
