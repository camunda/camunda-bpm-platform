'use strict';

/* http://docs.angularjs.org/guide/dev_guide.e2e-testing */

describe('cockpit dashboard', function() {

  beforeEach(function() {
    browser().navigateTo('/cockpit/#/dashboard');
    sleep(1);
  });
  
  it('should redirect to #/dashboard', function() {
    expect(browser().window().hash()).toBe("/dashboard");
  });

  it('should show name of process invoice receipt (fox)', function() {
    expect(repeater('.tile-grid', 'process definition tiles').column('statistic.definition.name')).toContain('invoice receipt (fox)');
  });
  
  it('should contain a shortcut process definition name', function() {
    expect(repeater('.tile-grid', 'process definition tiles').column('shortcutProcessDefinitionName(statistic.definition.name)')).toContain('Loan applicant, with a ve...');
  });
  
  it('should contain the process definition key as process definition name', function() {
    expect(repeater('.tile-grid', 'process definition tiles').column('statistic.definition.name')).toContain('loan_applicant_process');
  });
  
  it('should show the number of running instances for a process definition', function () {
    expect(repeater('.tile:first').column('statistic.instances')).toContain("3");
  });
  
  it('should navigate to process definition site', function () {
    element('.tile:first').click();
    expect(browser().window().hash()).toBe("/process-definition/5");
  });
  
});
