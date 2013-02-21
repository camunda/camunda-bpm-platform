'use strict';

/* http://docs.angularjs.org/guide/dev_guide.e2e-testing */

describe('cockpit dashboard', function() {

  beforeEach(function() {
    browser().navigateTo('/cockpit/#/dashboard');
  });
  
  it('should redirect to #/dashboard', function() {

    expect(browser().window().path()).toBe("/cockpit/#/dashboard");

  });

  it('should show name of process 1', function() {
    expect(repeater('process-definition-tiles p', 'process definition tiles').column('processDefinition.name')).toContain('invoice receipt (fox)');
  });

   
});