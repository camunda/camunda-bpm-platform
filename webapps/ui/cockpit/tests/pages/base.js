'use strict';

var Page = require('../../../common/tests/pages/page');

module.exports = Page.extend({

  suspendedBadge: function() {
    return element(by.css('.ctn-header .badge'));
  },

  breadCrumb: function(item) {
    // 0 = home
    // 1 = 1st bread crumb
    // ...

    item = item + 1;
    if(item == 2)
      return element(by.css('.breadcrumbs-panel ul li:nth-child(' + item + ')')).element(by.css('a'));
    else
      return element(by.css('.breadcrumbs-panel ul li:nth-child(' + item + ')'));
  },

  selectBreadCrumb: function(item) {
    this.breadCrumb(item).click();
  },

  navbarItem: function (idx) {
    return element(by.css('[cam-widget-header] [ng-transclude] > ul > li:nth-child(' + (idx + 1) + ')'));
  },

  navbarItemClick: function () {
    return this.navbarItem().element(by.css('a')).click();
  }
});
