'use strict';


angular.module('cycle.controllers', []);

function DefaultController($scope, $http, $location) {
  
  // TODO: get from cookie
  $scope.currentUser = null;
  
  $http.get('../../../currentUser').success(function(data) {
    $scope.currentUser = data;
  });
  
  $scope.$on("cycle.userChanged", function(event, user) {
    $scope.currentUser = user;
  });
  
  
  // Bread Crumb 
  var breadCrumbs = $scope.breadCrumbs = [];
  	
  $scope.$on("navigation-changed", function(evt, navigationItem) {
	  if(navigationItem==undefined) {
	    breadCrumbs.splice(0, breadCrumbs.length);
	  } else {
		var contains = false;
		var remove = 0;
		angular.forEach(breadCrumbs, function(item) {
			if(item.name == navigationItem.name) {
				contains = true;			
			}
			if(item.href.indexOf($location.path()) != 0) {
				remove++;
			}
		});
		
		for (var i = 0; i < remove; i++) {
		  breadCrumbs.pop();						
		}		
		
		if(!contains) {
			breadCrumbs.push({name:navigationItem.name, href:$location.path()});
		}		
	  }
  });
  // end Bread Crumb
};

function HomeController($scope, $routeParams) {
  $scope.$emit("navigation-changed");
}

function RoundtripDetailsController($scope, $routeParams, RoundtripDetails) {

  $scope.roundtrip = RoundtripDetails.get({id: $routeParams.roundtripId }, function() {
      $scope.$emit("navigation-changed", {name:$scope.roundtrip.name});
    } 
  );
  
  $scope.addBpmnModel = function(side) {
    
  };
};

function CreateNewRoundtripController($scope, $q, $http, $location, debouncer, Roundtrip) {

  $scope.name = '';
  
  $scope.errorClass = function(form) {
    return form.$valid || !form.$dirty ? '' : 'error';
  };
  
  $scope.$watch('name', function(newValue, oldValue) {
    checkName(newValue, oldValue);
  });
  
  $scope.cancel = function() {
    $("#create-roundtrip-dialog").modal('hide'); 
  };
  
  $scope.save = function() {
    if (!$scope.newRoundtripForm.$valid) {
      return;
    }
    
    var roundtrip = new Roundtrip({ name: $scope.name });
    roundtrip.$save(function() {

      $location.path("/roundtrip/" + roundtrip.id);
      $scope.$emit("roundtrip-added", roundtrip);
      $scope.name = '';
    });

    $("#create-roundtrip-dialog").modal('hide');
  };
  
  var checkName = debouncer.debounce(function(name) {
    isNameValid(name).then(function() {
      $scope.newRoundtripForm.name.$setValidity("occupied", true);
    }, function() {
      $scope.newRoundtripForm.name.$setValidity("occupied", false);
    });
  }, 1000);
  
  function isNameValid(name) {
    var deferred = $q.defer();
    
    if (!name || name == "") {
      deferred.resolve();
    }
    
    $http.get("../../resources/roundtrip/isNameValid?name=" + name).success(function(data) {
      if (data == "true") {
        deferred.resolve();
      } else {
        deferred.reject();
      }
    });
    
    return deferred.promise;
  }
};

function ListRoundtripsController($scope, $route, $routeParams, Roundtrip) {
  $scope.roundtrips = Roundtrip.query();
  
  var selectedRoundtripId = -1; // $routeParams.roundtripId;
  
  $scope.$watch(function() { return $routeParams.roundtripId; }, function(newValue, oldValue) {
    selectedRoundtripId = parseInt(newValue);
  });
  
  $scope.createNew = function() {
    $("#create-roundtrip-dialog").modal(); 
  };
  
  $scope.activeClass = function(roundtrip) {
    return (roundtrip.id == selectedRoundtripId ? 'active' : '');
  };
  
  $scope.$on("roundtrip-added", function(event, roundtrip) {
    $scope.roundtrips.push(roundtrip);
  });
};