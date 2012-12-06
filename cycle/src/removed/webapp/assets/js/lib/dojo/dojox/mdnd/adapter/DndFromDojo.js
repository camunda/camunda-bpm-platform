//>>built
define("dojox/mdnd/adapter/DndFromDojo",["dojo/_base/kernel","dojo/_base/declare","dojo/_base/connect","dojo/_base/array","dojo/dom-class","dojo/_base/window","dojox/mdnd/AreaManager","dojo/dnd/Manager"],function(_1,_2,_3,_4,_5){
var _6=_2("dojox.mdnd.adapter.DndFromDojo",null,{dropIndicatorSize:{"w":0,"h":50},dropIndicatorSize:{"w":0,"h":50},_areaManager:null,_dojoManager:null,_currentArea:null,_oldArea:null,_moveHandler:null,_subscribeHandler:null,constructor:function(){
this._areaManager=dojox.mdnd.areaManager();
this._dojoManager=_1.dnd.manager();
this._currentArea=null;
this._moveHandler=null;
this.subscribeDnd();
},subscribeDnd:function(){
this._subscribeHandler=[_3.subscribe("/dnd/start",this,"onDragStart"),_3.subscribe("/dnd/drop/before",this,"onDrop"),_3.subscribe("/dnd/cancel",this,"onDropCancel"),_3.subscribe("/dnd/source/over",this,"onDndSource")];
},unsubscribeDnd:function(){
_4.forEach(this._subscribeHandler,_3.unsubscribe);
},_getHoverArea:function(_7){
var x=_7.x;
var y=_7.y;
this._oldArea=this._currentArea;
this._currentArea=null;
var _8=this._areaManager._areaList;
for(var i=0;i<_8.length;i++){
var _9=_8[i];
var _a=_9.coords.x;
var _b=_a+_9.node.offsetWidth;
var _c=_9.coords.y;
var _d=_c+_9.node.offsetHeight;
if(_a<=x&&x<=_b&&_c<=y&&y<=_d){
this._areaManager._oldIndexArea=this._areaManager._currentIndexArea;
this._areaManager._currentIndexArea=i;
this._currentArea=_9.node;
break;
}
}
if(this._currentArea!=this._oldArea){
if(this._currentArea==null){
this.onDragExit();
}else{
if(this._oldArea==null){
this.onDragEnter();
}else{
this.onDragExit();
this.onDragEnter();
}
}
}
},onDragStart:function(_e,_f,_10){
this._dragNode=_f[0];
this._copy=_10;
this._source=_e;
this._outSourceHandler=_3.connect(this._dojoManager,"outSource",this,function(){
if(this._moveHandler==null){
this._moveHandler=_3.connect(_1.doc,"mousemove",this,"onMouseMove");
}
});
},onMouseMove:function(e){
var _11={"x":e.pageX,"y":e.pageY};
this._getHoverArea(_11);
if(this._currentArea&&this._areaManager._accept){
if(this._areaManager._dropIndicator.node.style.visibility=="hidden"){
this._areaManager._dropIndicator.node.style.visibility="";
_5.add(this._dojoManager.avatar.node,"dojoDndAvatarCanDrop");
}
this._areaManager.placeDropIndicator(_11,this.dropIndicatorSize);
}
},onDragEnter:function(){
var _12=this._dragNode.getAttribute("dndType");
var _13=(_12)?_12.split(/\s*,\s*/):["text"];
this._areaManager._isAccepted(_13,this._areaManager._areaList[this._areaManager._currentIndexArea].accept);
if(this._dojoManager.avatar){
if(this._areaManager._accept){
_5.add(this._dojoManager.avatar.node,"dojoDndAvatarCanDrop");
}else{
_5.remove(this._dojoManager.avatar.node,"dojoDndAvatarCanDrop");
}
}
},onDragExit:function(){
this._areaManager._accept=false;
if(this._dojoManager.avatar){
_5.remove(this._dojoManager.avatar.node,"dojoDndAvatarCanDrop");
}
if(this._currentArea==null){
this._areaManager._dropMode.refreshItems(this._areaManager._areaList[this._areaManager._oldIndexArea],this._areaManager._oldDropIndex,this.dropIndicatorSize,false);
this._areaManager._resetAfterDrop();
}else{
this._areaManager._dropIndicator.remove();
}
},isAccepted:function(_14,_15){
var _16=(_14.getAttribute("dndType"))?_14.getAttribute("dndType"):"text";
if(_16&&_16 in _15){
return true;
}else{
return false;
}
},onDndSource:function(_17){
if(this._currentArea==null){
return;
}
if(_17){
var _18=false;
if(this._dojoManager.target==_17){
_18=true;
}else{
_18=this.isAccepted(this._dragNode,_17.accept);
}
if(_18){
_3.disconnect(this._moveHandler);
this._currentArea=this._moveHandler=null;
var _19=this._areaManager._dropIndicator.node;
if(_19&&_19.parentNode!==null&&_19.parentNode.nodeType==1){
_19.style.visibility="hidden";
}
}else{
this._resetAvatar();
}
}else{
if(!this._moveHandler){
this._moveHandler=_3.connect(_1.doc,"mousemove",this,"onMouseMove");
}
this._resetAvatar();
}
},_resetAvatar:function(){
if(this._dojoManager.avatar){
if(this._areaManager._accept){
_5.add(this._dojoManager.avatar.node,"dojoDndAvatarCanDrop");
}else{
_5.remove(this._dojoManager.avatar.node,"dojoDndAvatarCanDrop");
}
}
},onDropCancel:function(){
if(this._currentArea==null){
this._areaManager._resetAfterDrop();
_3.disconnect(this._moveHandler);
_3.disconnect(this._outSourceHandler);
this._currentArea=this._moveHandler=this._outSourceHandler=null;
}else{
if(this._areaManager._accept){
this.onDrop(this._source,[this._dragNode],this._copy,this._currentArea);
}else{
this._currentArea=null;
_3.disconnect(this._outSourceHandler);
_3.disconnect(this._moveHandler);
this._moveHandler=this._outSourceHandler=null;
}
}
},onDrop:function(_1a,_1b,_1c){
_3.disconnect(this._moveHandler);
_3.disconnect(this._outSourceHandler);
this._moveHandler=this._outSourceHandler=null;
if(this._currentArea){
var _1d=this._areaManager._currentDropIndex;
_3.publish("/dnd/drop/after",[_1a,_1b,_1c,this._currentArea,_1d]);
this._currentArea=null;
}
if(this._areaManager._dropIndicator.node.style.visibility=="hidden"){
this._areaManager._dropIndicator.node.style.visibility="";
}
this._areaManager._resetAfterDrop();
}});
dojox.mdnd.adapter._dndFromDojo=null;
dojox.mdnd.adapter._dndFromDojo=new dojox.mdnd.adapter.DndFromDojo();
return _6;
});
