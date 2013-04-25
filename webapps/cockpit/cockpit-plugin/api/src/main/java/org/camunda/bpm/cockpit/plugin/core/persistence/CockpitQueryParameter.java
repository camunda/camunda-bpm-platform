package org.camunda.bpm.cockpit.plugin.core.persistence;

import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;


public class CockpitQueryParameter<T> extends ListQueryParameterObject {

  protected boolean historyEnabled = true;
  
  public CockpitQueryParameter() {
  }
  
  public CockpitQueryParameter(int firstResult, int maxResults) {
    this.firstResult = firstResult;
    this.maxResults = maxResults;
  }

  public boolean isHistoryEnabled() {
    return historyEnabled;
  }

  public void setHistoryEnabled(boolean historyEnabled) {
    this.historyEnabled = historyEnabled;
  }
  
}
