package org.camunda.bpm.qa.upgrade.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.value.builder.SerializedObjectValueBuilder;
import org.camunda.bpm.qa.upgrade.DescribesScenario;
import org.camunda.bpm.qa.upgrade.ScenarioSetup;
import org.camunda.bpm.qa.upgrade.json.beans.ObjectList;
import org.camunda.bpm.qa.upgrade.json.beans.Order;
import org.camunda.bpm.qa.upgrade.json.beans.OrderDetails;
import org.camunda.bpm.qa.upgrade.json.beans.RegularCustomer;
import static org.camunda.bpm.engine.variable.Variables.serializedObjectValue;
import static org.junit.Assert.assertEquals;

public class CreateProcessInstanceWithJsonVariablesScenario {

  @Deployment
  public static String deployProcess() {
    return "org/camunda/bpm/qa/upgrade/json/simpleProcess.bpmn20.xml";
  }

  @DescribesScenario("initProcessInstanceWithDifferentVariables")
  public static ScenarioSetup initProcessInstance() {
    return new ScenarioSetup() {
      public void execute(ProcessEngine engine, String scenarioName) {
        // given
        ProcessInstance processInstance = engine.getRuntimeService().startProcessInstanceByKey("Process", "processWithJsonVariables");
        // when
        Execution execution = engine.getRuntimeService().createExecutionQuery().processInstanceId(processInstance.getId()).singleResult();
        engine.getRuntimeService().setVariable(execution.getId(), "objectVariable", createObjectVariable());
        engine.getRuntimeService().setVariable(execution.getId(), "plainTypeArrayVariable", createPlainTypeArray());
        engine.getRuntimeService().setVariable(execution.getId(), "notGenericObjectListVariable", createNotGenericObjectList());
        engine.getRuntimeService().setVariable(execution.getId(), "serializedMapVariable", createSerializedMap());
      }
    };
  }

  public static Object createObjectVariable() {
    Order order = new Order();
    order.setId(1234567890987654321L);
    order.setOrder("order1");
    order.setDueUntil(20150112);
    order.setActive(true);

    OrderDetails orderDetails = new OrderDetails();
    orderDetails.setArticle("camundaBPM");
    orderDetails.setPrice(32000.45);
    orderDetails.setRoundedPrice(32000);

    List<String> currencies = new ArrayList<String>();
    currencies.add("euro");
    currencies.add("dollar");
    orderDetails.setCurrencies(currencies);

    order.setOrderDetails(orderDetails);

    List<RegularCustomer> customers = new ArrayList<RegularCustomer>();

    customers.add(new RegularCustomer("Kermit", 1354539722));
    customers.add(new RegularCustomer("Waldo", 1320325322));
    customers.add(new RegularCustomer("Johnny", 1286110922));

    order.setCustomers(customers);

    return order;
  }

  public static int[] createPlainTypeArray() {
    return new int[]{5, 10};
  }

  public static ObjectList createNotGenericObjectList() {
    ObjectList customers = new ObjectList();
    customers.add(new RegularCustomer("someCustomer", 5));
    customers.add(new RegularCustomer("secondCustomer", 666));
    return customers;
  }

  public static SerializedObjectValueBuilder createSerializedMap(){
    return serializedObjectValue("{\"foo\": \"bar\"}").serializationDataFormat("application/json").objectTypeName(HashMap.class.getName());
  }

}
