package org.camunda.bpm.engine.rest.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.activiti.engine.task.Task;

@XmlRootElement(name = "data")
public class TaskDto {

  private String name;

  @XmlElement
  public String getName() {
    return name;
  }
  
  public static TaskDto fromTask(Task task) {
    TaskDto dto = new TaskDto();
    dto.name = task.getName();
    return dto;
  }
}
