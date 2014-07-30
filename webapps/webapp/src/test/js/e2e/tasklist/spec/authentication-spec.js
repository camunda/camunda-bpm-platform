'use strict';

var authenticationPage = require('../pages');

describe('tasklist authentication - ', function() {

    it('should present a login form', function() {

      // when
      authenticationPage.navigateTo();
      browser.driver.manage().window().maximize();

      // then
      expect(authenticationPage.formElement().isDisplayed()).toBe(true);
      expect(authenticationPage.loginButton().isEnabled()).toBe(false);
      expect(authenticationPage.usernameInput().isDisplayed()).toBe(true);
      expect(authenticationPage.passwordInput().isDisplayed()).toBe(true);
    });


    describe('invalid submission', function() {

      it('should enable login button', function() {

        // when
        authenticationPage.navigateTo();
        authenticationPage.usernameInput().sendKeys('jonny1');
        authenticationPage.passwordInput().sendKeys('yada-yada');

        // then
        expect(authenticationPage.loginButton().isEnabled()).toBe(true);
      });


      it('should show error notification', function() {

        // when
        authenticationPage.loginButton().click();

        // then
        expect(authenticationPage.notifications().count()).toBe(1);
        expect(authenticationPage.notification()).toBe('Cannot log in with those credentials.');
      });

    });


    describe('valid submission', function() {

      it('closes the login form', function() {

        // when
        authenticationPage.navigateTo();
        authenticationPage.userLogin('jonny1', 'jonny1');

        // then
        expect(authenticationPage.formElement().isPresent()).toBe(false);
      });

    });

});
