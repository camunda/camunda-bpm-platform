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
