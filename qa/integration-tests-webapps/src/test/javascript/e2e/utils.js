/* global
    describe: false,
    xdescribe: false,
    ddescribe: false,
    it: false,
    xit: false,
    element: false,
    expect: false,
    by: false,
    browser: false,
    beforeEach: false,
    afterEach: false
*/
/* jshint node: true, unused: false */
'use strict';

/**
  @namespace cam.test
 */

/**
  @namespace cam.test.e2e
 */


/**
  Utilities for protractor E2E testing
  @memberof cam.test.e2e
  @name utils
  @see https://github.com/angular/protractor/blob/master/docs/api.md for API
 */
var utils = module.exports = {};


/**
  Wait for an element to be present
  @memberof cam.test.e2e.utils

  @param {string} selector              - a CSS selctor
  @return {!webdriver.promise.Promise}  - A promise
 */
utils.waitForElementToBePresent = function(selector) {
  // maximum waiting time of 10 seconds
  var max = 10000;
  return browser
    .wait(function() {
      return element(by.css(selector)).isPresent();
    });
};

/**
  Maximize the browser window (unless explicitly told not to do so)
  and selects an activity element in a rendered diagram.
  @memberof cam.test.e2e.utils

  @param {string} activityName          - The name of activity element to select
  @param {?boolean} maximize            - Set to false if you want to avoid browser window to maximize
  @return {!webdriver.promise.Promise}  - A promise of the selected element
 */
utils.selectActivityInDiagram = function(activityName, maximize) {
  if (maximize !== false) {
    browser.driver.manage().window().maximize();
  }

  var activity = element(by.css('.process-diagram *[data-activity-id=' + '"' + activityName + '"' + ']'));
  activity.click();
  return activity;
};

/**
  Ensure proper session and go to a given URL
  @memberof cam.test.e2e.utils

  @param {string} url       - A URL (or a path) to go to when authenticated
  @param {?string} username - A username to authenticate against
  @param {?string} password - A password to authenticate with
 */
utils.loginAndGoTo = function(url, username, password) {
  username = username || 'jonny1';
  password = password || 'jonny1';

  browser.get(url).then(function() {
    element(by.binding('authentication.user.name'))
      .isPresent()
      .then(function(yepNope) {

        // TODO? check url against currentUrl?
        // browser.getCurrentUrl().then(function(currentUrl) {
        //   console.info('currentUrl', currentUrl);
        // });

        if (yepNope) {
          browser.get(url);
        }
        else {
          browser.get('/camunda');

          element(by.model('username')).clear();
          element(by.model('password')).clear();
          element(by.model('username')).sendKeys(username);
          element(by.model('password')).sendKeys(password);

          element(by.css('.btn-primary.btn-large'))
            .click()
            .then(function() {
              browser.get(url);
            });
        }
      });
  });
};

utils.expectLoginSuccessful = function(username) {
  var loggedInUserMenu = element(by.binding('authentication.user.name'));
  expect(loggedInUserMenu.getText()).toEqual(username);
  return loggedInUserMenu;
};

utils.expectLoginFailed = function() {
  var notification = element(by.binding('notification.message'));
  expect(notification.getText()).toEqual('Wrong credentials or missing access rights to application');
  return notification;
};

/**
  Utility to test login (or failure at login)
  @memberof cam.test.e2e.utils

  @param {?string} username - A username to authenticate against
  @param {?string} password - A password to authenticate with
  @param {?boolean} valid   - If explicitly set to false, will expect the login attempt to fail
 */
utils.login = function(username, password, valid) {
  username = username || 'jonny1';
  password = password || 'jonny1';
  browser.get('/camunda');

  element(by.model('username')).clear();
  element(by.model('password')).clear();
  element(by.model('username')).sendKeys(username);
  element(by.model('password')).sendKeys(password);

  var submitButton = element(by.css('.btn-primary.btn-large'));
  submitButton.click();

  if (valid !== false) {
    utils.expectLoginSuccessful(username);
  }
  else {
    utils.expectLoginFailed();
  }
};


