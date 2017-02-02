'use strict';

module.exports = [
  '$scope', 'Uri', 'exposeScopeProperties',
  ExternalTaskErrorMessageLink
];

function ExternalTaskErrorMessageLink($scope, Uri, exposeScopeProperties) {
  exposeScopeProperties($scope, this, ['taskId', 'historic']);

  this.Uri = Uri;
}

ExternalTaskErrorMessageLink.prototype.getStacktraceUrl = function() {
  var abstractUrl = 'engine://engine/:engine/external-task/' + this.taskId + '/errorDetails';

  if (this.historic) {
    abstractUrl = 'engine://engine/:engine/history/external-task-log/' + this.taskId + '/error-details';
  }

  return this.Uri.appUri(abstractUrl);
};
