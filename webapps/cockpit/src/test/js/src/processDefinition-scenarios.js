'use strict';

/* http://docs.angularjs.org/guide/dev_guide.e2e-testing */

describe('cockpit process-definition', function() {

  // how to make this dynamically?
  var processDefinitionId = '007bb4fe-8012-11e2-b1c4-001f3b473707';

  beforeEach(function() {
    browser().navigateTo('/cockpit/#/dashboard');
    sleep(1);
  });

  it('should redirect to #/dashboard when processDefinitionId is missing', function() {
    browser().navigateTo('/cockpit/#/process-definition');
    sleep(1);
    expect(browser().window().hash()).toBe("/dashboard");
  })

  it('should show process definition info', function() {
    browser().navigateTo('/cockpit/#/process-definition/' + processDefinitionId);
    sleep(1);
    expect(browser().window().hash()).toBe("/process-definition/" + processDefinitionId);
    expect(binding('processDefinition.name')).toBe('invoice receipt (fox)');
    //expect(binding('processDefinition.key')).toBe(null);
    expect(binding('processDefinition.version')).toBe('1');
    expect(binding('processDefinitionLatestVersionCount.count')).toBe('10');
    expect(binding('processDefinitionTotalCount.count')).toBe('10');
  });

  it('should render process definition diagram', function() {
    browser().navigateTo('/cockpit/#/process-definition/' + processDefinitionId);
    sleep(1);
    expect(element('#processDiagram svg', 'bpmnRenderer div').count()).toBe(1);
    expect(element('#processDiagram svg rect#svg_Process_Engine_1', 'process engine pool').count()).toBe(1);
  });

});
