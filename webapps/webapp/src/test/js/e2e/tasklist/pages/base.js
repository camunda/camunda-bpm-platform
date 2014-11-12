'use strict';

var Page = require('../../commons/pages/page');

var navigationSection = element(by.css('[id="cam-tasklist-navigation"]'));

module.exports = Page.extend({

  pageHeader: function() {
    return element(by.css('.navbar-header'));
  },

  pageHeaderName: function() {
    return this.pageHeader().element(by.css('.navbar-brand')).getAttribute('title');
  },

/*  selectNavbarItem: function(navbarItem) {
    var index = [
      'Layout',
      'Process',
      'Engine',
      'Account',
      'Webapps'
    ];
    var item;
    var itemIndex = index.indexOf(navbarItem) + 1;

    if (itemIndex)
      item = navigationSection.element(by.css('.navbar ul li:nth-child(' + itemIndex + ')'));
    else
      item = navigationSection.element(by.css('.navbar ul li:nth-child(1)'));

    item.click();
    return item;
  },*/

  selectNavbarItem: function(navbarItem) {
    var index = [
      'Layout',
      'Process',
      'Engine',
      'Account',
      'Webapps'
    ];
    var cssElement;
    var item;

    switch(index.indexOf(navbarItem)) {
      case 0:
        cssElement = '[cam-layout-switcher]';
        break;
      case 1:
        cssElement = '.process-definitions';
        break;
      case 2:
        cssElement = '.engine-select';
        break;
      case 3:
        cssElement = '.user-account';
        break;
      case 4:
        cssElement = '.app-switch';
        break;
      default:
        cssElement = '';
        console.log('cannot find navbar item');
    }
    item = navigationSection.element(by.css(cssElement));
    item.click();

    return item;
  },

  navigateLogout: function() {
    browser.get(this.url +'logout');
    expect(element(by.css('input[type="password"]')).isDisplayed()).toBe(true);
  }

});
