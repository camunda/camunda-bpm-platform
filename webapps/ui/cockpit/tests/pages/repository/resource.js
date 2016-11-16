'use strict';

var Page = require('./repository-view');

module.exports = Page.extend({

  formElement: function() {
    return element(by.css('[cam-resource-wrapper]'));
  },

  noResourceInfoText: function() {
    return this.formElement().element(by.css('.no-resource')).getText();
  },

  waitForResourceDetailView: function() {
    var elementToWaitFor = this.resourceName();
    this.waitForElementToBeVisible(elementToWaitFor);
  },

  resourceName: function() {
    return this.formElement().element(by.css('h2.name')).getText();
  },

  resourceVersionElement: function() {
    return this.formElement().element(by.css('header .version'));
  },

  resourceVersion: function() {
    return this.resourceVersionElement().getText();
  },

  downloadButton: function() {
    return element(by.css('.download-resource'));
  },

  bpmnDiagramFormElement: function() {
    return element(by.css('[cam-widget-bpmn-viewer]'));
  },

  dmnDiagramFormElement: function() {
    return element(by.css('[cam-widget-dmn-viewer]'));
  },

  cmmnDiagramFormElement: function() {
    return element(by.css('[cam-widget-cmmn-viewer]'));
  },

  imageFormElement: function() {
    return element(by.css('.image-resource'));
  },

  unkownResourceFormElement: function() {
    return element(by.css('.unkown-resource'));
  }

});
