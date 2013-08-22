'use strict';

/* Services */

angular
  .module('cycle.services', ['ngResource'])
  .value('Debouncer', {
    /**
     * Debounce a function call, making it callable an arbitrary number of times before it is actually executed once. 
     * 
     * @param {function()} func The function to debounce.
     * @param {number} wait The debounce timeout.
     * @return {function()} A function that can be called an arbitrary number
     *         of times within the given time.
     */
    debounce: function(func, wait) {
      var timer;
      return function() {
        var context = this, args = arguments;
        clearTimeout(timer);
        timer = setTimeout(function() {
          timer = null;
          func.apply(context, args);
        }, wait);
      };
    }
  })
  /**
   * Provides the app service with the functions 
   * 
   *  root() -> returns the application root
   *  uri(str) -> returns a application root relative uri from the given argument
   */
  .factory('App', function() {
    function root() {
      return $("base").attr("app-base");
    }
    
    return {
      root: root, 
      uri: function(str) {
        return root() + (str.indexOf("/") == 0 ? str.substring(1, str.length) : str);
      }
    };
  })
  .factory('Roundtrip', function($resource, App) {
    return $resource(App.uri('secured/resource/roundtrip/:id'), {id: "@id"}, {});
  })
  .factory('User', function($resource, App) {
    return $resource(App.uri('secured/resource/user/:id'), {id: "@id"}, {});
  })  
  .factory('RoundtripDetails', function($resource, App) {
    return $resource(App.uri('secured/resource/roundtrip/:id/details'), {id: "@id"}, {});
  })
  .factory('ConnectorConfiguration', function($resource, App) {
    return $resource(App.uri('secured/resource/connector/configuration/:id'), {id: "@connectorId"}, {
      'queryDefaults':  {method:'GET', isArray:true, params: { id: 'defaults' }}
    });
  })
  .factory('ConnectorCredentials', function($resource, App) {
    return $resource(App.uri('secured/resource/connector/credentials/:id'), {id: "@id"}, {});
  })
  .factory('Commons', function($http, HttpUtils, App) {
    return {
      getModelerNames: function() {
        return HttpUtils.makePromise($http.get(App.uri('secured/resource/diagram/modelerNames')));
      },
      isImageAvailable : function (node) {
        var uri = "secured/resource/connector/" + node.connectorId + "/contents/info?type=PNG_FILE&nodeId=" + escape(node.id);
        return $http.get(App.uri(uri));
      },
      isContentAvailable: function (node) {
        var uri = "secured/resource/connector/" + node.connectorId + "/contents/info?nodeId=" + encodeURI(node.id);
        return $http.get(App.uri(uri));
      },
      getDiagramStatus: function(diagram) {
        var uri = "secured/resource/diagram/" + diagram.id + "/syncStatus";
        return $http.get(App.uri(uri));
      }, 
      getImageUrl: function (diagram, update) {
        var uri = App.uri("secured/resource/diagram/" + diagram.id + "/image");
        if (update) {
          uri +="?ts=" + new Date().getTime();
        }
        return uri;
      }
    };
  })
  .service('Connector', function($http, $q, App) {
    var connectorMap;
    var connectorMapLoadPromise;

    function withConnectors() {
      if (connectorMap) {
        return $q.when(connectorMap);
      }

      return initConnectors();
    }

    function loadConnectorMap() {
      var deferred = $q.defer();

      $http.get(App.uri("secured/resource/connector/list")).success(function(data) {
        var map = {};

        angular.forEach(data, function(connector) {
          map[connector.connectorId] = connector;
        });

        deferred.resolve(map);
      });

      return deferred.promise;
    }

    function supportsCommitMessages(connectorConfig) {
      var id = connectorConfig.connectorId;

      // is our connectorMap loaded
      if (connectorMap) {
        var connector = connectorMap[id];
        return connector && connector.supportsCommitMessage;
      } else
      if (connectorMapLoadPromise) {
        // nothing to do, already loading stuff
      } else {

        // must load connector information
        connectorMapLoadPromise = loadConnectorMap().then(function(map) {
          connectorMapLoadPromise = null;
          connectorMap = map;
        });
      }

      // connector not found
      // or connector map not yet loaded
      return false;
    }

	  return { 
      supportsCommitMessages : supportsCommitMessages
	  };
	  
  })
  .service('Error', function () {
    return {
      errors : [],
      errorConsumers : [],  
      addError: function (error) {
        this.errors.push(error); 
        this.errorConsumers[this.errorConsumers.length-1](error);
      },
      removeError: function(error) {
    	var idx = this.errors.indexOf(error);
    	  this.errors.splice(idx,1);  
      },
      removeAllErrors: function() {
        // not assigning a new [], because it still can be referenced somewhere => memory leak
        this.errors.length = 0;
      },
      registerErrorConsumer: function(callback) {
        this.errorConsumers.push(callback);  
      },
      unregisterErrorConsumer: function(callback) {
        this.errorConsumers.splice(this.errorConsumers.indexOf(callback),1);
      }      
    };
  })
  .service('HttpUtils', function($q) {
    return {
      makePromise: function(http) {
        var deferred = $q.defer();
        http.success(function() {
          deferred.resolve.apply(this, arguments);
        });
        http.error(function() {
          deferred.reject.apply(this, arguments);
        });
        return deferred.promise;
      }
    };
  })
  .factory('cycleHttpInterceptor', function($q, Error, RequestStatus) {
    return function(promise) {
    	
      RequestStatus.setBusy(true);  
      
      return promise.then(function (response, arg1, arg2)  {
        RequestStatus.setBusy(false);
        return promise;
        
      }, function (response) {    	  
        RequestStatus.setBusy(false);
    	
        if (parseInt(response.status) == 500) {
          if (response.data && response.data.message) {
            Error.addError({ "status" : "Error" , "config" :  response.data.message , "type" : response.data.exceptionType });
          } else {
            Error.addError({ "status" : "Error" , "config" :  "A problem occurred: try logging out of cycle and logging back in. If the problem persists, contact your administrator." });     
          }
          
        } else if (parseInt(response.status) == 0) {
          Error.addError({ "status" : "Request Timeout" , "config" :  "Your request timed out. Try refreshing the page." });
          
        } else if (parseInt(response.status) == 401) {
          Error.addError({ "status" : "Unauthorized" , "config" :  "Your session has probably expired. Try refreshing the page and login again." });
          
        } else {
          Error.addError({ "status" : "Error" , "config" :  "A problem occurred: try logging out of cycle and logging back in. If the problem persists, contact your administrator." });
        }         
        return $q.reject(response);
      });
    };
  })
  /**
   * Lists all available events used in cycle for emit and on
   */
  .factory('Event', function() {
    return {
      userChanged : "user-changed",
      navigationChanged : "navigation-changed",
      roundtripAdded : "roundtrip-added",
      roundtripChanged : "roundtrip-changed",
      modelImageClicked : "model-image-clicked",
      componentError : "component-error",
      selectedConnectorChanged : "selected-connector-changed" ,
      destroy : "$destroy", // angular own event which is fired when current scope is destroyed
      ngChange : "change", // jquery + angularjs event
      imageAvailable :  "image-available"
    };
  })
  /**
   * Credentials (áka user management)
   */
  .factory('Credentials', function($http, App) {

    function Credentials() {
      this.currentCredentials = null;
      
      var self = this;
      
      // bind watchCurrent to credentials to make it directly accessible
      // for scope.$watch(Credentials.watchCurrent)
      self.watchCurrent = function() {
        return self.current();
      };
    }

    Credentials.prototype = {
      reload: function() {
        var self = this;
        
        $http.get(App.uri('currentUser')).success(function(data) {
          self.currentCredentials = data;
        });
      },
      isAdmin: function() {
        return this.currentCredentials && this.currentCredentials.admin;
      },
      current: function() {
        return this.currentCredentials;
      }
    };

    return new Credentials();
  })
  /**
   * RequestStatus isBusy=true -> cycle is processing an AJAX request
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