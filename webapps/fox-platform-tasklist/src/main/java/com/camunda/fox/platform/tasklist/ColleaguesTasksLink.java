package com.camunda.fox.platform.tasklist;

public class ColleaguesTasksLink extends TaskNavigationLink {

  private String colleagueId;

  public ColleaguesTasksLink(String label, String colleagueId, boolean active) {
    super(label, active);
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
