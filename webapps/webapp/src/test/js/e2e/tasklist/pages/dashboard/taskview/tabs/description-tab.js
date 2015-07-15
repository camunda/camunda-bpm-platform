'use strict';

var Tab = require('./tab');

module.exports = Tab.extend({

  tabIndex: 3,

  descriptionFormElement: function() {
    return element(by.css('.description-pane'));
  },

  descriptionField: function() {
    return this.descriptionFormElement()
      .element(by.css('[ng-show="task.description"]')).getText();
  }

});
