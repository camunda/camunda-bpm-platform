ngDefine('camunda.common.services', function(module) {

  var Service = function () {
    function RequestStatus() {
      var self = this;

      // bind watchCurrent to credentials to make it directly accessible
      // for scope.$watch(RequestStatus.watchBusy)
      self.watchBusy = function() {
        return self.busy;
      };
    }

    RequestStatus.prototype.isBusy = function() {
      return this.busy;
    };

    RequestStatus.prototype.setBusy = function(busy) {
      this.busy = busy;
    };

    return new RequestStatus();
  };

  /**
   * RequestStatus isBusy=true -> application is processing an AJAX request
   */
  module
    .factory("RequestStatus", Service);

});
