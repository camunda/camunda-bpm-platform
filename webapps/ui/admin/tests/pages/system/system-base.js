'use strict';

var Page = require('./../base');

var groupsSection = element(by.css('[provider=activeSettingsProvier]'));

module.exports = Page.extend({

  selectSystemNavbarItem: function(navbarItem) {
    return element(by.cssContainingText('.sidebar-nav li', navbarItem)).click();
  },

  boxHeader: function() {
    return groupsSection.element(by.css('div.h4')).getText();
  }
});
