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

    it('should render view1 when user navigates to /view1', function() {
      expect(element('[ng-view] p:first').text()).
        toMatch(/partial for view 1/);
    });

  });
  
  describe('roundtrip-view', function() {

    beforeEach(function() {
      browser().navigateTo('#/');
    });

    it('should render view1 when user navigates to /view1', function() {
      expect(element('[ng-view] p:first').text()).
        toMatch(/partial for view 1/);
    });

  });
});
