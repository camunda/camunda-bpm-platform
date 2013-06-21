'use strict';

/* Plugin Services */

define([ "angular" ], function(angular) {

  var module = angular.module("cockpit.services");

  function ActivityInstanceProvider() {

    function aggregateActivityInstancesHelper(activityInstance, result) {
      var children = activityInstance.childActivityInstances;
      for (var i = 0; i < children.length; i++) {
        var child = children[i];
        aggregateActivityInstancesHelper(child, result);
        
        var mappings = result[child.activityId];
        if (!mappings) {
          mappings = [];
          result[child.activityId] = mappings;
        }
        mappings.push(child);
      }

      var transitions = activityInstance.childTransitionInstances;
      for (var i = 0; i < transitions.length; i++) {
        var transition = transitions[i];
        
        var mappings = result[transition.targetActivityId];
        if (!mappings) {
          mappings = [];
          result[transition.targetActivityId] = mappings;
        }
        mappings.push(transition);
      };
    }
    
    function addChildren(parent, processSemantic, activityInstance, activityIdToNodeMap) {
      angular.forEach(activityInstance.childActivityInstances, function(childActivityInstance) {
        
        // create and decorate child node
        var childNode = {};
        childNode.id = childActivityInstance.id;
        childNode.activityId = childActivityInstance.activityId;
        childNode.children = [];
        childNode.isOpen = true;
        childNode.isSelected = false;
        
        var instanceList = activityIdToNodeMap[childNode.activityId];
        if (!instanceList) {
          instanceList = activityIdToNodeMap[childNode.activityId] = [];
        }
        
        instanceList.push(childNode);
        
        // get the label for child
        childNode.label = getActivityName(processSemantic.baseElements, childActivityInstance.activityId);
        
        // add parent the child node
        parent.children.push(childNode);
        
        // call recursive add children for child node as parent
        addChildren(childNode, processSemantic, childActivityInstance, activityIdToNodeMap);
      });
      
      angular.forEach(activityInstance.childTransitionInstances, function(childTransitionInstance) {
        
        // create and decorate child node
        var childNode = {};
        childNode.id = childTransitionInstance.id;
        childNode.activityId = childTransitionInstance.targetActivityId;
        childNode.children = [];
        childNode.isOpen = true;
        childNode.isSelected = false;
        
        var instanceList = activityIdToNodeMap[childNode.activityId];
        if (!instanceList) {
          instanceList = activityIdToNodeMap[childNode.activityId] = [];
        }
        
        instanceList.push(childNode);
        
        // get the label for child
        childNode.label = getActivityName(processSemantic.baseElements, childTransitionInstance.targetActivityId);
        
        // add parent the child node
        parent.children.push(childNode);
      });
    }
    
    function getActivityName(elements, activityId) {
      var name = null;
      for (var i = 0; i < elements.length; i++) {
        var element = elements[i];
        if (element.id === activityId) {
          name = element.name;
          if (!name) {
            name = element.type + ' (' + element.id + ')';
          }
          return name;
        }
        if (element.baseElements) {
          name = getActivityName(element.baseElements, activityId);
          if (name) {
            return name;
          }
        }
      }
    }

    function aggregateActivityInstances(activityInstances) {
      var result = [];
      aggregateActivityInstancesHelper(activityInstances, result);
      return result;
    }
    
    function createActivityInstanceTree(processDefinition, semantic, activityInstances, activityIdToNodeMap) {
      // get the corresponding semantic for the model.
      var correspondingSemantic = null;
      for (var i = 0; i < semantic.length; i++) {
        var currentSemantic = semantic[i];
        if (currentSemantic.id === processDefinition.key && currentSemantic.type === 'process') {
          correspondingSemantic = currentSemantic;
        }
      }
      // create and decorate root
      var root = {};
      root.id = activityInstances.id;
      root.activityId = activityInstances.id;
      root.label = correspondingSemantic.name;
      root.children = [];
      root.isOpen = true;
      root.isSelected = false;
      
      var instanceList = activityIdToNodeMap[root.activityId];
      if (!instanceList) {
        instanceList = activityIdToNodeMap[root.activityId] = [];
      }
      
      instanceList.push(root);
      
      // add children
      addChildren(root, correspondingSemantic, activityInstances, activityIdToNodeMap);
      
      return root;
    }
    
    return {
      aggregateActivityInstances: aggregateActivityInstances,
      createActivityInstanceTree: createActivityInstanceTree
    };
    
  }
  
  module.factory('ActivityInstance', ActivityInstanceProvider);
  // end config

  return module;
  
});
