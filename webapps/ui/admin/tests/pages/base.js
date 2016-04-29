'use strict';

var Page = require('../../../common/tests/pages/page');

var navigationSection = element(by.css('[ng-controller="NavigationController"]'));

var index = [
  'Users',
  'Groups',
  'Tenants',
  'Authorizations',
  'System'
];

module.exports = Page.extend({

  pageHeader: function() {
    return element(by.css('.breadcrumbs-panel li.active .text')).getText();
  },

  boxHeader: function() {
    return element(by.css('.section-content div.h3, .section-content h3')).getText();
  },

  selectNavbarItem: function(navbarItem) {
    var item;
    var itemIndex = index.indexOf(navbarItem) + 1;


    if(!itemIndex)
      itemIndex = 1;

    item = navigationSection.element(by.css('[cam-widget-header] ul li:nth-child(' + itemIndex + ')'));
    item.click();

    return item;
  },

  checkNavbarItem: function(navbarItem) {
    var idx = index.indexOf(navbarItem) + 1;

    if(!idx)
      idx = 1;

    return navigationSection.element(by.css('[cam-widget-header] ul li:nth-child(' + idx + ')'))
  }



});
