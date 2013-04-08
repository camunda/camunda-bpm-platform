package org.camunda.bpm.example.invoice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineTestCase;

public class InvoiceTestCase extends ProcessEngineTestCase {
	
	@Deployment(resources="invoice.bpmn")
	public void testHappyPath() {
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("invoice");
		
		List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
		
		assertEquals(1, tasks.size());
		assertEquals("assignApprover", tasks.get(0).getTaskDefinitionKey());
		
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("approver", "somebody");
		taskService.complete(tasks.get(0).getId(), variables);

		tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
		
		assertEquals(1, tasks.size());
		assertEquals("approveInvoice", tasks.get(0).getTaskDefinitionKey());
		assertEquals("somebody", tasks.get(0).getAssignee());
		
		variables.clear();
		variables.put("approved", Boolean.TRUE);
		taskService.complete(tasks.get(0).getId(), variables);

		tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();

		assertEquals(1, tasks.size());
		assertEquals("prepareBankTransfer", tasks.get(0).getTaskDefinitionKey());
		taskService.complete(tasks.get(0).getId());
		
		assertProcessEnded(pi.getId());
	}

}
