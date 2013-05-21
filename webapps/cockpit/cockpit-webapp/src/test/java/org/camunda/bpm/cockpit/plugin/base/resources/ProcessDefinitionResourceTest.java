package org.camunda.bpm.cockpit.plugin.base.resources;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

import org.camunda.bpm.cockpit.plugin.base.persistence.entity.ProcessDefinitionDto;
import org.camunda.bpm.cockpit.plugin.test.AbstractCockpitPluginTest;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ProcessDefinitionResourceTest extends AbstractCockpitPluginTest {
  
  private ProcessDefinitionResource resource;
  
  @Before
  public void setUp() {
    resource = new ProcessDefinitionResource(getProcessEngine().getName()); 
  }
  
  @Test
  @Deployment(resources = {"org/camunda/bpm/cockpit/plugin/base/resource/invoice.bpmn"})
  public void shouldListOneProcessDefinitionName() {
    List<ProcessDefinitionDto> result = resource.getProcessDefinitions();
    
    assertThat(result).hasSize(1);
    
    ProcessDefinitionDto dto = result.get(0);
    
    assertThat(dto.getId()).isNotNull();
    assertThat(dto.getName()).isEqualTo("invoice receipt");
    assertThat(dto.getKey()).isEqualTo("invoice");
  }
  
  @Test
  @Deployment(resources = {"org/camunda/bpm/cockpit/plugin/base/resource/invoice.bpmn",
      "org/camunda/bpm/cockpit/plugin/base/resource/order.bpmn"})
  public void shouldListAllProcessDefinitionNames() {
    List<ProcessDefinitionDto> result = resource.getProcessDefinitions();
    
    assertThat(result).hasSize(2);
    
    for (ProcessDefinitionDto dto : result) {
      assertThat(dto.getId()).isNotNull();
      if (dto.getKey().equals("invoice")) {       
        assertThat(dto.getName()).isEqualTo("invoice receipt");
      } else if (dto.getKey().equals("order")) {
        assertThat(dto.getName()).isEqualTo("order goods");
      } else {
        Assert.fail("A non expected process definition with the name " + dto.getName() + " was deployed.");
      }
    }
  }
  
  @Test
  @Deployment(resources = {"org/camunda/bpm/cockpit/plugin/base/resource/order.bpmn"})
  public void shouldListProcessDefinitionNameOfLatestVersion() {
    org.camunda.bpm.engine.repository.Deployment deployment = getProcessEngine().getRepositoryService().createDeployment()
        .addClasspathResource("org/camunda/bpm/cockpit/plugin/base/resource/order_second_version.bpmn")
        .deploy();
    
    List<ProcessDefinitionDto> result = resource.getProcessDefinitions();
    
    assertThat(result).hasSize(1);
    
    ProcessDefinitionDto dto = result.get(0);
    
    assertThat(dto.getId()).isNotNull();
    assertThat(dto.getName()).isEqualTo("order goods process");
    assertThat(dto.getKey()).isEqualTo("order");
    
    getProcessEngine().getRepositoryService().deleteDeployment(deployment.getId(), true);
  }

}
