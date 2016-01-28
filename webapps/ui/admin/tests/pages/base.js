'use strict';

var Page = require('../../../common/tests/pages/page');

var navigationSection = element(by.css('[ng-controller="NavigationController"]'));

module.exports = Page.extend({

  pageHeader: function() {
    return element(by.css('.page-header')).getText();
  },

  selectNavbarItem: function(navbarItem) {
    var index = [
      'Users',
      'Groups',
      'Authorizations',
      'System'
    ];
    var item;
    var itemIndex = index.indexOf(navbarItem) + 1;

    if (itemIndex)
      item = navigationSection.element(by.css('[cam-widget-header] ul li:nth-child(' + itemIndex + ')'));
    else
      item = navigationSection.element(by.css('[cam-widget-header] ul li:nth-child(1)'));

    item.click();
    return item;
  }

});
