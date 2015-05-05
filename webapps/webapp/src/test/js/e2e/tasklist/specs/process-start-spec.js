/* jshint node: true, unused: false */
/* global __dirname: false, describe: false, beforeEach: false, before:false, it: false, browser: false,
          element: false, expect: false, by: false, protractor: false, driver: false, until: false */
'use strict';

var testHelper = require('../../test-helper');
var setupFile = require('./process-start-setup');

var page = require('../pages/dashboard');


function openDialogAndSelectProcess(name, done) {
  function select() {
    element(by.cssContainingText('.processes a', name)).click()
      .then(noErrorDone(done), done);
  }

  function open() {
    page.startProcess.navigationLinkElement().click()
      .then(function () {
        browser.sleep(1000).then(select, done);
      }, done);
  }

  function close() {
    element(by.css('[ng-click="$dismiss()"]')).click()
      .then(open, done);
  }

  element(by.css('.modal-content')).isDisplayed()
    .then(function (yepNope) {
      if (yepNope) {
        close();
      }
      else {
        open();
      }
    });
}


function noErrorDone(done) {
  return function () { done(); };
}

describe('Start task', function () {
  before(function() {
    return testHelper(setupFile);
  });


  describe('menu link', function () {
    before(function () {
      page.navigateToWebapp('Tasklist');
      page.authentication.userLogin('admin', 'admin');
    });

    it('can be found in navigation', function () {
      expect(page.startProcess.navigationLinkElement().isPresent())
        .to.eventually.eql(true);

      expect(page.startProcess.navigationLinkElement().isDisplayed())
        .to.eventually.eql(true);
    });
  });


  describe('process definitions list', function() {
    it('opens', function () {
      // when
      page.startProcess.navigationLinkElement().click();

      // then
      expect(page.startProcess.searchProcessInput().isDisplayed())
        .to.eventually.eql(true);
    });
  });


  describe('form', function () {
    describe('generic', function () {
      before(function (done) {
        openDialogAndSelectProcess('processWithSubProcess', done);
      });


      it('has a field for business key', function () {
        expect(page.startProcess.businessKeyField().isDisplayed())
          .to.eventually.eql(true);

        page.startProcess.businessKeyField().sendKeys('bububu-businessKey');
      });


      it('can add variables', function () {
        expect(page.startProcess.genericFormAddVariableButton().isDisplayed())
          .to.eventually.eql(true);

        // // when
        page.startProcess.genericFormAddVariableButton().click();

        // // then
        expect(page.startProcess.genericFormRowsCount()).to.eventually.eql(1);

        expect(page.startProcess.startButton().getAttribute('disabled'))
          .to.eventually.eql('true');
      });


      it('can be submitted', function () {
        // when
        page.startProcess.genericFormRowNameField(0).sendKeys('YadaYada');
        page.startProcess.genericFormRowTypeFieldSelect(0, 'String');
        page.startProcess.genericFormRowValueField(0).sendKeys('YuduYudu');

        expect(page.startProcess.startButton().getAttribute('disabled'))
          .to.eventually.eql(null);
      });
    });


    xdescribe('embedded', function () {
      before(function (done) {
        openDialogAndSelectProcess('examples', done);
      });


      it('has a field for business key', function () {
        expect(page.startProcess.businessKeyField().isDisplayed())
          .to.eventually.eql(true);
      });


      it('can be submitted');
    });


    xdescribe('generated (is not yet supported)', function () {
      before(function (done) {
        openDialogAndSelectProcess('Generated', done);
      });


      it('has a field for business key', function () {
        expect(page.startProcess.businessKeyField().isDisplayed())
          .to.eventually.eql(true);
      });


      it('can be submitted');
    });
  });
});
