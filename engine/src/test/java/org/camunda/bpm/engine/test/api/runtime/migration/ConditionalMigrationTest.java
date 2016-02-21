package org.camunda.bpm.engine.test.api.runtime.migration;

import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.describeExecutionTree;
import static org.junit.Assert.*;
import java.util.Collections;
import java.util.List;
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
	public void basicConditionalMigrationTestCase()
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
		ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
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
					describeExecutionTree(null).scope().id(processInstance.getId())
					.child("freeShipment").scope()
					.done());
			assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

			ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
			assertThat(updatedTree).hasStructure(
					describeActivityInstanceTree(targetProcessDefinition.getId())
					.beginScope("subProcess")
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
					describeExecutionTree(null).scope().id(processInstance.getId())
					.child("costShipment").scope()
					.done());
			assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

			ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
			assertThat(updatedTree).hasStructure(
					describeActivityInstanceTree(targetProcessDefinition.getId())
					.beginScope("subProcess")
					.activity("costShipment", testHelper.getSingleActivityInstance(activityInstance, "costShipment").getId())
					.done());

			Task migratedTask = rule.getTaskService().createTaskQuery().singleResult();
			Assert.assertNotNull(migratedTask);
			assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());

			rule.getTaskService().complete(migratedTask.getId());
			testHelper.assertProcessEnded(processInstance.getId());
	
		}
	}
	@Test
	public void singleConditionMultipleExecutionPathTestCase()
	{
		ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SINGLE_CONDITIONAL_SOURCE_PROCESS);
		ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SINGLE_CONDITONAL_TARGET_PROCESS);

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
				.mapActivities("receiveLoanForm", "receiveLoanForm")
				.mapActivitiesConditionally("acceptLoan", "acceptLoan", trueCondition) 
				.mapActivitiesConditionally("givecheque", "givecheque", trueCondition)
				.mapActivitiesConditionally("acceptLoan", "rejectLoan", falseCondition)
				.build();
		assertNotNull(migrationPlan);

		ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
		assertNotNull(processInstance);
		ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(processInstance.getId());
		assertNotNull(activityInstance);
		
		rule.getRuntimeService().executeMigrationPlan(migrationPlan, Collections.singletonList(processInstance.getId()));

		String variableName = "documentvalid";
		boolean value = (Boolean)rule.getRuntimeService().getVariable(processInstance.getId(), variableName);
		assertNotNull(value);

		ExecutionTree executionTree = null; 
		if(value == true)		
		{			
			executionTree = ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());
			assertNotNull(executionTree);
			assertThat(executionTree)
			.matches(
					describeExecutionTree(null).scope().id(processInstance.getId())
					.child(null).scope()
					.child("receiveLoanForm").scope().id(activityInstance.getActivityInstances("receiveLoanForm")[0].getExecutionIds()[0])
					.child("acceptLoan").scope().id(activityInstance.getActivityInstances("acceptLoan")[0].getExecutionIds()[0])
					.child("giveCheque").scope().id(activityInstance.getActivityInstances("rejectLoan")[0].getExecutionIds()[0])
					.done());

			assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

			ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
			assertThat(updatedTree).hasStructure(
					describeActivityInstanceTree(targetProcessDefinition.getId())
					.beginScope("subProcess")
					.activity("receiveLoanForm", testHelper.getSingleActivityInstance(activityInstance, "receiveLoanForm").getId())
					.activity("acceptLoan", testHelper.getSingleActivityInstance(activityInstance, "acceptLoan").getId())
					.activity("giveCheque", testHelper.getSingleActivityInstance(activityInstance, "giveCheque").getId())
					.done());

			List<Task> migratedTasks = rule.getTaskService().createTaskQuery().list();
			assertEquals(3, migratedTasks.size());

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
					describeExecutionTree(null).scope().id(processInstance.getId())
					.child(null).scope()
					.child("receiveLoanForm").scope().id(activityInstance.getActivityInstances("receiveLoanForm")[0].getExecutionIds()[0])
					.child("rejectLoan").scope().id(activityInstance.getActivityInstances("rejectLoan")[0].getExecutionIds()[0])
					.done());

			assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

			ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
			assertThat(updatedTree).hasStructure(
					describeActivityInstanceTree(targetProcessDefinition.getId())
					.beginScope("subProcess")
					.activity("receiveLoanForm", testHelper.getSingleActivityInstance(activityInstance, "receiveLoanForm").getId())
					.activity("rejectLoan", testHelper.getSingleActivityInstance(activityInstance, "rejectLoan").getId())
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
	public void multipleConditionMultipleExecutionPathTestCase()
	{
		ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.MULTIPLE_CONDITION_SOURCE_PROCESS);
		ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.MULTIPLE_CONDITION_TARGET_PROCESS);

		Condition hungryTrueCondition = new Condition(){
			@Override
			public boolean shouldMap(DelegateExecution e) {
				return ((Boolean)e.getVariable("hungry")) == true;
			}
		};
		Condition hungryFalseCondition = new Condition(){
			@Override
			public boolean shouldMap(DelegateExecution e) {
				return ((Boolean)e.getVariable("hungry")) == false;
			}
		};

		Condition cashPaymentTrueCondition = new Condition(){
			@Override
			public boolean shouldMap(DelegateExecution e) {
				return ((Boolean)e.getVariable("cashinhand")) == true;
			}
		};
		Condition cashPaymentFalseCondition = new Condition(){
			@Override
			public boolean shouldMap(DelegateExecution e) {
				return ((Boolean)e.getVariable("cashinhand")) == false;
			}
		};

		ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
		assertNotNull(processInstance);
		ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(processInstance.getId());
		assertNotNull(activityInstance);

		MigrationPlan migrationPlan = rule.getRuntimeService()
				.createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
				.mapActivities("gotoRestaurant", "gotoRestaurant")
				.mapActivitiesConditionally("orderPizza", "orderPizza", hungryTrueCondition) 
				.mapActivitiesConditionally("orderPizza", "orderSnack", hungryFalseCondition)
				.mapActivities("eat", "eat")
				.mapActivitiesConditionally("payByCash", "payByCash", cashPaymentTrueCondition)
				.mapActivitiesConditionally("payByCash", "payByCard", cashPaymentFalseCondition)
				.build();
		assertNotNull(migrationPlan);

		rule.getRuntimeService().executeMigrationPlan(migrationPlan, Collections.singletonList(processInstance.getId()));

		String variableName = "hungry";
		boolean hungry = (Boolean)rule.getRuntimeService().getVariable(processInstance.getId(), variableName);
		assertNotNull(hungry);

		variableName = "cashInHand";
		boolean cashInHand = (Boolean)rule.getRuntimeService().getVariable(processInstance.getId(), variableName);
		assertNotNull(cashInHand);

		ExecutionTree executionTree = null; 
		if(hungry == true && cashInHand == true)		
		{			
			executionTree = ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());
			assertThat(executionTree)
			.matches(
					describeExecutionTree(null).scope().id(processInstance.getId())
					.child(null).scope()
					.child("gotoRestaurant").scope().id(activityInstance.getActivityInstances("gotoRestaurant")[0].getExecutionIds()[0])
					.child("orderPizza").scope().id(activityInstance.getActivityInstances("orderPizza")[0].getExecutionIds()[0])
					.child("eat").scope().id(activityInstance.getActivityInstances("eat")[0].getExecutionIds()[0])
					.child("payByCash").scope().id(activityInstance.getActivityInstances("payByCash")[0].getExecutionIds()[0])
					.done());

			assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

			ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
			assertThat(updatedTree).hasStructure(
					describeActivityInstanceTree(targetProcessDefinition.getId())
					.beginScope("subProcess")
					.activity("gotoRestaurant", testHelper.getSingleActivityInstance(activityInstance, "gotoRestaurant").getId())
					.activity("orderPizza", testHelper.getSingleActivityInstance(activityInstance, "orderPizza").getId())
					.activity("eat", testHelper.getSingleActivityInstance(activityInstance, "eat").getId())
					.activity("payByCash", testHelper.getSingleActivityInstance(activityInstance, "payByCash").getId())
					.done());

			List<Task> migratedTasks = rule.getTaskService().createTaskQuery().list();
			assertEquals(4, migratedTasks.size());

			for (Task migratedTask : migratedTasks) {
				assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());
			}
			for (Task migratedTask : migratedTasks) {
				rule.getTaskService().complete(migratedTask.getId());
			}
		}
		else if(hungry == true && cashInHand == false)
		{
			executionTree = ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());
			assertThat(executionTree)
			.matches(
					describeExecutionTree(null).scope().id(processInstance.getId())
					.child(null).scope()
					.child("gotoRestaurant").scope().id(activityInstance.getActivityInstances("gotoRestaurant")[0].getExecutionIds()[0])
					.child("orderPizza").scope().id(activityInstance.getActivityInstances("orderPizza")[0].getExecutionIds()[0])
					.child("eat").scope().id(activityInstance.getActivityInstances("eat")[0].getExecutionIds()[0])
					.child("payByCard").scope().id(activityInstance.getActivityInstances("payByCard")[0].getExecutionIds()[0])
					.done());

			assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

			ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
			assertThat(updatedTree).hasStructure(
					describeActivityInstanceTree(targetProcessDefinition.getId())
					.beginScope("subProcess")
					.activity("gotoRestaurant", testHelper.getSingleActivityInstance(activityInstance, "gotoRestaurant").getId())
					.activity("orderPizza", testHelper.getSingleActivityInstance(activityInstance, "orderPizza").getId())
					.activity("eat", testHelper.getSingleActivityInstance(activityInstance, "eat").getId())
					.activity("payByCard", testHelper.getSingleActivityInstance(activityInstance, "payByCard").getId())
					.done());

			List<Task> migratedTasks = rule.getTaskService().createTaskQuery().list();
			assertEquals(4, migratedTasks.size());

			for (Task migratedTask : migratedTasks) {
				assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());
			}
			for (Task migratedTask : migratedTasks) {
				rule.getTaskService().complete(migratedTask.getId());
			}
		}
		else if(hungry == false && cashInHand == true)
		{
			executionTree = ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());
			assertThat(executionTree)
			.matches(
					describeExecutionTree(null).scope().id(processInstance.getId())
					.child(null).scope()
					.child("gotoRestaurant").scope().id(activityInstance.getActivityInstances("gotoRestaurant")[0].getExecutionIds()[0])
					.child("orderSnack").scope().id(activityInstance.getActivityInstances("orderSnack")[0].getExecutionIds()[0])
					.child("eat").scope().id(activityInstance.getActivityInstances("eat")[0].getExecutionIds()[0])
					.child("payByCash").scope().id(activityInstance.getActivityInstances("payByCash")[0].getExecutionIds()[0])
					.done());

			assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

			ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
			assertThat(updatedTree).hasStructure(
					describeActivityInstanceTree(targetProcessDefinition.getId())
					.beginScope("subProcess")
					.activity("gotoRestaurant", testHelper.getSingleActivityInstance(activityInstance, "gotoRestaurant").getId())
					.activity("orderSnack", testHelper.getSingleActivityInstance(activityInstance, "orderSnack").getId())
					.activity("eat", testHelper.getSingleActivityInstance(activityInstance, "eat").getId())
					.activity("payByCash", testHelper.getSingleActivityInstance(activityInstance, "payByCash").getId())
					.done());

			List<Task> migratedTasks = rule.getTaskService().createTaskQuery().list();
			assertEquals(4, migratedTasks.size());

			for (Task migratedTask : migratedTasks) {
				assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());
			}
			for (Task migratedTask : migratedTasks) {
				rule.getTaskService().complete(migratedTask.getId());
			}
		}		
		else if(hungry == false && cashInHand == false)
		{
			executionTree = ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());
			assertThat(executionTree)
			.matches(
					describeExecutionTree(null).scope().id(processInstance.getId())
					.child(null).scope()
					.child("gotoRestaurant").scope().id(activityInstance.getActivityInstances("gotoRestaurant")[0].getExecutionIds()[0])
					.child("orderSnack").scope().id(activityInstance.getActivityInstances("orderSnack")[0].getExecutionIds()[0])
					.child("eat").scope().id(activityInstance.getActivityInstances("eat")[0].getExecutionIds()[0])
					.child("payByCard").scope().id(activityInstance.getActivityInstances("payByCard")[0].getExecutionIds()[0])
					.done());

			assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

			ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
			assertThat(updatedTree).hasStructure(
					describeActivityInstanceTree(targetProcessDefinition.getId())
					.beginScope("subProcess")
					.activity("gotoRestaurant", testHelper.getSingleActivityInstance(activityInstance, "gotoRestaurant").getId())
					.activity("orderSnack", testHelper.getSingleActivityInstance(activityInstance, "orderSnack").getId())
					.activity("eat", testHelper.getSingleActivityInstance(activityInstance, "eat").getId())
					.activity("payByCard", testHelper.getSingleActivityInstance(activityInstance, "payByCard").getId())
					.done());

			List<Task> migratedTasks = rule.getTaskService().createTaskQuery().list();
			assertEquals(4, migratedTasks.size());

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