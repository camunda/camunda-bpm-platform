'use strict';

var Page = require('./../base');

var groupsSection = element(by.css('[provider=activeSettingsProvier]'));

module.exports = Page.extend({

  selectSystemNavbarItem: function(navbarItem) {
    return element(by.cssContainingText('aside li', navbarItem)).click();
  },

  boxHeader: function() {
    return groupsSection.element(by.css('div.h3')).getText();
  }
});
