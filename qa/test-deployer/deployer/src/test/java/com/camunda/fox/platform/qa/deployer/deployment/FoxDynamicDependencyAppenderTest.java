package com.camunda.fox.platform.qa.deployer.deployment;

import com.camunda.fox.platform.qa.deployer.sample.WithInvalidProcessDeployments;
import com.camunda.fox.platform.qa.deployer.sample.WithInheritedProcessDeployments;
import com.camunda.fox.platform.qa.deployer.sample.WithMixedProcessDeployments;
import com.camunda.fox.platform.qa.deployer.sample.WithProcessDeploymentsOnClassLevel;
import com.camunda.fox.platform.qa.deployer.sample.WithValidProcessDeployments;
import com.camunda.fox.platform.qa.deployer.sample.WithNoProcessDeployments;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author nico.rehwaldt@camunda.com
 */
public class FoxDynamicDependencyAppenderTest {
  
  @Test
  public void testParsingOfClassWithMixedProcessDeployments() throws Exception {
    
    // given
    FoxDynamicDependencyAppender dependencyAppender = new FoxDynamicDependencyAppender();
    TestClass testClass = new TestClass(WithMixedProcessDeployments.class);
    List<String> expectedResources = Arrays.asList(
                                      "processes/CdiResolvingBean.bpmn20.xml",
                                      "processes/DelegateExecution.bpmn20.xml");
    
    // when
    Set<String> resources = dependencyAppender.fetchAllProcessDeployments(testClass);
    
    // then
    assertThat(resources).isEqualTo(new HashSet<String>(expectedResources));
  }
  
  @Test
  public void testParsingOfClassWithProcessDeploymentsOnClassLevel() throws Exception {
    
    // given
    FoxDynamicDependencyAppender dependencyAppender = new FoxDynamicDependencyAppender();
    TestClass testClass = new TestClass(WithProcessDeploymentsOnClassLevel.class);
    List<String> expectedResources = Arrays.asList(
                                      "processes/CdiResolvingBean.bpmn20.xml",
                                      "processes/DelegateExecution.bpmn20.xml");
    
    // when
    Set<String> resources = dependencyAppender.fetchAllProcessDeployments(testClass);
    
    // then
    assertThat(resources).isEqualTo(new HashSet<String>(expectedResources));
  }
   
  @Test
  public void testParsingOfClassWithProcessDeploymentsOnMethodLevel() throws Exception {
    
    // given
    FoxDynamicDependencyAppender dependencyAppender = new FoxDynamicDependencyAppender();
    TestClass testClass = new TestClass(WithValidProcessDeployments.class);
    List<String> expectedResources = Arrays.asList(
                                      "processes/CdiResolvingBean.bpmn20.xml",
                                      "processes/DelegateExecution.bpmn20.xml",
                                      "processes/SimpleExpressionEvaluation.bpmn20.xml");
    
    // when
    Set<String> resources = dependencyAppender.fetchAllProcessDeployments(testClass);
    
    // then
    assertThat(resources).isEqualTo(new HashSet<String>(expectedResources));
  }
  
   
  @Test
  public void testParsingOfClassWithInheritedProcessDeployments() throws Exception {
    
    // given
    FoxDynamicDependencyAppender dependencyAppender = new FoxDynamicDependencyAppender();
    TestClass testClass = new TestClass(WithInheritedProcessDeployments.class);
    List<String> expectedResources = Arrays.asList(
                                      "processes/CdiResolvingBean.bpmn20.xml",
                                      "processes/DelegateExecution.bpmn20.xml");
    
    // when
    Set<String> resources = dependencyAppender.fetchAllProcessDeployments(testClass);
    
    // then
    assertThat(resources).isEqualTo(new HashSet<String>(expectedResources));
  }
  
  @Test
  public void testParsingOfClassWithoutProcessDeployments() throws Exception {
    
    // given
    FoxDynamicDependencyAppender dependencyAppender = new FoxDynamicDependencyAppender();
    TestClass testClass = new TestClass(WithNoProcessDeployments.class);
    
    // when
    Set<String> resources = dependencyAppender.fetchAllProcessDeployments(testClass);
    
    // then
    assertThat(resources).isEmpty();
  }
  
  @Test
  public void testJavaArchiveCreationFromClassWithProcessDeploments() throws Exception {
    
    // given
    FoxDynamicDependencyAppender dependencyAppender = new FoxDynamicDependencyAppender();
    TestClass testClass = new TestClass(WithValidProcessDeployments.class);
    
    List<String> expectedResources = Arrays.asList(
                                      "processes/CdiResolvingBean.bpmn20.xml",
                                      "processes/DelegateExecution.bpmn20.xml",
                                      "processes/SimpleExpressionEvaluation.bpmn20.xml");
    
    // when
    Set<String> resources = dependencyAppender.fetchAllProcessDeployments(testClass);
    JavaArchive processesArchive = dependencyAppender.toJavaArchive(resources);
    
    // then
    for (String s: expectedResources) {
      assertThat(processesArchive.get(s)).isNotNull();
    }
  }
  
  @Test
  public void testJavaArchiveCreationFromClassWithInvalidProcessDeployments() throws Exception {
    
    // given
    FoxDynamicDependencyAppender dependencyAppender = new FoxDynamicDependencyAppender();
    TestClass testClass = new TestClass(WithInvalidProcessDeployments.class);
    
    // when
    Set<String> resources = dependencyAppender.fetchAllProcessDeployments(testClass);
    
    try {
      dependencyAppender.toJavaArchive(resources);
      // then
      fail("expected exception to be raised");     
    } catch (Exception e) {
      // expected
    }
  }
  
  @Test
  @Ignore
  public void testActivitiCdiPackaging() throws Exception {
    
    // given
    JavaArchive archive = null; // FoxDynamicDependencyAppender.ACTIVITI_CDI_ARCHIVE;
    // TODO: FIX ME!
    
    // when
    Node beansXml = archive.get("META-INF/beans.xml");
    Node processEngineExtension = archive.get("META-INF/services/org.activiti.cdi.spi.ProcessEngineLookup");
    Node cdiExtension = archive.get("META-INF/services/javax.enterprise.inject.spi.Extension");
    Node activitiCdiPackage = archive.get("org/activiti/cdi");
    
    // then
    assertThat(processEngineExtension).isNotNull();
    assertThat(cdiExtension).isNotNull();
    assertThat(beansXml).isNotNull();
    
    assertThat(activitiCdiPackage).isNotNull();
  }
}
