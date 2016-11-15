'use strict';

var testHelper = require('../../../common/tests/test-helper');
var setupFile = require('./dashboard-setup');

var dashboardPage = require('../pages/dashboard');


describe('Cockpit Dashboard Spec', function() {
  before(function() {
    return testHelper(setupFile.setup1, function() {
      dashboardPage.navigateToWebapp('Cockpit');
      dashboardPage.authentication.userLogin('admin', 'admin');
    });
  });

  it('should display all sections of the page in the header', function() {
    expect(dashboardPage.navbarItem(0).getText()).to.eventually.eql('Processes');
    expect(dashboardPage.navbarItem(1).getText()).to.eventually.eql('Decisions');
    expect(dashboardPage.navbarItem(2).getText()).to.eventually.eql('Human Tasks');
    expect(dashboardPage.navbarItem(3).getText()).to.eventually.eql('More');

    dashboardPage.navbarDropDown().click();
    expect(dashboardPage.navbarDropDownItem(0).getText()).to.eventually.eql('Deployments');
    expect(dashboardPage.navbarDropDownItem(1).getText()).to.eventually.eql('Batches');
  });


  it('should display information about actual state', function () {
    expect(element(by.css('.actual .process-instances .value')).getText()).to.eventually.eql('0');
    expect(element(by.css('.actual .open-human-tasks .value')).getText()).to.eventually.eql('0');
    expect(element(by.css('.actual .open-incidents .value')).getText()).to.eventually.eql('0');
  });

  it('should display information about deployed resources', function () {
    expect(element(by.css('.deployed .processes .value')).getText()).to.eventually.eql('1');
    expect(element(by.css('.deployed .decisions .value')).getText()).to.eventually.eql('0');
    expect(element(by.css('.deployed .cases .value')).getText()).to.eventually.eql('0');
    expect(element(by.css('.deployed .deployments .value')).getText()).to.eventually.eql('1');
  });



  describe('section toggle buttons', function() {
    it('can be used to hide a section', function() {
      var section = element(by.css('section.actual'));
      var btn = section.element(by.css('.section-toggle'));

      expect(section.element(by.css('.stats')).isPresent()).to.eventually.eql(true);
      expect(btn.isPresent()).to.eventually.eql(true);

      btn.click();

      expect(section.element(by.css('.stats')).isPresent()).to.eventually.eql(false);
    });


    it('retains the state after page reload', function() {
      browser.refresh();
      var section = element(by.css('section.actual'));

      expect(section.element(by.css('.stats')).isPresent()).to.eventually.eql(false);
    });


    it('can be used to show the sections again', function() {
      var section = element(by.css('section.actual'));
      var btn = section.element(by.css('.section-toggle'));

      expect(section.element(by.css('.stats')).isPresent()).to.eventually.eql(false);

      btn.click();

      expect(section.element(by.css('.stats')).isPresent()).to.eventually.eql(true);
    });
  });
});
