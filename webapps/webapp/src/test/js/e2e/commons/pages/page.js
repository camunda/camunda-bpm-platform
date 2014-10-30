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
  expect(browser.getCurrentUrl()).toBe('http://localhost:8080' + injectParams(this.url, params));
};

Page.prototype.navigateToWebapp = function(appName) {
  browser.get('camunda/app/' + appName.toLowerCase() + '/');
  browser.driver.manage().window().maximize();

  var navbarName = element(by.css('.brand'));
  expect(navbarName.getText()).toEqual('camunda ' + appName);
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
  return this.notifications().get(item).element(by.binding('notification.message')).getText();
};

Page.prototype.logout = function() {
  element(by.css('.navbar [sem-show-user-actions]')).click();
  element(by.css('.navbar [sem-log-out]')).click();
};

Page.prototype.loggedInUser = function() {
  return element(by.css('.navbar [sem-show-user-actions]')).getText();
};

Page.prototype.switchWebapp = function(appName) {
  element(by.css('.navbar [sem-show-applications]')).click();
  element(by.css('.navbar [sem-jump-to-'+ appName + ']')).click();
};


module.exports = Page;
