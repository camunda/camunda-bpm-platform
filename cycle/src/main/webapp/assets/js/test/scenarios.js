'use strict';

/* http://docs.angularjs.org/guide/dev_guide.e2e-testing */

describe('cycle', function() {

  beforeEach(function() {
    browser().navigateTo('../../app/secured/view/index');
  });
  
  it('should be on view/index page after navigation', function() {
    expect(browser().window().path()).toBe("/cycle/app/secured/view/index/");
  });

  describe('no-roundtrips-view', function() {

    beforeEach(function() {
      browser().navigateTo('#/');
    });

    it('should render no-roundtrips-view when user navigates to /', function() {
      expect(element('[ng-view] h1').text()).toMatch(/No roundtrip selected/);
    });

  });
  
  describe('add-roundtrip', function() {
    
    it('should contain add roundtrip button', function() {
      expect(element('a.btn[ng-click="createNew()"]').count()).toBe(1);
    });

    
    it('should contain additional tests', function() {
      expect(true).toBe(false);
    });
  });
});
