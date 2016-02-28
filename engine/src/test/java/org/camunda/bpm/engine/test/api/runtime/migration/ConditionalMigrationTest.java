package org.camunda.bpm.engine.test.api.runtime.migration;

import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.describeExecutionTree;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.Condition;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ExecutionTree;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class ConditionalMigrationTest {
	protected ProcessEngineRule rule = new ProcessEngineRule();
	protected MigrationTestRule testHelper = new MigrationTestRule(rule);
	
	@Rule
	public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

	@Test
	public void testCase1()
	{
		basicConditionalMigrationTestCase(120);
		basicConditionalMigrationTestCase(100);
		basicConditionalMigrationTestCase(90);
	}
	public void basicConditionalMigrationTestCase(int amount)
	{
		ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.BASIC_CONDITONAL_SOURCE_PROCESS);
		ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.BASIC_CONDITIONAL_TARGET_PROCESS);

		Condition largerOrderCondition = new Condition(){
			@Override
			public boolean shouldMap(DelegateExecution e) {
				return ((Integer)e.getVariable("amount")) > 100;
			}
		};
		Condition smallerOrderCondition = new Condition(){
			@Override
			public boolean shouldMap(DelegateExecution e) {
				return ((Integer)e.getVariable("amount")) <= 100;
			}
		};
		MigrationPlan migrationPlan = rule.getRuntimeService()
				.createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
				.mapActivitiesConditionally("freeShipment", "freeShipment", largerOrderCondition)
				.mapActivitiesConditionally("freeShipment", "costShipment", smallerOrderCondition)
				.build();
		assertNotNull(migrationPlan);
		
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("amount", amount);
		ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId(), variables);
		assertNotNull(processInstance);
		ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(processInstance.getId());
		assertNotNull(activityInstance);
		
		rule.getRuntimeService().executeMigrationPlan(migrationPlan, Collections.singletonList(processInstance.getId()));
		String variableName = "amount";
		int value = (Integer)rule.getRuntimeService().getVariable(processInstance.getId(), variableName);
		assertNotNull(value);
		ExecutionTree executionTree = null;
		if(value > 100)
		{
			executionTree = ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());
			assertNotNull(executionTree);
			assertThat(executionTree)
			.matches(
					describeExecutionTree("freeShipment")
					.done());
			assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

			ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
			assertThat(updatedTree).hasStructure(
					describeActivityInstanceTree(targetProcessDefinition.getId())
					.activity("freeShipment", testHelper.getSingleActivityInstance(activityInstance, "freeShipment").getId())
					.done());

			Task migratedTask = rule.getTaskService().createTaskQuery().singleResult();
			Assert.assertNotNull(migratedTask);
			assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());

			rule.getTaskService().complete(migratedTask.getId());
			testHelper.assertProcessEnded(processInstance.getId());
		}
		else
		{
			executionTree = ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());
			assertNotNull(executionTree);
			assertThat(executionTree)
			.matches(
					describeExecutionTree("costShipment")
					.done());
			assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());
			
			ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
			assertThat(updatedTree).hasStructure(
					describeActivityInstanceTree(targetProcessDefinition.getId())
					.activity("costShipment", testHelper.getSingleActivityInstance(activityInstance, "freeShipment").getId())
					.done());

			Task migratedTask = rule.getTaskService().createTaskQuery().singleResult();
			Assert.assertNotNull(migratedTask);
			assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());

			rule.getTaskService().complete(migratedTask.getId());
			testHelper.assertProcessEnded(processInstance.getId());
	
		}
	}
	
	@Test
	public void testCase2()
	{
		singleConditionMultipleProcessTestCase(true);
		singleConditionMultipleProcessTestCase(false);
		
	}
	public void singleConditionMultipleProcessTestCase(boolean valid)
	{
		ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SUB_PROCESS_CONDITIONAL_SOURCE_PROCESS);
		ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SUB_PROCESS_CONDITIONAL_TARGET_PROCESS);

		Condition trueCondition = new Condition(){
			@Override
			public boolean shouldMap(DelegateExecution e) {
				return ((Boolean)e.getVariable("documentvalid")) == true;
			}
		};
		Condition falseCondition = new Condition(){
			@Override
			public boolean shouldMap(DelegateExecution e) {
				return ((Boolean)e.getVariable("documentvalid")) == false;
			}
		};
		MigrationPlan migrationPlan = rule.getRuntimeService()
				.createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
				.mapActivities("subProcess", "subProcess")
				.mapActivitiesConditionally("TaskA", "TaskA", trueCondition) 
				.mapActivitiesConditionally("TaskA", "TaskC", falseCondition)
				.mapActivities("TaskB", "TaskB")
				.build();
		assertNotNull(migrationPlan);
		
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("documentvalid", valid);
		ProcessInstance processInstance =rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId(), variables);
		assertNotNull(processInstance);
		
		ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(processInstance.getId());
		rule.getRuntimeService().executeMigrationPlan(migrationPlan, Collections.singletonList(processInstance.getId()));
		String variableName = "documentvalid";
		boolean value = (Boolean)rule.getRuntimeService().getVariable(processInstance.getId(), variableName);
		assertNotNull(value);
		ExecutionTree executionTree = null;
		if(value == true)		
		{			
			executionTree = ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());
		    assertThat(executionTree)
		    .matches(
		    	   describeExecutionTree(null).scope()
		          .child(null).concurrent().noScope()
		          .child("TaskA").scope().up().up()
		          .child("TaskB").concurrent().noScope()
		      .done());
		    
			assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

			ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
		    assertThat(updatedTree).hasStructure(
		        describeActivityInstanceTree(targetProcessDefinition.getId())
		          .beginScope("subProcess", testHelper.getSingleActivityInstance(activityInstance, "subProcess").getId())
		            .activity("TaskA", testHelper.getSingleActivityInstance(activityInstance, "TaskA").getId())
		          .endScope()
		            .activity("TaskB", testHelper.getSingleActivityInstance(activityInstance, "TaskB").getId())
		        .done());
		    
			List<Task> migratedTasks = rule.getTaskService().createTaskQuery().list();
			assertEquals(2, migratedTasks.size());

			for (Task migratedTask : migratedTasks) {
				assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());
			}
			for (Task migratedTask : migratedTasks) {
				rule.getTaskService().complete(migratedTask.getId());
			}
		}
		else
		{
			executionTree = ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());
		    assertThat(executionTree)
		    .matches(
		    	   describeExecutionTree(null).scope()
		          .child(null).concurrent().noScope()
		          .child("TaskC").scope().up().up()
		          .child("TaskB").concurrent().noScope()
		      .done());
		    
			assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

			ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
		    assertThat(updatedTree).hasStructure(
		        describeActivityInstanceTree(targetProcessDefinition.getId())
		          .beginScope("subProcess", testHelper.getSingleActivityInstance(activityInstance, "subProcess").getId())
		            .activity("TaskC", testHelper.getSingleActivityInstance(activityInstance, "TaskA").getId())
		          .endScope()
		            .activity("TaskB", testHelper.getSingleActivityInstance(activityInstance, "TaskB").getId())
		        .done());
		    
			List<Task> migratedTasks = rule.getTaskService().createTaskQuery().list();
			assertEquals(2, migratedTasks.size());

			for (Task migratedTask : migratedTasks) {
				assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());
			}
			for (Task migratedTask : migratedTasks) {
				rule.getTaskService().complete(migratedTask.getId());
			}

		}
		testHelper.assertProcessEnded(processInstance.getId());
	}

	@Test
	public void testCase3()
	{
		multipleConditionMultipleProcessTestCase(true,true);
		multipleConditionMultipleProcessTestCase(true,false);
		multipleConditionMultipleProcessTestCase(false,true);
		multipleConditionMultipleProcessTestCase(false,false);
	}
	public void multipleConditionMultipleProcessTestCase(boolean firstcondition, boolean secondcondition)
	{
		ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.MULTIPLE_CONDITION_SOURCE_PROCESS);
		ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.MULTIPLE_CONDITION_TARGET_PROCESS);

		Condition firstTrueCondition = new Condition(){
			@Override
			public boolean shouldMap(DelegateExecution e) {
				return ((Boolean)e.getVariable("firstcondition")) == true;
			}
		};
		Condition firstFalseCondition = new Condition(){
			@Override
			public boolean shouldMap(DelegateExecution e) {
				return ((Boolean)e.getVariable("firstcondition")) == false;
			}
		};

		Condition secondTrueCondition = new Condition(){
			@Override
			public boolean shouldMap(DelegateExecution e) {
				return ((Boolean)e.getVariable("secondcondition")) == true;
			}
		};
		Condition secondFalseCondition = new Condition(){
			@Override
			public boolean shouldMap(DelegateExecution e) {
				return ((Boolean)e.getVariable("secondcondition")) == false;
			}
		};
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("firstcondition", firstcondition);
		variables.put("secondcondition", secondcondition);
		ProcessInstance processInstance =rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId(), variables);
		assertNotNull(processInstance);
		ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(processInstance.getId());
		assertNotNull(activityInstance);

		MigrationPlan migrationPlan = rule.getRuntimeService()
				.createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
				.mapActivities("subProcessOne", "subProcessOne")
				.mapActivitiesConditionally("TaskA", "TaskA", firstTrueCondition) 
				.mapActivitiesConditionally("TaskA", "TaskC", firstFalseCondition)
				.mapActivities("subProcessTwo", "subProcessTwo")
				.mapActivitiesConditionally("TaskB", "TaskB", secondTrueCondition)
				.mapActivitiesConditionally("TaskB", "TaskD", secondFalseCondition)
				.build();
		assertNotNull(migrationPlan);

		rule.getRuntimeService().executeMigrationPlan(migrationPlan, Collections.singletonList(processInstance.getId()));
		ExecutionTree executionTree = null;
		if(firstcondition == true && secondcondition == true)		
		{			
			executionTree = ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());
		    assertThat(executionTree)
		    .matches(
		    	   describeExecutionTree(null).scope()
		          .child(null).concurrent().noScope()
		          .child("TaskB").scope().up().up()
		          .child(null).concurrent().noScope()
		          .child("TaskA").scope()
		      .done());
		    
			assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

			ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
		    assertThat(updatedTree).hasStructure(
		        describeActivityInstanceTree(targetProcessDefinition.getId())
		          .beginScope("subProcessOne", testHelper.getSingleActivityInstance(activityInstance, "subProcessOne").getId())
		            .activity("TaskA", testHelper.getSingleActivityInstance(activityInstance, "TaskA").getId())
		          .endScope()
		          .beginScope("subProcessTwo", testHelper.getSingleActivityInstance(activityInstance, "subProcessTwo").getId())
		            .activity("TaskB", testHelper.getSingleActivityInstance(activityInstance, "TaskB").getId())
		           .endScope() 
		        .done());
		    
			List<Task> migratedTasks = rule.getTaskService().createTaskQuery().list();
			assertEquals(2, migratedTasks.size());

			for (Task migratedTask : migratedTasks) {
				assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());
			}
			for (Task migratedTask : migratedTasks) {
				rule.getTaskService().complete(migratedTask.getId());
			}
		}
		else if(firstcondition == true && secondcondition == false)
		{
			executionTree = ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());
		    assertThat(executionTree)
		    .matches(
		    	   describeExecutionTree(null).scope()
		          .child(null).concurrent().noScope()
		          .child("TaskD").scope().up().up()
		          .child(null).concurrent().noScope()
		          .child("TaskA").scope()
		      .done());
		    
			assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

			ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
		    assertThat(updatedTree).hasStructure(
		        describeActivityInstanceTree(targetProcessDefinition.getId())
		          .beginScope("subProcessOne", testHelper.getSingleActivityInstance(activityInstance, "subProcessOne").getId())
		            .activity("TaskA", testHelper.getSingleActivityInstance(activityInstance, "TaskA").getId())
		          .endScope()
		          .beginScope("subProcessTwo", testHelper.getSingleActivityInstance(activityInstance, "subProcessTwo").getId())
		            .activity("TaskD", testHelper.getSingleActivityInstance(activityInstance, "TaskB").getId())
		           .endScope() 
		        .done());
		    
			List<Task> migratedTasks = rule.getTaskService().createTaskQuery().list();
			assertEquals(2, migratedTasks.size());

			for (Task migratedTask : migratedTasks) {
				assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());
			}
			for (Task migratedTask : migratedTasks) {
				rule.getTaskService().complete(migratedTask.getId());
			}
		}
		else if(firstcondition == false && secondcondition == true)
		{
			executionTree = ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());
		    assertThat(executionTree)
		    .matches(
		    	   describeExecutionTree(null).scope()
		          .child(null).concurrent().noScope()
		          .child("TaskB").scope().up().up()
		          .child(null).concurrent().noScope()
		          .child("TaskC").scope()
		      .done());
		    
			assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

			ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
		    assertThat(updatedTree).hasStructure(
		        describeActivityInstanceTree(targetProcessDefinition.getId())
		          .beginScope("subProcessOne", testHelper.getSingleActivityInstance(activityInstance, "subProcessOne").getId())
		            .activity("TaskC", testHelper.getSingleActivityInstance(activityInstance, "TaskA").getId())
		          .endScope()
		          .beginScope("subProcessTwo", testHelper.getSingleActivityInstance(activityInstance, "subProcessTwo").getId())
		            .activity("TaskB", testHelper.getSingleActivityInstance(activityInstance, "TaskB").getId())
		           .endScope() 
		        .done());
		    
			List<Task> migratedTasks = rule.getTaskService().createTaskQuery().list();
			assertEquals(2, migratedTasks.size());

			for (Task migratedTask : migratedTasks) {
				assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());
			}
			for (Task migratedTask : migratedTasks) {
				rule.getTaskService().complete(migratedTask.getId());
			}
		}		
		else if(firstcondition == false && secondcondition == false)
		{
			executionTree = ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());
		    assertThat(executionTree)
		    .matches(
		    	   describeExecutionTree(null).scope()
		          .child(null).concurrent().noScope()
		          .child("TaskD").scope().up().up()
		          .child(null).concurrent().noScope()
		          .child("TaskC").scope()
		      .done());
		    
			assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

			ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
		    assertThat(updatedTree).hasStructure(
		        describeActivityInstanceTree(targetProcessDefinition.getId())
		          .beginScope("subProcessOne", testHelper.getSingleActivityInstance(activityInstance, "subProcessOne").getId())
		            .activity("TaskC", testHelper.getSingleActivityInstance(activityInstance, "TaskA").getId())
		          .endScope()
		          .beginScope("subProcessTwo", testHelper.getSingleActivityInstance(activityInstance, "subProcessTwo").getId())
		            .activity("TaskD", testHelper.getSingleActivityInstance(activityInstance, "TaskB").getId())
		           .endScope() 
		        .done());
		    
			List<Task> migratedTasks = rule.getTaskService().createTaskQuery().list();
			assertEquals(2, migratedTasks.size());

			for (Task migratedTask : migratedTasks) {
				assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());
			}
			for (Task migratedTask : migratedTasks) {
				rule.getTaskService().complete(migratedTask.getId());
			}

		}
		testHelper.assertProcessEnded(processInstance.getId());
	}
}