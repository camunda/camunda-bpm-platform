'use strict';


angular.module('cycle.controllers', []);

window.credentials = null;

function DefaultController($scope, $http, $location, App, Event, Error, Credentials, $element) {
  $scope.appErrors = function () {
    return Error.errors;
  };
  
  $scope.removeError = function (error) {
    Error.removeError(error);
  };
  
  Credentials.reload();
    
  // TODO: get from cookie
  $scope.currentUser = null;
    
  $scope.$watch(Credentials.watchCurrent, function(newValue) {
    $scope.currentUser = newValue;
  });
    
  $scope.$on(Event.userChanged, function(event, user) {
    $scope.currentUser = user;
  }); 
    
  // needed for form validation
  // DO NOT REMOVE FROM DEFAULT CONTROLLER!
  $scope.errorClass = function(form) {
    return form.$valid || !form.$dirty ? '' : 'error';
  };

  // Bread Crumb 
  var breadCrumbs = $scope.breadCrumbs = [];

  $scope.$on(Event.navigationChanged, function(event, navigationItem) {
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

function HomeController($scope, Event) {
  $scope.$emit(Event.navigationChanged);
}

function RoundtripDetailsController($scope, $routeParams, RoundtripDetails, Commons, Event) {
  $scope.currentPicture = 'leftHandSide';
  
  $scope.diagramDetailsDialog = new Dialog();
  $scope.diagramDetailsDialog.setCenter(true);
  
  $scope.syncDialog = new Dialog();
  $scope.syncDialog.setAutoClosable(false);

  //get roundtrip details and forward to main page if bad request (ie. invalid id) occurs
  $scope.roundtrip = RoundtripDetails.get({id: $routeParams.roundtripId }, function() {}, function(response) {
    if (response.status == 400) {
      $location.path("/");
    }
  });

  $scope.canSync = function() {
    
    var roundtrip = $scope.roundtrip,
        lhsModeSyncStatus = null,
        rhsModeSyncStatus = null;
    
    if (!roundtrip) {
      return false;
    }
    
    if (roundtrip.rightHandSide && roundtrip.leftHandSide) {
      roundtrip.leftHandSide.syncStatus ? lhsModeSyncStatus = roundtrip.leftHandSide.syncStatus.status : lhsModeSyncStatus = "UNAVAILABLE";
      roundtrip.rightHandSide.syncStatus ? rhsModeSyncStatus = roundtrip.rightHandSide.syncStatus.status : rhsModeSyncStatus = "UNAVAILABLE";
      return lhsModeSyncStatus != "UNAVAILABLE" && rhsModeSyncStatus != "UNAVAILABLE"; 
    }
    
    return false;
  };
  
  /**
   * Return true if the managed roundtrip can be created (LTR or RTL)
   */
  $scope.canCreate = function(mode) {
    var sourceModeSyncStatus = null,
        targetModeSyncStatus = null,
        roundtrip = $scope.roundtrip;
    
    if (!roundtrip) {
      return false;
    }
    
    if (mode == "LEFT_TO_RIGHT") {
      if (roundtrip.leftHandSide && !roundtrip.rightHandSide) {
        // added this additional check to avoid an error: roundrip.leftHandSide.syncStatus is undefined
        if (roundtrip.leftHandSide.syncStatus) { 
          sourceModeSyncStatus = roundtrip.leftHandSide.syncStatus.status || "UNAVAILABLE";
          return sourceModeSyncStatus != "UNAVAILABLE";
        }
      } else if (roundtrip.leftHandSide && roundtrip.rightHandSide) {
        if (roundtrip.rightHandSide.syncStatus && roundtrip.leftHandSide.syncStatus) {
          sourceModeSyncStatus = roundtrip.rightHandSide.syncStatus.status || "UNAVAILABLE";
          targetModeSyncStatus = roundtrip.leftHandSide.syncStatus.status || "UNAVAILABLE";
          return sourceModeSyncStatus == "UNAVAILABLE" && targetModeSyncStatus != "UNAVAILABLE";
        }        
      }
    } else if (mode == "RIGHT_TO_LEFT") {
      if (roundtrip.rightHandSide && !roundtrip.leftHandSide) {
        // added this additional check to avoid an error: roundrip.rightHandSide.syncStatus is undefined
        if (roundtrip.rightHandSide.syncStatus) {
          sourceModeSyncStatus = roundtrip.rightHandSide.syncStatus.status || "UNAVAILABLE";
          return sourceModeSyncStatus != "UNAVAILABLE";
        }
      } else if (roundtrip.leftHandSide && roundtrip.rightHandSide) {
        if (roundtrip.leftHandSide.syncStatus && roundtrip.rightHandSide.syncStatus) { 
          sourceModeSyncStatus = roundtrip.leftHandSide.syncStatus.status || "UNAVAILABLE";
          targetModeSyncStatus = roundtrip.rightHandSide.syncStatus.status || "UNAVAILABLE";
          return sourceModeSyncStatus == "UNAVAILABLE" && targetModeSyncStatus != "UNAVAILABLE";
        }
      }
    }
    
    return false;
  };
  
  $scope.activeClass = function(side) {
    return side == $scope.currentPicture ? "active" : "";
  };
  
  function fullScreenShowDiagram(side) {
    $scope.setCurrentPicture(side);
    $scope.diagramDetailsDialog.open();
  }

  $scope.$on(Event.modelImageClicked, function(event, side) {
    fullScreenShowDiagram(side);
  });

  $scope.openSyncDialog = function (syncMode) {
    $scope.syncMode = syncMode;
    $scope.syncDialog.open();
  };
  
  $scope.createNewDiagram = function(diagram) {
    var roundtrip = $scope.roundtrip;
    
    diagram.diagramTemplate = null;
    
    switch (diagram.identifier) {
    case "leftHandSide":
      diagram.diagramTemplate = { label: roundtrip.rightHandSide.label };
      break;
    case "rightHandSide":
      diagram.diagramTemplate = { label: roundtrip.leftHandSide.label };
    }
    
    diagram.editDialogMode = "CREATE_NEW_DIAGRAM";
    diagram.editDiagramDialog.open();
  };

  $scope.delayedSetCurrentPicture = function (picture) {
    setTimeout(function() {
      console.log(picture);
      $scope.setCurrentPicture(picture);
      
      // Same bug as with the tree; DO NOT DELETE the following two lines!
      $scope.$digest();
      $scope.$apply();
    }, 800);
  };
  
  $scope.setCurrentPicture = function (picture) {
    $scope.currentPicture = picture;
  };
}

function SyncRoundtripController($scope, $http, $q, App, Event, Connector) {
  
  var SYNC_SUCCESS = "synchronizationSuccess",
      SYNC_FAILED = "synchronizationFailed",
      PERFORM_SYNC = "performSynchronize",
      BEFORE_SYNC = "beforeStart";
  
  $scope.status = BEFORE_SYNC;
  
  $scope.commitMessage = "Model updated using camunda cycle.";
  
  $scope.showCommitMessageForm = function() {
    return ($scope.status == BEFORE_SYNC || $scope.status == PERFORM_SYNC) && $scope.targetConnectorSupportsCommitMessage();
  };
  
  $scope.targetConnectorSupportsCommitMessage = function() {
	if($scope.syncMode == "LEFT_TO_RIGHT") {
		return Connector.supportsCommitMessages($scope.roundtrip.rightHandSide.connectorNode);
	} else if($scope.syncMode == "RIGHT_TO_LEFT") {
		return Connector.supportsCommitMessages($scope.roundtrip.leftHandSide.connectorNode);
	}    
  };
  
  $scope.cancel = function () {
    $scope.syncDialog.close();
  };
  
  $scope.syncNoteCls = function(mode) {
    return mode == 'LEFT_TO_RIGHT' ? 'ltr' : 'rtl';
  };
  
  $scope.performSync = function() {
    $scope.status = PERFORM_SYNC;
    
    var Delay = function(delayMs) {
      var deferred = $q.defer();
      
      setTimeout(function() {
        deferred.resolve();
        $scope.$apply();
      }, delayMs);
      
      return deferred.promise;
    };
    
    var delayed = new Delay(2000);
    
    $http.post(App.uri('secured/resource/roundtrip/' + $scope.roundtrip.id + '/sync?syncMode=' + $scope.syncMode+"&message="+encodeURIComponent($scope.commitMessage))).
      success(function(data) {
        delayed.then(function() {
          $scope.roundtrip.$get({id: $scope.roundtrip.id });
          var status = data.status;
          if (status == "SYNC_SUCCESS") {
            $scope.status = SYNC_SUCCESS;
          } else if (status == "SYNC_FAILED") {
            $scope.status = SYNC_FAILED;
            $scope.message = data.message;
          }
        });
      });
  };
}

/**
 * Works along with the bpmn-diagram directive to manage a single bpmn-diagram in the roundtrip view.
 */
function BpmnDiagramController($scope, Commons, Event, $http, App) {

  var SYNC_SUCCESS = "synchronizationSuccess",
      SYNC_FAILED = "synchronizationFailed";
  
  $scope.imageStatus = "UNKNOWN";
  $scope.modelStatus = "UNKNOWN";

  function changeModelStatus(status) {
    $scope.modelStatus = status;
  }

  $scope.editDiagramDialog = new Dialog();
  $scope.editDiagramDialog.setAutoClosable(false);

  $scope.addDiagram = function() {
    $scope.editDialogMode = "ADD_DIAGRAM";
    $scope.editDiagramDialog.open();
  };

  $scope.cancelAddDiagram = function() {
    $scope.editDiagramDialog.close();
  };

  $scope.saveDiagram = function(diagram) {
    $scope.roundtrip[$scope.identifier] = diagram;

    $scope.roundtrip.$save(function() {
      $scope.diagram = $scope.roundtrip[$scope.identifier];
      $scope.editDiagramDialog.close();
    });
  };
  
  $scope.createDiagram = function(diagram, commitMessage) {
    switch ($scope.handle) {
    case "rightDiagram":
      $scope.syncMode = "LEFT_TO_RIGHT";
      break;
    case "leftDiagram":
      $scope.syncMode = "RIGHT_TO_LEFT";
    }
    
    $http.post(App.uri('secured/resource/roundtrip/' + $scope.roundtrip.id + '/create/?diagramlabel=' + diagram.label + '&syncMode=' + $scope.syncMode + '&modeler=' + diagram.modeler + '&connectorId=' + diagram.connectorNode.connectorId + '&parentFolderId=' + diagram.connectorNode.id +'&message='+encodeURIComponent(commitMessage)))
    .success(function(data) {
        $scope.roundtrip.$get({id: $scope.roundtrip.id });
        $scope.status = SYNC_SUCCESS;
        $scope.editDiagramDialog.close();
    })
    .error(function (data) {
        $scope.status = SYNC_FAILED;
    });
  
  };

  $scope.diagramClass = function(diagram) {
    return $scope.modelStatus == "UNAVAILABLE" ? "error" : "";
  };

  $scope.showImage = function(side) {
    $scope.$emit(Event.modelImageClicked, side);
  };

  $scope.$watch("diagram", function(newDiagramValue) {
    // Check availability only when diagram is saved
    if (newDiagramValue && newDiagramValue.id) {
      $scope.checkContentAvailable(newDiagramValue);
    }
  });

  $scope.checkContentAvailable = function(diagram) {
    Commons.getDiagramStatus(diagram)
      .success(function(data) {
        $scope.diagram.syncStatus = data;
        changeModelStatus(data.status);
      });
  };
    
}

/**
 * Realizes the edit operation of a bpmn diagram inside the respective dialog.
 */
function EditDiagramController($scope, Commons, Event, ConnectorConfiguration, Connector, Error) {
  
  var CAMUNDA_MODELER = "camunda modeler", 
      RIGHT_HAND_SIDE = "rightHandSide";
  
  $scope.commitMessage = "Model created using camunda cycle.";
  
  $scope.modelerNames = [];
  $scope.connectors = ConnectorConfiguration.query();

  // make a copy of the diagram to edit / add
  $scope.editDiagram = angular.extend(angular.copy($scope.diagram || {}), $scope.diagramTemplate || {});

  // Can the modeler name be edited?
  var canEditModeler = $scope.canEditModeler = function() {
    return !!($scope.identifier != RIGHT_HAND_SIDE || ($scope.editDiagram.modeler && $scope.editDiagram.modeler != CAMUNDA_MODELER));
  };
  
  $scope.showCommitMessageInput = function() {	  
    if (!$scope.connector) {
      return false;
    }
    return $scope.editDialogMode == "CREATE_NEW_DIAGRAM" && Connector.supportsCommitMessages($scope.connector);
  };

  // is the dialog model valid and can be submitted?
  var isValid = $scope.isValid = function() {
    var editDiagram = $scope.editDiagram;
    var valid = !!editDiagram.modeler && $scope.addModelForm.$valid && $scope.selectedNode && $scope.selectedNode.type == "BPMN_FILE";
    return valid;
  };
  
  var isValidAndFolder = $scope.isValidAndFolder = function() {
    var editDiagram = $scope.editDiagram;
    var validAndfolder = !!editDiagram.modeler && $scope.addModelForm.$valid && $scope.selectedNode && $scope.selectedNode.type == "FOLDER";
    return validAndfolder;
  };
  
  $scope.acceptedChildTypes = function() {
    if ($scope.editDialogMode == "ADD_DIAGRAM") {
      return [ "PNG_FILE", "BPMN_FILE", "FOLDER" ];
    }
    if ($scope.editDialogMode == "CREATE_NEW_DIAGRAM") {
      return [ "FOLDER" ];
    }
    return [ "ANY_FILE", "FOLDER" ];
  };
  
  $scope.cancel = function() {
    $scope.cancelAddDiagram();
  };
  
  // save the dialog 
  $scope.save = function() {
    if (!isValid()) {
      return;
    }
    $scope.saveDiagram($scope.editDiagram);
  };
  
  $scope.create = function() {
      $scope.createDiagram($scope.editDiagram, $scope.commitMessage);
  };

  $scope.$on(Event.selectedConnectorChanged, function(event) {
    if (Error.errors.length > 0) {
      Error.removeAllErrors();
    }
    $scope.selectedNode = null;
  });

  // Watch for change in diagram path
  $scope.$watch('selectedNode', function(newValue) {
    if (newValue) {
      $scope.editDiagram.connectorNode = newValue;
    }
  });
  
  // set modeler name as camunda modeler whenever a right hand side 
  // diagram with no name is edited
  // relaxed implements AT in HEMERA-2549
  if (!canEditModeler()) {
    $scope.editDiagram.modeler = CAMUNDA_MODELER;
  }
  
  // TODO: nre: On update: How to initially display the right folder structure?
  // 
  // get required data
  Commons.getModelerNames().then(function(data) {
    // filter out CAMUNDA_MODELER
    for (var i = data.length-1; i >= 0; i--) {
      if (angular.equals(data[i], CAMUNDA_MODELER)) {
        data.splice(i, 1);
        break;
      }
    }

    $scope.modelerNames = data;

    // set default value
    if (data.length > 0 && canEditModeler()) {
      $scope.editDiagram.modeler = data[0];
    }
  });
}

/**
 * Responsible for adding a new roundtrip from within the roundtrip list
 */
function CreateRoundtripController($scope, $q, $http, $location, App, Roundtrip, Event) {

  $scope.newRoundtrip = { };

  // cancel the add operation áka close the dialog
  $scope.cancel = function() {
    $scope.newRoundtripDialog.close();
  };

  // is the dialog model valid and can be submitted?
  var isValid = $scope.isValid = function() {
    return $scope.newRoundtripForm.$valid;
  };

  // save the dialog 
  $scope.save = function() {
    if (!isValid()) {
      return;
    }

    var roundtrip = new Roundtrip($scope.newRoundtrip);

    // redirect to created roundtrip after save and close dialog
    roundtrip.$save(function() {
      $scope.newRoundtripDialog.close();
      
      $location.path("/roundtrip/" + roundtrip.id);
      $scope.$emit(Event.roundtripAdded, roundtrip);
    });
  };

  /**
   * Checks the validity of a name in the backend.
   * Returns a promise which is fulfilled when the check was done. 
   * 
   * Usage: 
   * isNameAvailable("Walter").then(function(nameOk) {
   *   console.log("Name 'Walter' is ok? ", nameOk);
   * });
   * 
   * @param name to be checked
   * 
   * @returns promise to be fulfilled when the check was done
   */
  $scope.isNameAvailable = function(name) {
    var deferred = $q.defer();
    
    if (!name || name == "") {
      deferred.resolve(true);
    } else {
      $http.get(App.uri("secured/resource/roundtrip/isNameAvailable?name=" + name)).success(function(data) {
        deferred.resolve(data == "true");
      });
    }
    
    return deferred.promise;
  };
};

/**
 * Responsible for listing the roundtrips and updating the currently selected one
 * 
 */
function ListRoundtripsController($scope, $routeParams, $http, $location, Roundtrip, Event, App) {
  
  // TODO: Add documentation page
  $scope.roundtrips = Roundtrip.query();
  $scope.newRoundtripDialog = new Dialog();
  
  $scope.deleteRoundtripDialog = new Dialog();
  $scope.deleteRoundtripDialog.setAutoClosable(false);
  
  $scope.selectedRoundtripId = null;
  
  // Update the selected roundtrip on route change
  $scope.$watch(function() { return $routeParams.roundtripId; }, function(newValue, oldValue) {
    if (newValue) {
      $scope.selectedRoundtripId = parseInt(newValue);
      if (isNaN($scope.selectedRoundtripId)) {
        $scope.selectedRoundtripId = null;
      } else {
        angular.forEach($scope.roundtrips, function(item) {
          if (item.id == $routeParams.roundtripId) {
            // find the roundtripname for this roundtrip-id
            $scope.$emit(Event.navigationChanged, {name:item.name});
          }
        });
      }
    } else {
      $scope.selectedRoundtripId = null;
    }
  });
  
  $scope.createNew = function() {
    $scope.newRoundtripDialog.open();
  };
  
  $scope.deleteRoundtrip = function() {
    if (!$scope.selectedRoundtripId) {
      return;
    }
    
    $scope.deleteRoundtripDialog.open();
  };
  
  $scope.activeClass = function(roundtrip) {
    return (roundtrip.id == $scope.selectedRoundtripId ? 'active' : '');
  };
  
  $scope.$on(Event.roundtripAdded, function(event, roundtrip) {
    $scope.roundtrips.push(roundtrip);
  });
  
};

/**
 * Responsible to delete roundtrips
 * 
 */
function DeleteRoundtripController($scope, $routeParams, $http, $location, App) {

  var PERFORM_DEL = "performRoundtripDeletion",
      DEL_SUCCESS = "deletionSuccess",
      DEL_FAILED = "deletionFailed";
  
  $scope.toBeDeleted = PERFORM_DEL;
  
  function findRoundtripById(roundtrips, roundtripId) {
    var roundtrip = null;
    
    angular.forEach(roundtrips, function(e, i) {
      if (e.id == roundtripId) {
        roundtrip = e;
      }
    });

    return roundtrip;
  }
  
  $scope.performDeletion = function() {
    if (!$routeParams.roundtripId) {
      return;
    }

    var roundtrip = findRoundtripById($scope.roundtrips, $routeParams.roundtripId);

    roundtrip.$delete(function() {
      $scope.toBeDeleted = DEL_SUCCESS;
      $scope.roundtrips.splice($scope.roundtrips.indexOf(roundtrip), 1);
      $location.path("/");
    }, function(data) {
      $scope.toBeDeleted = DEL_FAILED;
    });
  };
};

// connector-setup.html ////////////////////////////////

function ConnectorSetupController($scope, $http, $location, App, Event, Commons, ConnectorConfiguration) {
  $scope.editConnectorConfigurationDialog = new Dialog();
  $scope.deleteConnectorConfigurationDialog = new Dialog();
  
  $scope.$emit(Event.navigationChanged, {name:"Connectors"});

  $scope.connectorConfigurations = ConnectorConfiguration.query();

  $scope.createNew = function() {
    $scope.currentConnectorConfiguration = null;
    $scope.connectorDialogMode = "ADD_CONNECTOR";
    $scope.editConnectorConfigurationDialog.open();
  };
  
  $scope.editConnector = function(connectorConfiguration) {
    $scope.connectorDialogMode = "EDIT_CONNECTOR";
     // make a copy of the connector to edit
    $scope.currentConnectorConfiguration = connectorConfiguration;
    $scope.editConnectorConfigurationDialog.open();
  };
  
  $scope.deleteConnector = function(connectorConfiguration) {
    $scope.currentConnectorConfiguration = connectorConfiguration;
    $scope.deleteConnectorConfigurationDialog.open();
  };
  
  $scope.saveConnectorConfiguration = function(editConnectorConfiguration) {
    var connectorConfiguration = new ConnectorConfiguration(editConnectorConfiguration);
    
    connectorConfiguration.$save(function() {
      $scope.connectorConfigurations.push(connectorConfiguration);
      $scope.editConnectorConfigurationDialog.close();
    });
  };
  
  $scope.updateConnectorConfiguration = function(editConnectorConfiguration) {
    var currentConnectorConfig = $scope.currentConnectorConfiguration;
    
    angular.extend(currentConnectorConfig, editConnectorConfiguration);
    currentConnectorConfig.$save(function() {
      $scope.editConnectorConfigurationDialog.close();
    });
  };
  
};

function EditConnectorController($scope, $http, App, ConnectorConfiguration) {
  
  var BASE_PATH = "BASE_PATH",
      SIGNAVIO_BASE_URL = "signavioBaseUrl",
      REPO_PATH = "repositoryPath",
      FOLDER_ROOT_PATH = "folderRootPath",
      ALLOW_ALL_SSL_HOSTNAMES = "allowAllSSLHostnames",
      TEMPORARY_FILE_STORE = "temporaryFileStore",
      PROXY_URL = "proxyUrl",
      PROXY_USERNAME = "proxyUsername",
      PROXY_PASSWORD = "proxyPassword";
  
  var REQUIRED_PROPERTIES = [BASE_PATH, SIGNAVIO_BASE_URL, REPO_PATH];

  $scope.selectedConnectorConfiguration = null;

  $scope.defaultConfigurations = ConnectorConfiguration.queryDefaults();

  // cached custom properties keys
  $scope.customPropertyNames = [];
  
  $scope.editConnectorConfiguration = angular.extend(angular.copy($scope.currentConnectorConfiguration || {}));

  $scope.connectorTest = null;
  
  $scope.passwordRequired = !(($scope.connectorDialogMode == "EDIT_CONNECTOR") && ($scope.editConnectorConfiguration.loginMode == "GLOBAL"));
  
  $scope.$watch("editConnectorConfiguration", function(editConnectorConfiguration) {
    if (!editConnectorConfiguration) {
      return;
    }
    
    angular.forEach($scope.connectorConfigurations, function(e, i) {
      if (e.connectorId == editConnectorConfiguration.connectorId) {
        $scope.connectorConfigurationBlueprint = e;
      }
    });
  });
  
  // Fix for HEMERA-2965: We need to cache the keys for the configuration, 
  // otherwise the custom fields loose focus on type
  $scope.$watch("editConnectorConfiguration.properties", function(newValue) {
    if (newValue) {
      var keys = [];
      angular.forEach(newValue, function(val, key) {
        keys.push(key);
      });
      
      // sort the array and than reverse the sorted order
      keys.sort().reverse();
      $scope.customPropertyNames = keys;
    }
  });
  
  function copySettings(blueprint) {
    var config = $scope.editConnectorConfiguration;

    // make sure properties is empty
    config.properties = {};

    // remember name
    config.connectorName = blueprint.name;

    // remember class
    config.connectorClass = blueprint.connectorClass;

    // extend properties
    angular.extend(config.properties, blueprint.properties);
  }
  
  $scope.$watch("blueprint", function(newBlueprint) {
    if (newBlueprint) {
      copySettings(newBlueprint);
    }
  });
  
  $scope.save = function() {
    if (!isValid()) {
      return;
    }
    
    // TODO: rename connectorId to id
    if ($scope.editConnectorConfiguration.connectorId) {
      $scope.updateConnectorConfiguration($scope.editConnectorConfiguration);
    } else {
      $scope.saveConnectorConfiguration($scope.editConnectorConfiguration);
    }
  };

  // is the dialog model valid and can be submitted?
  var isValid = $scope.isValid = function() {
    var editConnectorConfig = $scope.editConnectorConfiguration;
    var valid = !!editConnectorConfig.connectorClass && $scope.editConnectorConfigForm.$valid;
    return valid;
  };

  $scope.test = function() {
    $http.post(App.uri("secured/resource/connector/configuration/test"), $scope.editConnectorConfiguration).success(function(data) {
      $scope.connectorTest = data;
    });
  };

  $scope.isPropertyPassword = function(propertyKey) {
    if (/.*password.*/i.test(propertyKey)) {
      return true;
    }
    return false;
  };
  
  $scope.isPropertyRequired = function(propertyKey) {	
    return REQUIRED_PROPERTIES.indexOf(propertyKey) > -1;
  };
  
  $scope.propertyNameText = function(propertyKey) {
	return propertyKey + ($scope.isPropertyRequired(propertyKey)?"*":"");
  };

  $scope.currentHelpText = function(propertyKey) {
   if (propertyKey == BASE_PATH) {
     return "provide path to your file system, e.g. 'C:\\Users\\Tommy\\FileSystem'";
   } else if (propertyKey == SIGNAVIO_BASE_URL) {
     return "enter the URL to your modelers loginpage, e.g. 'https://editor.signavio.com/'";
   } else if (propertyKey == REPO_PATH) {
     return "The SVN root URL to use, e.g. 'https://svn.camunda.com/fox/'";
   } else if (propertyKey == FOLDER_ROOT_PATH) {
     return "if you want to point the camunda modeler to some root directory different to the normal Signavio root directory, enter the ID here, e.g. eb36d6fd27794eda95a9f7e9aa16d987";
   } else if (propertyKey == ALLOW_ALL_SSL_HOSTNAMES) {
     return "enter 'true' to allow changes in SSL-Hostnames of modeler, otherwise enter 'false'";
   } else if (propertyKey == TEMPORARY_FILE_STORE) {
     return "A directory in the local file system which can be used to store temporary files, e.g. 'c:/temp/svn'";
   } else if (propertyKey == PROXY_URL) {
     return "enter the proxy url if you use one, e.g. 'https://192.168.0.1:3582'";
   } else if (propertyKey == PROXY_USERNAME) {
     return "enter proxy username if your proxy requires one, otherwise ignore it.";
   } else if (propertyKey == PROXY_PASSWORD) {
     return "enter proxy password if your proxy requires one, otherwise ignore it.";
   }
  };
  
  $scope.editPasswordText = "Input a new password to override the existing password. If left blank, the password is unchanged.";
  $scope.editPasswordTitle = "Password";
}

function DeleteConnectorConfigurationController($scope, $location, $http, App) {

  var PERFORM_DEL = "TO_BE_DONE",
      DEL_SUCCESS = "SUCCESS",
      DEL_FAILED = "FAILURE";

  $scope.state = PERFORM_DEL;

  $scope.configuration = $scope.currentConnectorConfiguration;
  
  $scope.performConnectorDeletion = function() {
    var configuration = $scope.configuration;
    
    configuration.$delete(function() {
      $scope.state = DEL_SUCCESS;
      $scope.connectorConfigurations.splice($scope.connectorConfigurations.indexOf($scope.toBeDeletedConnector), 1);
    }, function(error) {
      $scope.state = DEL_FAILED;
    });
  };
}

// users.html ////////////////////////////////

function UsersController($scope, Event, User) {

  $scope.editUserDialog = new Dialog();
  $scope.deleteUserDialog = new Dialog();
  
  $scope.$emit(Event.navigationChanged, { name: "Users" });

  $scope.users = User.query();

  $scope.editUser = function(user) {
    $scope.mode = "EDIT";
    $scope.selectedUser = user;
    
    $scope.editUserDialog.open();
  };

  $scope.deleteUser = function(user) {
    $scope.selectedUser = user;
    $scope.deleteUserDialog.open();
  };
  
  $scope.isCurrentUser = function(user) {
    return user.name == ($scope.currentUser || {}).name;
  };

  $scope.createNew = function() {
    $scope.mode = "ADD";
    $scope.selectedUser = null;
    
    $scope.editUserDialog.open();
  };
  
  $scope.saveUser = function(userData, callbackFn) {
    var isNew = !userData.id;
    
    var user = $scope.selectedUser || new User({});
    
    // copy user data
    angular.extend(user, userData);
    
    user.$save(function () {
      callbackFn.apply(arguments);
      
      if (isNew) {
        $scope.users = User.query();
      }
    });
  };
};

function EditUserController($scope, $q, $http, App) {
  
  $scope.editUser = angular.copy($scope.selectedUser || {});
  $scope.oldName = $scope.editUser.name;
  
  $scope.save = function() {
    if (!isValid()) {
      return;
    }

    $scope.saveUser($scope.editUser, function() {
      $scope.editUserDialog.close();
    });
  };

  $scope.isNameAvailable = function(name) {
    var deferred = $q.defer();

    if (!name || name == $scope.oldName) {
      deferred.resolve(true);
      
      // make sure result of check is resolved
      // in the current execution thread
      var thenFn = deferred.promise.then;
      deferred.promise.then = function(arg) {
        $scope.$apply(function() {
          thenFn(arg);
        });
      };
    } else {
      $http.get(App.uri("secured/resource/user/isNameAvailable?name=" + name)).success(function(data) {
        deferred.resolve(data == "true");
      });
    }

    return deferred.promise;
  };

  // is the dialog model valid and can be submitted?
  var isValid = $scope.isValid = function() {
    return $scope.editUserForm.$valid;
  };
}

function DeleteUserController($scope) {

  var PERFORM_DEL = "TO_BE_DONE",
      DEL_SUCCESS = "SUCCESS",
      DEL_FAILED = "FAILURE";

  $scope.state = PERFORM_DEL;

  $scope.deleteUser = $scope.selectedUser;

  $scope.confirmDelete = function() {
    var user = $scope.deleteUser;
    
    user.$delete(function() {
      $scope.state = DEL_SUCCESS;
      $scope.users.splice($scope.users.indexOf(user), 1);
    }, function(error) {
      $scope.state = DEL_FAILED;
    });
  };
}

// create-initial-user.html ///////////////////////////////////////

function CreateInitialUserController($scope, $window, $http, App) {

  $scope.editUser = {};

  $scope.save = function() {
    if (!isValid()) {
      return;
    }

    $scope.saveUser($scope.editUser, function() {
      $window.location.href = App.uri("secured/view/index");
    });
  };

  $scope.saveUser = function(userData, callbackFn) {
    $http.post(App.uri("first-time-setup"), userData).success(callbackFn);
  };
  
  // is the dialog model valid and can be submitted?
  var isValid = $scope.isValid = function() {
    return $scope.editUserForm.$valid;
  };
}

// profile.html ////////////////////////////////

function ProfileController($scope, $http, App, Event, Credentials, ConnectorConfiguration, ConnectorCredentials) {
  
  $scope.connectorCredentialsDialog = new Dialog();
  $scope.changePasswordDialog = new Dialog();

  $scope.$emit(Event.navigationChanged, {name:"Profile"});

  $scope.connectorConfigurations = ConnectorConfiguration.query();
  
  $scope.$watch(Credentials.watchCurrent, function(newValue) {
    $scope.currentUser = newValue;
    if (newValue) {
      fetchConnectorCredentials();
    }
  });
  
  function fetchConnectorCredentials() {
    ConnectorCredentials.query( {userId: $scope.currentUser.id }, function (data) {
        var credentials = {};
        angular.forEach(data, function (item) {
          credentials[item.connectorId] = item;
        });
        $scope.connectorCredentialsByConnectorId = credentials;
      });
  }

  $scope.editConnectorCredentials = function (connectorConfiguration) {
    $scope.mode = "EDIT";
    $scope.connectorConfiguration = connectorConfiguration;
    $scope.selectedConnectorCredentials = $scope.connectorCredentialsByConnectorId[connectorConfiguration.connectorId];
    $scope.connectorCredentialsDialog.open();
  };

  $scope.addConnectorCredentials = function (connectorConfiguration) {
    $scope.mode = "ADD";
    $scope.connectorConfiguration = connectorConfiguration;
    $scope.selectedConnectorCredentials = null;
    $scope.connectorCredentialsDialog.open();
  };

  $scope.changePassword = function() {
    $scope.changePasswordDialog.open();
  };
  
  $scope.showEditAction = function (connectorConfiguration) {
    if (!$scope.connectorCredentialsByConnectorId) {
      return false;
    }
    var connectorId = connectorConfiguration.connectorId;
    return !!$scope.connectorCredentialsByConnectorId[connectorId];
  };
  
  $scope.saveConnectorCredentials = function(connectorCredentialsData, callbackFn) {
    var isNew = !connectorCredentialsData.id;
    
    var connectorCredentials = $scope.selectedConnectorCredentials || new ConnectorCredentials({});
    
    // copy connector credentials data
    angular.extend(connectorCredentials, connectorCredentialsData);
    
    connectorCredentials.$save(callbackFn);
    if (isNew) {
      $scope.connectorCredentialsByConnectorId[connectorCredentials.connectorId] = connectorCredentials;
    }
  };
  
}

function EditConnectorCredentials($scope, $http, App) {
  $scope.editCredentials = $scope.selectedConnectorCredentials || {};
  
  $scope.test = function () {
    $scope.editCredentials.connectorId = $scope.connectorConfiguration.connectorId;
    $scope.editCredentials.userId = $scope.currentUser.id;
    $http.post(App.uri("secured/resource/connector/credentials/test"), $scope.editCredentials)
      .success(function(data) {
        $scope.credentialsTest = data;
    });
  };
  
  var isValid = $scope.isValid = function () {
    return $scope.editConnectorCredentialsForm.$valid;
  };
  
  $scope.save = function () {
    if (!isValid()) {
      return;
    }
    
    $scope.editCredentials.connectorId = $scope.connectorConfiguration.connectorId;
    $scope.editCredentials.userId = $scope.currentUser.id;
    $scope.saveConnectorCredentials($scope.editCredentials, function() {
      $scope.connectorCredentialsDialog.close();
    });
  };
}

function ChangePasswordController($scope, $http, App) {
  
  var MODEL = { data: {}, newPasswordRepetition: null, state: null};
  
  $scope.passwordChange = angular.copy(MODEL);
  
  $scope.isValid = function (form) {
    return form && form.$valid;
  };
  
  function isValid() {
    return $scope.isValid($scope.changePasswordForm);
  }
  
  // needed for form validation
  // DO NOT REMOVE FROM CONTROLLER!
  $scope.errorClass = function(form) {
    return (!form || form.$valid || !form.$dirty) ? '' : 'error';
  };
  
  $scope.save = function () {
    if (!isValid()) {
      return;
    }
    
    var data = angular.copy($scope.passwordChange.data);
    
    $http.post(App.uri("secured/resource/user/" + $scope.currentUser.id + "/changePassword"), data)
      .success(function() {
        $scope.passwordChange.state = "SUCCESS";
        $scope.passwordChange.data = {};
        $scope.passwordChange.newPasswordRepetition = null;
      })
      .error(function() {
        $scope.passwordChange.state = "ERROR";
        $scope.passwordChange.data = {};
        $scope.passwordChange.newPasswordRepetition = null;
      });
  };
}
