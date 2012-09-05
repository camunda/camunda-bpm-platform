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
    
    it('should add a new roundtrip to the list when clicking on button', function() {
      var testRoundtripName="Roundtrip " +Math.floor(Math.random()*10000);
      
      element('a.btn[ng-click="createNew()"]').click();
      input('name').enter(testRoundtripName);
      sleep(2);
      element('#saveRoundtripButton').click();
      sleep(2);
      // There should be 2 links now, own in the bread crumb, one in the list
      expect(element('a:contains('+testRoundtripName+')').count()).toBe(2);
    });
    
  });
});
