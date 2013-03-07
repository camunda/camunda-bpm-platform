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
    List<String> argumentList = (List<String>) list;
    
    Set<String> setToCompare = new HashSet<String>(listToCompare);
    int initialSize = setToCompare.size();
    
    Set<String> argumentSet = new HashSet<String>(argumentList);
    
    // intersection of the two sets
    setToCompare.retainAll(argumentSet);
    
    if (initialSize == setToCompare.size()) {
      return true;
    } else {
      return false;
    }
  }

}
