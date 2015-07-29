package org.camunda.bpm.example.invoice;

import static org.camunda.bpm.engine.variable.Variables.fileValue;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineTestCase;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;

public class InvoiceTestCase extends ProcessEngineTestCase {
	
	@Deployment(resources="invoice.bpmn")
	public void testHappyPath() {
		InputStream invoiceInputStream = InvoiceProcessApplication.class.getClassLoader().getResourceAsStream("invoice.pdf");
		VariableMap variables = Variables.createVariables()
				.putValue("creditor", "Great Pizza for Everyone Inc.")
				.putValue("amount", "30€")
				.putValue("invoiceNumber", "GPFE-23232323")
				.putValue("invoiceDocument", fileValue("invoice.pdf")
						.file(invoiceInputStream)
						.mimeType("application/pdf")
						.create());

		ProcessInstance pi = runtimeService.startProcessInstanceByKey("invoice", variables);
		
		List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
		
		assertEquals(1, tasks.size());
		assertEquals("assignApprover", tasks.get(0).getTaskDefinitionKey());

		variables.clear();
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

		Job archiveInvoiceJob = managementService.createJobQuery().singleResult();
		assertNotNull(archiveInvoiceJob);
		managementService.executeJob(archiveInvoiceJob.getId());

		assertProcessEnded(pi.getId());
	}

}
