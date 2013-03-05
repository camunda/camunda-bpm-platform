package org.camunda.bpm.engine.rest.dto.task;

/**
 * @author: drobisch
 */
public class GroupDto {
  private String id;
  private String name;

  public GroupDto(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}
