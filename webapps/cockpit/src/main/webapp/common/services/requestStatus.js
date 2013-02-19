angular
.module('cockpit.service.request.status', [])
  /**
   * RequestStatus isBusy=true -> cockpit is processing an AJAX request
   */
  .factory('RequestStatus', function() {

    function RequestStatus() {
      
      var self = this;
      
      // bind watchCurrent to credentials to make it directly accessible
      // for scope.$watch(RequestStatus.watchBusy)
      self.watchBusy = function() {
        return self.busy;
      };      
    }

    RequestStatus.prototype = {
      isBusy: function() {
        return busy;
      },
      setBusy: function(busy) {
      this.busy = busy; 
      }    
    };

    return new RequestStatus();
  });