'use strict'

var tasklistPage = require('../../tasklist/pages/dashboard');
var cockpitPage = require('../../cockpit/pages/dashboard');
var adminPage = require('../../admin/pages/users');

describe("all webapps tests", function () {

  describe("start test", function () {

    it("should login admin", function () {

      // when
      adminPage.navigateToWebapp('Admin');
      adminPage.authentication.userLogin('jonny1', 'jonny1');

      // then
      adminPage.isActive();
    });

  });

  describe("naviagtion bar", function () {

    it("should check logged in user - admin", function () {

      // when
      adminPage.navigateTo();

      // then
      expect(adminPage.loggedInUser()).toBe('jonny1');
    });

    it("should check logged in user - cockpit", function () {

      // when
      cockpitPage.navigateTo();

      // then
      expect(cockpitPage.loggedInUser()).toBe('jonny1');
    });

    it("should check logged in user - tasklist", function () {

      // when
      tasklistPage.navigateTo();

      // then
      expect(tasklistPage.loggedInUser()).toBe('jonny1');
    });
  });
});