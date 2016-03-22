'use strict';

function injectParams(url, params) {
  var u = url;

  // replace /a/:foo -> /a/1
  Object.keys(params || {}).forEach(function(p) {
    u = u.replace(':' + p, params[p]);
  });

  return u;
}

function Page() { }

Page.extend = function(data) {
  function SubPage() {}

  SubPage.extend = this.extend;
  SubPage.prototype = Object.create(this.prototype);
  SubPage.prototype.constructor = SubPage;

  Object.keys(data).forEach(function(k) {
    SubPage.prototype[k] = data[k];
  });

  return SubPage;
};

/*prototype functionality*/
Page.prototype.navigateTo = function(params) {
  browser.get(injectParams(this.url, params));
  browser.driver.manage().window().maximize();
};

Page.prototype.isActive = function(params) {
  expect(browser.getCurrentUrl()).to.eventually.eql('http://localhost:8080' + injectParams(this.url, params));
};

Page.prototype.navigateToWebapp = function(appName) {
  browser.get('camunda/app/' + appName.toLowerCase() + '/');
  browser.driver.manage().window().maximize();

  expect(this.navbarBrand().getText()).to.eventually.eql('Camunda ' + appName);
};

Page.prototype.navbarBrand = function() {
  return element(by.css('.navbar-brand'));
};

Page.prototype.waitForElementToBeVisible = function(element, max) {
  var EC = protractor.ExpectedConditions;
  var isVisible = EC.visibilityOf(element);

  max = max || 5000;
  browser.wait(isVisible, max);
};

Page.prototype.waitForElementToBePresent = function(selector, max) {
  var EC = protractor.ExpectedConditions;
  var isPresent = EC.presenceOf(element);

  max = max || 5000;
  browser.wait(isPresent, max);
};

Page.prototype.waitForElementToBeNotPresent = function(element, max) {
  var EC = protractor.ExpectedConditions;
  var isNotPresent = EC.not(EC.presenceOf(element));

  max = max || 5000;
  browser.wait(isNotPresent, max);
};

/* notification */
Page.prototype.notifications = function() {
  return element.all(by.repeater('notification in notifications'));
};

Page.prototype.notification = function(item) {
  item = item || 0;
  return this.notifications().get(item).element(by.css('.message')).getText();
};

Page.prototype.logout = function() {
  element(by.css('[cam-widget-header] .account')).click();
  element(by.css('[cam-widget-header] [ng-click="logout()"]')).click();
};

Page.prototype.loggedInUser = function() {
  return element(by.css('[cam-widget-header] .account')).getText();
};

Page.prototype.findElementIndexInRepeater = function(repeaterName, elementSelector, elementName) {
  var deferred = protractor.promise.defer();

  element.all(by.repeater(repeaterName)).then(function(arr) {
    var count = arr.length;

    function noElementFound() {
      count --;
      if (count === 0) {
        deferred.reject('element not found in repeater: ' + repeaterName);
      }
    }

    for (var i = 0; i < arr.length; i++) {
      (function(boundI) {
        arr[boundI].element(elementSelector).getText().then(function(nameText) {

          if (nameText === elementName) {
            deferred.fulfill(boundI);
          } else {
            noElementFound();
          }
        });
      })(i);
    }
  });
  return deferred;
};



Page.prototype.headerWidget = function () {
  return element(by.css('[cam-widget-header]'));
};

Page.prototype.hamburgerButton = function () {
  return this.headerWidget().element(by.css('.navbar-toggle'));
};


Page.prototype.accountDropdown = function () {
  return this.headerWidget().element(by.css('.account.dropdown'));
};

Page.prototype.accountDropdownButton = function () {
  return this.accountDropdown().element(by.css('.dropdown-toggle'));
};


Page.prototype.engineSelectDropdown = function () {
  return this.headerWidget().element(by.css('.engine-select.dropdown'));
};

Page.prototype.engineSelectDropdownButton = function () {
  return this.engineSelectDropdown().element(by.css('.dropdown-toggle'));
};


Page.prototype.appSwitchDropdown = function () {
  return this.headerWidget().element(by.css('.app-switch.dropdown'));
};

Page.prototype.appSwitchDropdownButton = function () {
  return this.appSwitchDropdown().element(by.css('.dropdown-toggle'));
};


module.exports = Page;
