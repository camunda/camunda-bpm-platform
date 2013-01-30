'use strict';

/* http://docs.angularjs.org/guide/dev_guide.e2e-testing */

describe('error messages', function() {

  var newButtonSelect = 'a.btn[data-ng-click="createNew()"]';
  var saveButtonSelect = 'button.btn[data-ng-click="save()"]';
  var testRoundtripName = "RoundtripError " +Math.floor(Math.random()*10000);
  
  var signavioConnectorName = "sigi";
  var signavioConnectorUser = "test@camunda.com";
  var signavioConnectorPassword = "testtest";
  var signavioCorrectHost = "vm2.camunda.com:8080";
  var signavioWrongHost = "vm2.camunda.com:808";
  var signavioConnectorUrl = "http://" + signavioCorrectHost;
  var signavioConnectorWrongUrl = "http://" + signavioWrongHost;
  
  describe('display connection timed out error message', function() {

  	it('should create a signavio connector configuration', function() {
  	  browser().navigateTo('../app/secured/view/connectors');
  	  element(newButtonSelect).click();
  	  expect(browser().window().path()).toBe("/cycle/app/secured/view/connectors/");
  	  expect(element(newButtonSelect).count()).toBe(1);
  	  // select signavio connector
  	  select('blueprint').option(1);
  		expect(element('#signavioBaseUrl').val()).toBe('https://editor.signavio.com/');
  		// input configuration data
  		input('editConnectorConfiguration.name').enter(signavioConnectorName);
  		select('editConnectorConfiguration.loginMode').option('GLOBAL');
  		expect(element('#userName').count()).toBe(1);
  		expect(element('#password').count()).toBe(1);
  		input('editConnectorConfiguration.user').enter(signavioConnectorUser);
  		input('editConnectorConfiguration.password').enter(signavioConnectorPassword);
  		input('editConnectorConfiguration.properties[propertyName]').enter(signavioConnectorUrl);
  		expect(element('#signavioBaseUrl').val()).toBe(signavioConnectorUrl);
  		// save connector settings
  		element(saveButtonSelect).click();
  		expect(repeater('table tbody tr', 'Connector configurations').column('connectorConfiguration.name')).toContain(signavioConnectorName);
  	});
  	
  	it('should add a new roundtrip', function() {
      browser().navigateTo('../app/secured/view/index');
      element(newButtonSelect).click();
      input('newRoundtrip.name').enter(testRoundtripName);
      sleep(2);
      element('#saveRoundtripButton').click();
      sleep(2);
      // There should be 2 links now, own in the bread crumb, one in the list
      expect(element('a:contains('+testRoundtripName+')').count()).toBe(2);
    });
  	
  	it('should add a diagram to roundtrip', function() {
  	  var addLhsProcessModelButton = 'bpmn-diagram[identifier="leftHandSide"] a';
  	  var saveDiagramButton = 'modal-dialog[model="editDiagramDialog"] button[data-ng-click="save()"]';
  	  
  	  element(addLhsProcessModelButton).click();
  	  input('editDiagram.modeler').enter(signavioConnectorName);
  	  select('connector').option(0);
  	  sleep(5);
  	  // click My Documents
  	  element('#connectorTree div.dijitTreeContainer[role="tree"] span:contains("Meine Dokumente"):first').click();
  	  sleep(5);
  	  // click first folder
  	  element('#connectorTree div.dijitTreeContainer[role="tree"] span:contains("TestFolder"):first').click();
  	  sleep(5);
  	  // click first model
  	  element('#connectorTree div.dijitTreeContainer[role="tree"] span:contains("SubprocessBoundaryEventBug"):first').click();
  	  sleep(2);
  	  expect(element('input[data-ng-model="selectedNode.label"]').val()).toBe('SubprocessBoundaryEventBug');
  	  element(saveDiagramButton).click();
  	  sleep(2);
  	  expect(element('bpmn-diagram[identifier="leftHandSide"] h3.ng-binding').text()).toMatch(new RegExp(signavioConnectorName));
    });
  	
  	it('should break the signavio connector configuration', function() {
      browser().navigateTo('../app/secured/view/connectors');
      expect(browser().window().path()).toBe("/cycle/app/secured/view/connectors/");
      element('a[data-ng-click="editConnector(connectorConfiguration)"]').click();
      expect(element('#connectorLabel').val()).toBe(signavioConnectorName);
      
      input('editConnectorConfiguration.properties[propertyName]').enter(signavioConnectorWrongUrl);
      expect(element('#signavioBaseUrl').val()).toBe(signavioConnectorWrongUrl);
      element(saveButtonSelect).click();
    });
  	
  	it('should error message contain wrong signavio host being thrown', function() {
  	  browser().navigateTo('../app/secured/view/index');
  	  expect(element('a:contains(' + testRoundtripName + ')').count()).toBe(1);
  	  element('a:contains(' + testRoundtripName + ')').click();
      sleep(5);
      expect(element('div.errorPanel').text()).toMatch(new RegExp(signavioWrongHost));
    });
  	
  	it('test resources should be cleared', function() {
  	  // delete created testRoundtrip
      element('a[data-ng-click="deleteRoundtrip()"]').click();
      element('button[data-ng-click="performDeletion()"]').click();
      element('button[data-ng-click="deleteRoundtripDialog.close()"]').click();
      expect(element('a:contains(' + testRoundtripName + ')').count()).toBe(0);
      
      // delete connector
      browser().navigateTo('../app/secured/view/connectors');
      element('a:contains("delete")').click();
      element('button[data-ng-click="performConnectorDeletion()"]').click();
      element('button[data-ng-click="deleteConnectorConfigurationDialog.close()"]');
      expect(repeater('table tbody tr', 'Connector configurations').count()).toBe(0);
      
  	});

  });
  
});
