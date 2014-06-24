var utils = require('./../utils');
describe('Tasklist authentication', function() {
  var formElement, usernameInput, passwordInput;

  beforeEach(function() {
    browser.get('/camunda/app/tasklist/default');
    formElement = element(by.css('form[name="userLogin"]'));
    usernameInput = formElement.element(by.css('input[name="username"]'));
    passwordInput = formElement.element(by.css('input[name="password"]'));
    submitButton = formElement.element(by.css('[type="submit"]'));
  });

  afterEach(function() {
    browser.sleep(1000);
  });

  it('presents a login form', function() {
    expect(formElement.isDisplayed()).toBe(true);
    expect(submitButton.isEnabled()).toBe(false);
  });


  describe('login form', function() {
    it('has a username field', function() {
      expect(usernameInput.isDisplayed()).toBe(true);
    });


    it('has a password field', function() {
      expect(passwordInput.isDisplayed()).toBe(true);
    });


    describe('invalid submission', function() {
      it('shows an error message', function() {
        usernameInput.sendKeys('jonny1');
        passwordInput.sendKeys('yada-yada');

        expect(submitButton.isEnabled()).toBe(true);

        submitButton.click().then(function() {
          var messages = element.all(by.repeater('message in messages'));

          expect(formElement.isDisplayed()).toBe(true);

          expect(messages.count()).toBe(1)
          //expect(messages.get(0).isDisplayed()).toBe(true);

          //expect(messages.getAttribute('class')).toMatch('error');
        });
      });
    });


    describe('valid submission', function() {
      it('closes the login form', function() {
        usernameInput.sendKeys('jonny1');
        passwordInput.sendKeys('jonny1');

        expect(submitButton.isEnabled()).toBe(true);

        submitButton.click().then(function() {
          expect(formElement.isPresent()).toBe(false);
        });
      });
    });
  });
});
