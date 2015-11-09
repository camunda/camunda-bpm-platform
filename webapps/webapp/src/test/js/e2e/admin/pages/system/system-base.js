'use strict';

var Page = require('./../base');

var groupsSection = element(by.css('section'));

module.exports = Page.extend({

  selectSystemNavbarItem: function(navbarItem) {
    var index = [
      'General',
      'Execution Metrics',
      'License Key'
    ];
    var item;
    var itemIndex = index.indexOf(navbarItem) + 2;

    if (itemIndex)
      item = groupsSection.element(by.css('.sidebar-nav ul li:nth-child(' + itemIndex + ')'));
    else
      item = groupsSection.element(by.css('.sidebar-nav ul li:nth-child(1)'));

    item.click();
    return item;
  },

  boxHeader: function() {
    return groupsSection.element(by.css('legend')).getText();
  }

});
