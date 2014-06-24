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
 @namespace cam.test.e2e.admin
 */

/**
 Admin specific utilities for protractor E2E testing
 @memberof cam.test.e2e
 @name utils
 @see https://github.com/angular/protractor/blob/master/docs/api.md for API
 */
var adminUtils = module.exports = require('./../utils');

/**
 Select user in user list
 @memberof cam.test.e2e.admin.adminUtils

 @param {number} rowIndex             - Row index in list to select
 @param {string} firstName            - The first name of the user (for validation)
 @param {string} lastName             - The last name of the user (for validation)
 @return {!webdriver.promise.Promise}  - A promise of the selected element
 */
adminUtils.selectUserProfile = function(rowIndex, firstName, lastName) {
  var user = element(by.repeater('user in userList').row(rowIndex).column('{{user.firstName}} {{user.lastName}}'));

  expect(user.getText()).toEqual(firstName + ' ' + lastName);
  user.click();
  return user;
}
/**
 Select item in Admin header navbar
 @memberof cam.test.e2e.admin.adminUtils

 @param {string} navbarItem
 @return {!webdriver.promise.Promise}  - A promise of the selected element
 */
adminUtils.selectAdminNavbarItem = function (navbarItem) {
  var index = [
    'Users',
    'Groups',
    'Authorization',
    'System'
  ];
  var item;
  var itemIndex = index.indexOf(navbarItem) + 1;

  if (itemIndex)
    item = element(by.css('.navbar ul li:nth-child(' + itemIndex + ')'));
  else
    item = element(by.css('.navbar ul li:nth-child(1)'));

  item.click();
  return item;
};

/**
 Select Profile in users side navbar
 @memberof cam.test.e2e.admin.adminUtils

 @param {string} navbarItem
 @return {!webdriver.promise.Promise}  - A promise of the selected element
 */
adminUtils.selectUserNavbarItem = function(navbarItem) {
  var index = [
    'Profile',
    'Account',
    'Groups'
  ];
  var item;
  var itemIndex = index.indexOf(navbarItem) + 1;

  if (itemIndex)
    item = element(by.css('.sidebar-nav ul li:nth-child(' + itemIndex + ')'));
  else
    item = element(by.css('.sidebar-nav ul li:nth-child(1)'));

  item.click();
  return item;
}