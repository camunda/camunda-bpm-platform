//>>built
define("dojox/widget/Portlet",["dojo/_base/declare","dojo/_base/kernel","dojo/fx","dijit/TitlePane","./PortletSettings","./PortletDialogSettings"],function(_1,_2,fx,_3,_4,_5){
_2.experimental("dojox.widget.Portlet");
return _1("dojox.widget.Portlet",[_3,dijit._Container],{resizeChildren:true,closable:true,_parents:null,_size:null,dragRestriction:false,buildRendering:function(){
this.inherited(arguments);
dojo.style(this.domNode,"visibility","hidden");
},postCreate:function(){
this.inherited(arguments);
dojo.addClass(this.domNode,"dojoxPortlet");
dojo.removeClass(this.arrowNode,"dijitArrowNode");
dojo.addClass(this.arrowNode,"dojoxPortletIcon dojoxArrowDown");
dojo.addClass(this.titleBarNode,"dojoxPortletTitle");
dojo.addClass(this.hideNode,"dojoxPortletContentOuter");
dojo.addClass(this.domNode,"dojoxPortlet-"+(!this.dragRestriction?"movable":"nonmovable"));
var _6=this;
if(this.resizeChildren){
this.subscribe("/dnd/drop",function(){
_6._updateSize();
});
this.subscribe("/Portlet/sizechange",function(_7){
_6.onSizeChange(_7);
});
this.connect(window,"onresize",function(){
_6._updateSize();
});
var _8=dojo.hitch(this,function(id,_9){
var _a=dijit.byId(id);
if(_a.selectChild){
var s=this.subscribe(id+"-selectChild",function(_b){
var n=_6.domNode.parentNode;
while(n){
if(n==_b.domNode){
_6.unsubscribe(s);
_6._updateSize();
break;
}
n=n.parentNode;
}
});
var _c=dijit.byId(_9);
if(_a&&_c){
_6._parents.push({parent:_a,child:_c});
}
}
});
var _d;
this._parents=[];
for(var p=this.domNode.parentNode;p!=null;p=p.parentNode){
var id=p.getAttribute?p.getAttribute("widgetId"):null;
if(id){
_8(id,_d);
_d=id;
}
}
}
this.connect(this.titleBarNode,"onmousedown",function(_e){
if(dojo.hasClass(_e.target,"dojoxPortletIcon")){
dojo.stopEvent(_e);
return false;
}
return true;
});
this.connect(this._wipeOut,"onEnd",function(){
_6._publish();
});
this.connect(this._wipeIn,"onEnd",function(){
_6._publish();
});
if(this.closable){
this.closeIcon=this._createIcon("dojoxCloseNode","dojoxCloseNodeHover",dojo.hitch(this,"onClose"));
dojo.style(this.closeIcon,"display","");
}
},startup:function(){
if(this._started){
return;
}
var _f=this.getChildren();
this._placeSettingsWidgets();
dojo.forEach(_f,function(_10){
try{
if(!_10.started&&!_10._started){
_10.startup();
}
}
catch(e){
}
});
this.inherited(arguments);
dojo.style(this.domNode,"visibility","visible");
},_placeSettingsWidgets:function(){
dojo.forEach(this.getChildren(),dojo.hitch(this,function(_11){
if(_11.portletIconClass&&_11.toggle&&!_11.get("portlet")){
this._createIcon(_11.portletIconClass,_11.portletIconHoverClass,dojo.hitch(_11,"toggle"));
dojo.place(_11.domNode,this.containerNode,"before");
_11.set("portlet",this);
this._settingsWidget=_11;
}
}));
},_createIcon:function(_12,_13,fn){
var _14=dojo.create("div",{"class":"dojoxPortletIcon "+_12,"waiRole":"presentation"});
dojo.place(_14,this.arrowNode,"before");
this.connect(_14,"onclick",fn);
if(_13){
this.connect(_14,"onmouseover",function(){
dojo.addClass(_14,_13);
});
this.connect(_14,"onmouseout",function(){
dojo.removeClass(_14,_13);
});
}
return _14;
},onClose:function(evt){
dojo.style(this.domNode,"display","none");
},onSizeChange:function(_15){
if(_15==this){
return;
}
this._updateSize();
},_updateSize:function(){
if(!this.open||!this._started||!this.resizeChildren){
return;
}
if(this._timer){
clearTimeout(this._timer);
}
this._timer=setTimeout(dojo.hitch(this,function(){
var _16={w:dojo.style(this.domNode,"width"),h:dojo.style(this.domNode,"height")};
for(var i=0;i<this._parents.length;i++){
var p=this._parents[i];
var sel=p.parent.selectedChildWidget;
if(sel&&sel!=p.child){
return;
}
}
if(this._size){
if(this._size.w==_16.w&&this._size.h==_16.h){
return;
}
}
this._size=_16;
var fns=["resize","layout"];
this._timer=null;
var _17=this.getChildren();
dojo.forEach(_17,function(_18){
for(var i=0;i<fns.length;i++){
if(dojo.isFunction(_18[fns[i]])){
try{
_18[fns[i]]();
}
catch(e){
}
break;
}
}
});
this.onUpdateSize();
}),100);
},onUpdateSize:function(){
},_publish:function(){
dojo.publish("/Portlet/sizechange",[this]);
},_onTitleClick:function(evt){
if(evt.target==this.arrowNode){
this.inherited(arguments);
}
},addChild:function(_19){
this._size=null;
this.inherited(arguments);
if(this._started){
this._placeSettingsWidgets();
this._updateSize();
}
if(this._started&&!_19.started&&!_19._started){
_19.startup();
}
},destroyDescendants:function(_1a){
this.inherited(arguments);
if(this._settingsWidget){
this._settingsWidget.destroyRecursive(_1a);
}
},destroy:function(){
if(this._timer){
clearTimeout(this._timer);
}
this.inherited(arguments);
},_setCss:function(){
this.inherited(arguments);
dojo.style(this.arrowNode,"display",this.toggleable?"":"none");
}});
});
