'use strict';

var testHelper = require('../../../common/tests/test-helper');

var systemPage = require('../pages/system');

var setupFile = require('./system-setup');

testHelper.expectStringEqual = require('../../../common/tests/string-equal');

describe('Admin system Spec with normal user', function() {
  before(function() {
    return testHelper(setupFile.setup1, function() {
      // given
      systemPage.navigateToWebapp('Admin');
    })
  });

  it('should not show system navbar item for normal user', function() {
    // when
    systemPage.authentication.userLogin('ringo', 'cam123');

    // then
    expect(systemPage.checkNavbarItem('System').isPresent()).to.eventually.be.false
  });
});

describe('Admin system Spec', function() {

  before(function() {
    return testHelper(setupFile.setup1, function() {

      systemPage.navigateToWebapp('Admin');
      systemPage.authentication.userLogin('admin', 'admin');
    });
  });


  describe('navigate to system pages', function() {

    it('should navigate to system menu', function() {

      // when
      systemPage.selectNavbarItem('System');

      // then
      systemPage.general.isActive();
      systemPage.general.loggedInUser('admin');
      testHelper.expectStringEqual(systemPage.pageHeader(), 'System Settings');
    });


    it('should validate general page', function() {

      // when
      systemPage.selectSystemNavbarItem('General');

      // then
      systemPage.general.isActive();
      expect(systemPage.general.boxHeader()).to.eventually.eql('General Settings');
    });


    it('should validate metrics page', function() {

      // when
      systemPage.selectSystemNavbarItem('Execution Metrics');

      // then
      systemPage.executionMetrics.isActive();
      expect(systemPage.executionMetrics.boxHeader()).to.eventually.eql('Execution Metrics');
      expect(systemPage.executionMetrics.flowNodesResult()).to.eventually.eql('6');
      expect(systemPage.executionMetrics.decisionElementsResult()).to.eventually.eql('9');
    });


    it('should support time range', function() {

      // given
      // we are on the flow node count page

      // when
      systemPage.executionMetrics.startDateField().clear();
      systemPage.executionMetrics.endDateField().clear();

      systemPage.executionMetrics.startDateField('2014-01-01T00:00:00');
      systemPage.executionMetrics.endDateField('2014-12-31T23:59:59');
      systemPage.executionMetrics.refreshButton().click();

      // then
      // expect(systemPage.executionMetrics.resultField()).to.eventually.eql('0');
      expect(systemPage.executionMetrics.flowNodesResult()).to.eventually.eql('0');
      expect(systemPage.executionMetrics.decisionElementsResult()).to.eventually.eql('0');
    });
  });

});
