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

function RoundtripDetailsController($scope, $routeParams, Roundtrip) {

  $scope.roundtrip = Roundtrip.get({id: $routeParams.roundtripId }, function() {
      $scope.$emit("navigation-changed", {name:$scope.roundtrip.name});
    } 
  );
  
  $scope.addBpmnModel = function(side) {
    
  };

  $scope.addModel = function() {
    $("#add-model-roundtrip-dialog").modal();
    dojoTree();
  };

  $scope.cancel = function() {
    $("#add-model-roundtrip-dialog").modal('hide');
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



// tree evaluation
function dojoTree() {
  require([
    "dojo/ready", "dojo/dom", "dojo/store/Memory", "dijit/tree/ObjectStoreModel", "dijit/Tree"
  ], function(ready, dom, Memory, ObjectStoreModel, Tree){

    // Create test store, adding the getChildren() method required by ObjectStoreModel
    var myStore = new Memory({
      data: [
        { id: 'world', name:'The earth', type:'planet', population: '6 billion'},
        { id: 'AF', name:'Africa', type:'continent', population:'900 million', area: '30,221,532 sq km',
          timezone: '-1 UTC to +4 UTC', parent: 'world'},
        { id: 'EG', name:'Egypt', type:'country', parent: 'AF' },
        { id: 'KE', name:'Kenya', type:'country', parent: 'AF' },
        { id: 'Nairobi', name:'Nairobi', type:'city', parent: 'KE' },
        { id: 'Mombasa', name:'Mombasa', type:'city', parent: 'KE' },
        { id: 'SD', name:'Sudan', type:'country', parent: 'AF' },
        { id: 'Khartoum', name:'Khartoum', type:'city', parent: 'SD' },
        { id: 'AS', name:'Asia', type:'continent', parent: 'world' },
        { id: 'CN', name:'China', type:'country', parent: 'AS' },
        { id: 'IN', name:'India', type:'country', parent: 'AS' },
        { id: 'RU', name:'Russia', type:'country', parent: 'AS' },
        { id: 'MN', name:'Mongolia', type:'country', parent: 'AS' },
        { id: 'OC', name:'Oceania', type:'continent', population:'21 million', parent: 'world'},
        { id: 'EU', name:'Europe', type:'continent', parent: 'world' },
        { id: 'DE', name:'Germany', type:'country', parent: 'EU' },
        { id: 'FR', name:'France', type:'country', parent: 'EU' },
        { id: 'ES', name:'Spain', type:'country', parent: 'EU' },
        { id: 'IT', name:'Italy', type:'country', parent: 'EU' },
        { id: 'NA', name:'North America', type:'continent', parent: 'world' },
        { id: 'SA', name:'South America', type:'continent', parent: 'world' }
      ],
      getChildren: function(object){
        return this.query({parent: object.id});
      }
    });

    // Create the model
    var myModel = new ObjectStoreModel({
      store: myStore,
      query: {id: 'world'}
    });

    // Create the Tree.   Note that all widget creation should be inside a dojo.ready().
    ready(function(){
      var tree = new Tree({
        model: myModel
      });
      tree.placeAt(dom.byId("dojoTree"));
      tree.startup();
    });
  });
};