'use strict';

var dashboardPage = require('../pages/dashboard');

describe('tasklist URLs - ', function() {

  describe('unauthenticated user', function() {
    var loginUrl = 'http://localhost:8080' + dashboardPage.url + 'login';
    var taskUrl;
    var taskName;

    it('redirects to /login', function() {

      // when
      dashboardPage.navigateTo();
      dashboardPage.navigateLogout();
      dashboardPage.navigateTo({some: 'thing'});

      // then'
      expect(browser.getCurrentUrl()).toBe(loginUrl);
    });


    it('can login', function() {

      // when
      dashboardPage.authentication.userLogin('jonny1', 'jonny1');
      dashboardPage.taskList.selectTask(0);

      // then
      browser.getCurrentUrl().then(function(urlName) {
        taskUrl = urlName;
        expect(urlName).toContain('camunda/app/tasklist/default/#/?filter=');
      });

      // finally
      dashboardPage.taskList.taskName(0).then(function(name) {
        taskName = name;
      });

    });


    it('can be redirected after login', function() {

      // when
      dashboardPage.navigateLogout();
      browser.get(taskUrl);

      // then
      expect(browser.getCurrentUrl()).toBe(loginUrl);
      dashboardPage.authentication.userLogin('demo', 'demo');
      expect(browser.getCurrentUrl()).toBe(taskUrl);
      expect(dashboardPage.currentTask.taskName()).toBe(taskName);
    });
  });

});
