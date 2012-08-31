'use strict';


angular.module('cycle.controllers', []);

function DefaultController($scope, $http, $location, app) {
  
  // TODO: get from cookie
  $scope.currentUser = null;
  
  $http.get(app.uri('currentUser')).success(function(data) {
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

function RoundtripDetailsController($scope, $routeParams, RoundtripDetails, app, $http) {
  $scope.roundtrip = RoundtripDetails.get({id: $routeParams.roundtripId });
};

/**
 * Works along with the bpmn-diagram directive to manage a single bpmn-diagram in
 * the roundtrip view. 
 */
function BpmnDiagramController($scope, $http) {
  $scope.editDiagramDialog = new Dialog();
  
  $scope.addDiagram = function() {
    $scope.editDiagramDialog.open();
  };
 
  $scope.cancelAddDiagram = function() {
    $scope.editDiagramDialog.close();
  };
  
  $scope.saveDiagram = function(diagram) {
    $scope.roundtrip[$scope.identifier] = diagram;
    
    $scope.roundtrip.$save(function() {
      $scope.editDiagramDialog.close();
    });
  };
}

/**
 * Realizes the edit operation of a bpmn diagram inside the respective 
 * dialog. 
 */
function EditDiagramController($scope, $q, $http, debouncer, app) {
  
  var FOX_DESIGNER = "fox designer", 
    RIGHT_HAND_SIDE = "rightHandSide";

  var canEditModeler = $scope.canEditModeler = function() {
    return !!($scope.identifier != RIGHT_HAND_SIDE || $scope.editDiagram.modeler)
  };
  
  function getModelerNames() {
    $http.get(app.uri('secured/resource/diagram/modelerNames')).success(function(data) {
      $scope.modelerNames = data;
      // set default value, when only one entry
      if (data.length == 1 && canEditModeler()) {
        $scope.editDiagram.modeler = data[0];
      }
    });
  }

  function getConnectors() {
    $http.get(app.uri("secured/resource/connector/list")).success(function(data) {
      $scope.connectors = data;
    });
  }
    
  // is the dialog model valid and can be submitted?
  var isValid = $scope.isValid = function() {
    var editDiagram = $scope.editDiagram;
    console.log(!!editDiagram.modeler, ($scope.addModelForm.$valid !== false), (!!editDiagram.diagramPath && editDiagram.diagramPath.type == 'FILE'), editDiagram.diagramPath ? editDiagram.diagramPath.type : 'null');
    return !!editDiagram.modeler && ($scope.addModelForm.$valid !== false) && (editDiagram.diagramPath ? editDiagram.diagramPath.type == 'FILE' : false);
  };
  
  $scope.cancel = function() {
    $scope.cancelAddDiagram();
  };
  
  // save the dialog 
  $scope.save = function() {
    if (!isValid()) {
      return;
    }
    
    // Update diagram path
    $scope.editDiagram.diagramPath.connectorId = $scope.connector.connectorId;
    
    $scope.saveDiagram($scope.editDiagram);
  };

  $scope.modelerNames = [];
  $scope.connectors = [];
  
  // make a copy of the diagram to edit / add
  $scope.editDiagram = angular.copy($scope.diagram || {});

  // Watch for component error  
  $scope.$on("component-error", function(event, error) {
    $scope.error = error;
  });
  
  // Watch for change in diagram path
  $scope.$watch('editDiagram.diagramPath', function(newValue) {
    if (newValue) {
      console.log("editDiagram.diagramPath: " + newValue.name)
    }
  });
  
  // set modeler name as fox designer whenever a right hand side 
  // diagram with no name is edited
  // relaxed implements AT in HEMERA-2549
  if (!canEditModeler()) {
    $scope.editDiagram.modeler = FOX_DESIGNER;
  }
  
  // Error to be displayed in dialog
  $scope.error = null;
  
  // TODO: nico.rehwaldt: On update: How to initially display the right folder structure?
  // 
  // get required data
  getModelerNames();
  getConnectors();
}

function CreateNewRoundtripController($scope, $q, $http, $location, debouncer, app, Roundtrip) {

  $scope.name = '';
  $scope.nameChecked = false;
  
  $scope.errorClass = function(form) {
    return form.$valid || !form.$dirty ? '' : 'error';
  };
  
  // watch the name to check its validity on change
  $scope.$watch('name', function(newValue, oldValue) {
    $scope.nameChecked = false;
    $scope.newRoundtripForm.name.$setValidity("unused", true);
    checkName(newValue, oldValue);
  });
  
  // cancel the add operation áka close the dialog
  $scope.cancel = function() {
    $scope.newRoundtripDialog.close();
  };
  
  // is the dialog model valid and can be submitted?
  var isValid = $scope.isValid = function() {
    return $scope.newRoundtripForm.$valid && $scope.nameChecked;
  };
  
  // save the dialog 
  $scope.save = function() {
    if (!isValid()) {
      return;
    }
    
    var roundtrip = new Roundtrip({ name: $scope.name });
    
    // redirect to created roundtrip after save and close dialog
    roundtrip.$save(function() {
      $scope.newRoundtripDialog.close();
      
      $location.path("/roundtrip/" + roundtrip.id);
      $scope.$emit("roundtrip-added", roundtrip);
    });
  };
  
  var checkName = debouncer.debounce(function(name) {
    isNameValid(name).then(function(valid) {
      $scope.nameChecked = true;
      
      if ($scope.newRoundtripForm.name) {
        $scope.newRoundtripForm.name.$setValidity("unused", valid);
      }
    });
  }, 1000);
  
  function isNameValid(name) {
    var deferred = $q.defer();
    
    if (!name || name == "") {
      deferred.resolve(true);
    } else {
      $http.get(app.uri("secured/resource/roundtrip/isNameValid?name=" + name)).success(function(data) {
        deferred.resolve(data == "true");
      });
    }
    
    return deferred.promise;
  }
};

function ListRoundtripsController($scope, $route, $routeParams, Roundtrip) {
  $scope.roundtrips = Roundtrip.query();
  $scope.newRoundtripDialog = new Dialog();
  
  var selectedRoundtripId = -1; // $routeParams.roundtripId;
  
  $scope.$watch(function() { return $routeParams.roundtripId; }, function(newValue, oldValue) {
    selectedRoundtripId = parseInt(newValue);    
    if ($routeParams.roundtripId != undefined) {
      angular.forEach($scope.roundtrips, function(item) {
        if (item.id == $routeParams.roundtripId) {
          // find the roundtripname for this roundtrip-id
          $scope.$emit("navigation-changed", {name:item.name});
        }
      });
    }
  });
  
  $scope.createNew = function() {
    $scope.newRoundtripDialog.open();
  };
  
  $scope.activeClass = function(roundtrip) {
    return (roundtrip.id == selectedRoundtripId ? 'active' : '');
  };
  
  $scope.$on("roundtrip-added", function(event, roundtrip) {
    $scope.roundtrips.push(roundtrip);
  });
};
