'use strict';

var AuthenticationPage = require('../pages/authentication');

var authenticationPage = new AuthenticationPage();

describe('authentication page - ', function() {

  describe('tasklist login', function() {

    it('should present a login form', function() {

      console.log('\n' + 'tasklist login');

      // when
      authenticationPage.navigateTo({webapp: 'tasklist'});

      // then
      expect(authenticationPage.formElement().isDisplayed()).toBe(true);
      expect(authenticationPage.loginButton().isEnabled()).toBe(false);
      expect(authenticationPage.usernameInput().isDisplayed()).toBe(true);
      expect(authenticationPage.passwordInput().isDisplayed()).toBe(true);
    });


    describe('invalid submission', function() {

      it('should enable login button', function() {

        // when
        authenticationPage.navigateTo(({webapp: 'tasklist'}));
        authenticationPage.usernameInput('jonny1');
        authenticationPage.passwordInput('yada-yada');

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

      beforeEach(function() {

        authenticationPage.navigateTo(({webapp: 'tasklist'}));
      });

      it('should login via login button', function() {

        // when
        authenticationPage.usernameInput('jonny1');
        authenticationPage.passwordInput('jonny1');
        authenticationPage.loginButton().click();

        // then
        expect(authenticationPage.formElement().isPresent()).toBe(false);
      });


      it('should login via ENTER key', function() {

        // when
        authenticationPage.usernameInput('jonny1');
        authenticationPage.passwordInput('jonny1');
        protractor.getInstance().actions().sendKeys(protractor.Key.ENTER).perform();

        // then
        expect(authenticationPage.formElement().isPresent()).toBe(false);
      });

    });

  });


  describe('cockpit login', function() {

    it('should present a login form', function() {

      console.log('\n' + 'cockpit login');

      // when
      authenticationPage.navigateTo({webapp: 'cockpit'});

      // then
      expect(authenticationPage.formElement().isDisplayed()).toBe(true);
      expect(authenticationPage.loginButton().isEnabled()).toBe(true);
      expect(authenticationPage.usernameInput().isDisplayed()).toBe(true);
      expect(authenticationPage.passwordInput().isDisplayed()).toBe(true);
    });


    describe('invalid submission', function() {

      it('should enable login button', function() {

        // when
        authenticationPage.navigateTo(({webapp: 'cockpit'}));
        authenticationPage.usernameInput('jonny1');
        authenticationPage.passwordInput('yada-yada');

        // then
        expect(authenticationPage.loginButton().isEnabled()).toBe(true);
      });


      it('should show error notification', function() {

        // when
        authenticationPage.loginButton().click();

        // then
        expect(authenticationPage.notifications().count()).toBe(2);
        expect(authenticationPage.notification()).toBe('Login is required to access the resource');
      });

    });


    describe('valid submission', function() {

      beforeEach(function() {

        authenticationPage.navigateTo(({webapp: 'cockpit'}));
      });

      it('should login via login button', function() {

        // when
        authenticationPage.usernameInput('jonny1');
        authenticationPage.passwordInput('jonny1');
        authenticationPage.loginButton().click();

        // then
        expect(authenticationPage.formElement().isPresent()).toBe(false);
        expect(authenticationPage.loggedInUser()).toBe('jonny1');
      });


      it('should login via ENTER key', function() {

        // when
        authenticationPage.usernameInput('jonny1');
        authenticationPage.passwordInput('jonny1');
        protractor.getInstance().actions().sendKeys(protractor.Key.ENTER).perform();

        // then
        expect(authenticationPage.formElement().isPresent()).toBe(false);
        expect(authenticationPage.loggedInUser()).toBe('jonny1');
      });

    });

    it('should logout', function() {

      // when
      authenticationPage.logout();

      // then
      expect(authenticationPage.formElement().isDisplayed()).toBe(true);
      expect(authenticationPage.loginButton().isEnabled()).toBe(true);
      expect(authenticationPage.usernameInput().isDisplayed()).toBe(true);
      expect(authenticationPage.passwordInput().isDisplayed()).toBe(true);
    });

  });


  describe('admin login', function() {

    it('should present a login form', function() {

      console.log('\n' + 'admin login');

      // when
      authenticationPage.navigateTo({webapp: 'admin'});

      // then
      expect(authenticationPage.formElement().isDisplayed()).toBe(true);
      expect(authenticationPage.loginButton().isEnabled()).toBe(true);
      expect(authenticationPage.usernameInput().isDisplayed()).toBe(true);
      expect(authenticationPage.passwordInput().isDisplayed()).toBe(true);
    });


    describe('invalid submission', function() {

      it('should enable login button', function() {

        // when
        authenticationPage.navigateTo(({webapp: 'admin'}));
        authenticationPage.usernameInput('jonny1');
        authenticationPage.passwordInput('yada-yada');

        // then
        expect(authenticationPage.loginButton().isEnabled()).toBe(true);
      });


      it('should show error notification', function() {

        // when
        authenticationPage.loginButton().click();

        // then
        expect(authenticationPage.notifications().count()).toBe(2);
        expect(authenticationPage.notification(1)).toBe('Wrong credentials or missing access rights to application');
      });

    });


    describe('valid submission', function() {

      beforeEach(function() {

        authenticationPage.navigateTo(({webapp: 'admin'}));
      });

      it('should login via login button', function() {

        // when
        authenticationPage.usernameInput('jonny1');
        authenticationPage.passwordInput('jonny1');
        authenticationPage.loginButton().click();

        // then
        expect(authenticationPage.formElement().isPresent()).toBe(false);
        expect(authenticationPage.loggedInUser()).toBe('jonny1');
      });


      it('should login via ENTER key', function() {

        // when
        authenticationPage.usernameInput('jonny1');
        authenticationPage.passwordInput('jonny1');
        protractor.getInstance().actions().sendKeys(protractor.Key.ENTER).perform();

        // then
        expect(authenticationPage.formElement().isPresent()).toBe(false);
        expect(authenticationPage.loggedInUser()).toBe('jonny1');
      });

    });


    it('should logout', function() {

      // when
      authenticationPage.logout();

      // then
      expect(authenticationPage.formElement().isDisplayed()).toBe(true);
      expect(authenticationPage.loginButton().isEnabled()).toBe(true);
      expect(authenticationPage.usernameInput().isDisplayed()).toBe(true);
      expect(authenticationPage.passwordInput().isDisplayed()).toBe(true);
    });

  });

});