'use strict';

var Page = require('./repository-view');

module.exports = Page.extend({

  formElement: function() {
    return element(by.css('[cam-resources]'));
  },

  resourceList: function() {
    return this.formElement().all(by.repeater('(delta, resource) in resources'));
  },

  getResourceIndex: function(resourceName) {
    return this.findElementIndexInRepeater('(delta, resource) in resources', by.css('.name .resource'), resourceName).then(function(idx) {
      return idx;
    });
  },

  selectResource: function(idxOrName) {
    var self = this;
    function callPageObject(idx) {
      self.resourceList().get(idx).element(by.css('a')).click();
      self.waitForElementToBeVisible(element(by.css('[cam-resource-meta] .name')));
    }

    if (typeof idxOrName === 'number') {
      callPageObject.call(this, idxOrName);
    } else {
      this.getResourceIndex(idxOrName).then(callPageObject.bind(this));
    }
  },

  resourceName: function(idx) {
    return this.resourceList().get(idx).element(by.css('a')).getText();
  }

});
