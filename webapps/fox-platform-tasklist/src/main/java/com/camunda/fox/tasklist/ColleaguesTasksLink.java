package com.camunda.fox.tasklist;

public class ColleaguesTasksLink extends TaskNavigationLink {

  private String colleagueId;

  public ColleaguesTasksLink(String label, long count, String colleagueId, boolean active) {
    super(label, count, active);
    this.colleagueId = colleagueId;
  }

  public void setColleagueId(String colleagueId) {
    this.colleagueId = colleagueId;
  }

  public String getColleagueId() {
    return colleagueId;
  }

  @Override
  public String toString() {
    return "ColleaguesTasksLink [colleagueId=" + colleagueId + ", label=" + label + ", active=" + active + "]";
  }

}
