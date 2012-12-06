//>>built
define("dojox/app/controllers/Transition",["dojo/_base/lang","dojo/_base/declare","dojo/on","dojo/Deferred","dojo/when","dojox/css3/transit","../Controller"],function(_1,_2,on,_3,_4,_5,_6){
return _2("dojox.app.controllers.Transition",_6,{proceeding:false,waitingQueue:[],constructor:function(_7,_8){
this.events={"transition":this.transition,"startTransition":this.onStartTransition};
this.inherited(arguments);
},transition:function(_9){
this.proceedTransition(_9);
},onStartTransition:function(_a){
if(_a.preventDefault){
_a.preventDefault();
}
_a.cancelBubble=true;
if(_a.stopPropagation){
_a.stopPropagation();
}
var _b=_a.detail.target;
var _c=/#(.+)/;
if(!_b&&_c.test(_a.detail.href)){
_b=_a.detail.href.match(_c)[1];
}
this.transition({"viewId":_b,opts:_1.mixin({reverse:false},_a.detail)});
},proceedTransition:function(_d){
if(this.proceeding){
this.app.log("in app/controllers/Transition proceedTransition push event",_d);
this.waitingQueue.push(_d);
return;
}
this.proceeding=true;
this.app.log("in app/controllers/Transition proceedTransition calling trigger load",_d);
var _e=_d.params||{};
if(_d.opts&&_d.opts.params){
_e=_d.params||_d.opts.params;
}
this.app.trigger("load",{"viewId":_d.viewId,"params":_e,"callback":_1.hitch(this,function(){
var _f=this._doTransition(_d.viewId,_d.opts,_e,this.app);
_4(_f,_1.hitch(this,function(){
this.proceeding=false;
var _10=this.waitingQueue.shift();
if(_10){
this.proceedTransition(_10);
}
}));
})});
},_getDefaultTransition:function(_11){
var _12=_11;
var _13=_12.defaultTransition;
while(!_13&&_12.parent){
_12=_12.parent;
_13=_12.defaultTransition;
}
return _13;
},_doTransition:function(_14,_15,_16,_17){
this.app.log("in app/controllers/Transition._doTransition transitionTo=[",_14,"], parent.name=[",_17.name,"], opts=",_15);
if(!_17){
throw Error("view parent not found in transition.");
}
var _18,_19,_1a,_1b,_16,_1c=_17.selectedChild;
if(_14){
_18=_14.split(",");
}else{
_18=_17.defaultView.split(",");
}
_19=_18.shift();
_1a=_18.join(",");
_1b=_17.children[_17.id+"_"+_19];
if(!_1b){
throw Error("child view must be loaded before transition.");
}
_1b.params=_16||_1b.params;
if(!_1a&&_1b.defaultView){
_1a=_1b.defaultView;
}
if(!_1c){
this.app.log("> in Transition._doTransition calling next.beforeActivate next name=[",_1b.name,"], parent.name=[",_1b.parent.name,"],  !current path,");
_1b.beforeActivate();
this.app.log("> in Transition._doTransition calling next.afterActivate next name=[",_1b.name,"], parent.name=[",_1b.parent.name,"],  !current path");
_1b.afterActivate();
this.app.log("  > in Transition._doTransition calling app.triggger select view next name=[",_1b.name,"], parent.name=[",_1b.parent.name,"], !current path");
this.app.trigger("select",{"parent":_17,"view":_1b});
return;
}
if(_1b!==_1c){
var _1d=_1c.selectedChild;
while(_1d){
this.app.log("< in Transition._doTransition calling subChild.beforeDeactivate subChild name=[",_1d.name,"], parent.name=[",_1d.parent.name,"], next!==current path");
_1d.beforeDeactivate();
_1d=_1d.selectedChild;
}
this.app.log("< in Transition._doTransition calling current.beforeDeactivate current name=[",_1c.name,"], parent.name=[",_1c.parent.name,"], next!==current path");
_1c.beforeDeactivate();
this.app.log("> in Transition._doTransition calling next.beforeActivate next name=[",_1b.name,"], parent.name=[",_1b.parent.name,"], next!==current path");
_1b.beforeActivate();
this.app.log("> in Transition._doTransition calling app.triggger select view next name=[",_1b.name,"], parent.name=[",_1b.parent.name,"], next!==current path");
this.app.trigger("select",{"parent":_17,"view":_1b});
var _1e=_5(_1c.domNode,_1b.domNode,_1.mixin({},_15,{transition:this._getDefaultTransition(_17)||"none"}));
_1e.then(_1.hitch(this,function(){
var _1f=_1c.selectedChild;
while(_1f){
this.app.log("  < in Transition._doTransition calling subChild.afterDeactivate subChild name=[",_1f.name,"], parent.name=[",_1f.parent.name,"], next!==current path");
_1f.afterDeactivate();
_1f=_1f.selectedChild;
}
this.app.log("  < in Transition._doTransition calling current.afterDeactivate current name=[",_1c.name,"], parent.name=[",_1c.parent.name,"], next!==current path");
_1c.afterDeactivate();
this.app.log("  > in Transition._doTransition calling next.afterActivate next name=[",_1b.name,"], parent.name=[",_1b.parent.name,"], next!==current path");
_1b.afterActivate();
if(_1a){
this._doTransition(_1a,_15,_16,_1b);
}
}));
return _1e;
}else{
this.app.log("< in Transition._doTransition calling next.beforeDeactivate next name=[",_1b.name,"], parent.name=[",_1b.parent.name,"], next==current path");
_1b.beforeDeactivate();
this.app.log("  < in Transition._doTransition calling next.afterDeactivate next name=[",_1b.name,"], parent.name=[",_1b.parent.name,"], next==current path");
_1b.afterDeactivate();
this.app.log("> in Transition._doTransition calling next.beforeActivate next name=[",_1b.name,"], parent.name=[",_1b.parent.name,"], next==current path");
_1b.beforeActivate();
this.app.log("  > in Transition._doTransition calling next.afterActivate next name=[",_1b.name,"], parent.name=[",_1b.parent.name,"], next==current path");
_1b.afterActivate();
this.app.log("> in Transition._doTransition calling app.triggger select view next name=[",_1b.name,"], parent.name=[",_1b.parent.name,"], next==current path");
this.app.trigger("select",{"parent":_17,"view":_1b});
}
if(_1a){
return this._doTransition(_1a,_15,_16,_1b);
}
}});
});
