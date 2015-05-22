'use strict'

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

Page.prototype.waitForElementToBePresent = function(selector, max) {
  // maximum waiting time of 2 seconds
  max = max || 2000;
  return browser
      .wait(function() {
        return element(by.css(selector)).isPresent();
      }, max, 'Waiting for element (with selector "'+ selector +'") took too long.');
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
        deferred.reject('element not found');
      }
    }

    for (var i = 0; i < arr.length; i++) {
      (function(boundI) {
        arr[boundI].element(elementSelector).getText().then(function(nameText) {

          if (nameText === elementName) {
            deferred.fulfill(boundI);
          } else {
            noElementFound();
          };
        });
      })(i);
    };
  });
  return deferred;
};


module.exports = Page;
