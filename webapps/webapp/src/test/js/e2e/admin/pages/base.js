'use strict';

var Page = require('../../page');

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
      item = element(by.css('.navbar ul li:nth-child(' + itemIndex + ')'));
    else
      item = element(by.css('.navbar ul li:nth-child(1)'));

    item.click();
    return item;
  },

  logout: function() {
    element(by.css('.navbar [sem-show-user-actions]')).click();
    element(by.css('.navbar [sem-log-out]')).click();
  }  

});
