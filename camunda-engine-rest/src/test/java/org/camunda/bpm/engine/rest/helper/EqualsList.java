package org.camunda.bpm.engine.rest.helper;

import java.util.List;
import java.util.Set;

import org.mockito.ArgumentMatcher;

import com.google.common.collect.Sets;

public class EqualsList extends ArgumentMatcher<List<String>> {

  private List<String> listToCompare;
  
  public EqualsList(List<String> listToCompare) {
    this.listToCompare = listToCompare;
  }
  
  @Override
  public boolean matches(Object list) {
    List<String> argumentList = (List<String>) list;
    
    Set<String> setToCompare = Sets.newHashSet(listToCompare);
    Set<String> argumentSet = Sets.newHashSet(argumentList);
    
    Set<String> union = Sets.union(setToCompare, argumentSet);
    
    if (union.size() == setToCompare.size() && union.size() == argumentSet.size()) {
      return true;
    } else {
      return false;
    }
  }

}
