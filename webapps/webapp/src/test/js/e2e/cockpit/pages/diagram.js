'use strict';

var Base = require('./base');

module.exports = Base.extend({

  diagramActivity: function(activityName) {
    return element(by.css('.process-diagram *[data-activity-id=' + '"' + activityName + '"' + ']'));
  },

  selectActivity: function(activityName) {
    this.diagramActivity(activityName).click();
  },

  deselectActivity: function(activityName) {
    var selectedActivity = this.isActivitySelected(activityName);

    protractor.getInstance().actions()
        .keyDown(protractor.Key.CONTROL)
        .click(selectedActivity)
        .keyUp(protractor.Key.CONTROL)
        .perform();
  },

  activitiySelectionState: function(activityName) {
    return this.diagramActivity(activityName).getAttribute('class');
  },

  isActivitySelected: function(activityName) {
    //return this.diagramActivity(activityName).getAttribute('class');
    expect(this.activitiySelectionState(activityName)).toMatch('activity-highlight');
  },

  isActivityNotSelected: function(activityName) {
    expect(this.activitiySelectionState(activityName)).not.toMatch('activity-highlight');
  }

});