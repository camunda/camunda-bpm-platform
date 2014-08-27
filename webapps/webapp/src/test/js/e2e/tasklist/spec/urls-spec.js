'use strict';

var dashboardPage = require('../pages/dashboard');

describe('tasklist URLs - ', function() {

  describe('unauthenticated user', function() {
    var loginUrl = 'http://localhost:8080' + dashboardPage.url + 'login';
    var taskUrl;

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

      // then
      dashboardPage.isActive();

      // finally
      element(by.css('.tasks-list li:first-child .task a'))
        .getAttribute('href')
        .then(function(href) {
          taskUrl = href;
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
    });
  });

});
