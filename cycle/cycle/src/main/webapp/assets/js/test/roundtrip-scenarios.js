'use strict';

/* http://docs.angularjs.org/guide/dev_guide.e2e-testing */

describe('cycle roundtrips', function() {

  beforeEach(function() {
    browser().navigateTo('../app/secured/view/index');
  });
  
  it('should be on view/index page after navigation', function() {
    expect(browser().window().path()).toBe("/cycle/app/secured/view/index/");
  });

  describe('no-roundtrips-view', function() {

    beforeEach(function() {
      browser().navigateTo('#/');
    });

    it('should render no-roundtrips-view when user navigates to /', function() {
      expect(element('[data-ng-view] h1').text()).toMatch(/No roundtrip selected/);
    });

  });
  
  describe('add-roundtrip', function() {
    var addButtonSelect = 'a.btn[data-ng-click="createNew()"]';
    
    it('should contain add roundtrip button', function() {
      expect(element(addButtonSelect).count()).toBe(1);
    });
    
    it('should add a new roundtrip to the list when clicking on button', function() {
      var testRoundtripName="Roundtrip " +Math.floor(Math.random()*10000);
      
      element(addButtonSelect).click();
      input('newRoundtrip.name').enter(testRoundtripName);
      sleep(2);
      element('#saveRoundtripButton').click();
      sleep(2);
      // There should be 2 links now, own in the bread crumb, one in the list
      expect(element('a:contains('+testRoundtripName+')').count()).toBe(2);
    });
    
  });
   
});
