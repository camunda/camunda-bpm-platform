'use strict';

var Page = require('../../../common/tests/pages/page');

var navigationSection = element(by.css('[cam-widget-header]'));

module.exports = Page.extend({

  selectNavbarItem: function(navbarItem) {
    var index = [
      'Task',
      'Process',
      'Engine',
      'Account',
      'Webapps'
    ];
    var cssElement;
    var item;

    switch(index.indexOf(navbarItem)) {
      case 0:
        cssElement = '.create-task-action';
        break;
      case 1:
        cssElement = '.start-process-action';
        break;
      case 2:
        cssElement = '.engine-select';
        break;
      case 3:
        cssElement = '.account';
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

  logout: function() {
    this.selectNavbarItem('Account');
    element(by.css('[ng-click="logout()"]')).click();
  },

  navigateLogout: function() {
    browser.get(this.url +'logout');
  }

});
