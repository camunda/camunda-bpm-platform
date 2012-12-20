//>>built
define("dijit/_HasDropDown",["dojo/_base/declare","dojo/_base/Deferred","dojo/_base/event","dojo/dom","dojo/dom-attr","dojo/dom-class","dojo/dom-geometry","dojo/dom-style","dojo/has","dojo/keys","dojo/_base/lang","dojo/on","dojo/window","./registry","./focus","./popup","./_FocusMixin"],function(_1,_2,_3,_4,_5,_6,_7,_8,_9,_a,_b,on,_c,_d,_e,_f,_10){
return _1("dijit._HasDropDown",_10,{_buttonNode:null,_arrowWrapperNode:null,_popupStateNode:null,_aroundNode:null,dropDown:null,autoWidth:true,forceWidth:false,maxHeight:0,dropDownPosition:["below","above"],_stopClickEvents:true,_onDropDownMouseDown:function(e){
if(this.disabled||this.readOnly){
return;
}
e.preventDefault();
this._docHandler=this.connect(this.ownerDocument,"mouseup","_onDropDownMouseUp");
this.toggleDropDown();
},_onDropDownMouseUp:function(e){
if(e&&this._docHandler){
this.disconnect(this._docHandler);
}
var _11=this.dropDown,_12=false;
if(e&&this._opened){
var c=_7.position(this._buttonNode,true);
if(!(e.pageX>=c.x&&e.pageX<=c.x+c.w)||!(e.pageY>=c.y&&e.pageY<=c.y+c.h)){
var t=e.target;
while(t&&!_12){
if(_6.contains(t,"dijitPopup")){
_12=true;
}else{
t=t.parentNode;
}
}
if(_12){
t=e.target;
if(_11.onItemClick){
var _13;
while(t&&!(_13=_d.byNode(t))){
t=t.parentNode;
}
if(_13&&_13.onClick&&_13.getParent){
_13.getParent().onItemClick(_13,e);
}
}
return;
}
}
}
if(this._opened){
if(_11.focus&&_11.autoFocus!==false){
this._focusDropDownTimer=this.defer(function(){
_11.focus();
delete this._focusDropDownTimer;
});
}
}else{
this.defer("focus");
}
if(_9("ios")){
this._justGotMouseUp=true;
this.defer(function(){
this._justGotMouseUp=false;
});
}
},_onDropDownClick:function(e){
if(_9("touch")&&!this._justGotMouseUp){
this._onDropDownMouseDown(e);
this._onDropDownMouseUp(e);
}
if(this._stopClickEvents){
_3.stop(e);
}
},buildRendering:function(){
this.inherited(arguments);
this._buttonNode=this._buttonNode||this.focusNode||this.domNode;
this._popupStateNode=this._popupStateNode||this.focusNode||this._buttonNode;
var _14={"after":this.isLeftToRight()?"Right":"Left","before":this.isLeftToRight()?"Left":"Right","above":"Up","below":"Down","left":"Left","right":"Right"}[this.dropDownPosition[0]]||this.dropDownPosition[0]||"Down";
_6.add(this._arrowWrapperNode||this._buttonNode,"dijit"+_14+"ArrowButton");
},postCreate:function(){
this.inherited(arguments);
this.own(on(this._buttonNode,"mousedown",_b.hitch(this,"_onDropDownMouseDown")),on(this._buttonNode,"click",_b.hitch(this,"_onDropDownClick")),on(this.focusNode,"keydown",_b.hitch(this,"_onKey")),on(this.focusNode,"keyup",_b.hitch(this,"_onKeyUp")));
},destroy:function(){
if(this.dropDown){
if(!this.dropDown._destroyed){
this.dropDown.destroyRecursive();
}
delete this.dropDown;
}
this.inherited(arguments);
},_onKey:function(e){
if(this.disabled||this.readOnly){
return;
}
var d=this.dropDown,_15=e.target;
if(d&&this._opened&&d.handleKey){
if(d.handleKey(e)===false){
_3.stop(e);
return;
}
}
if(d&&this._opened&&e.keyCode==_a.ESCAPE){
this.closeDropDown();
_3.stop(e);
}else{
if(!this._opened&&(e.keyCode==_a.DOWN_ARROW||((e.keyCode==_a.ENTER||e.keyCode==dojo.keys.SPACE)&&((_15.tagName||"").toLowerCase()!=="input"||(_15.type&&_15.type.toLowerCase()!=="text"))))){
this._toggleOnKeyUp=true;
_3.stop(e);
}
}
},_onKeyUp:function(){
if(this._toggleOnKeyUp){
delete this._toggleOnKeyUp;
this.toggleDropDown();
var d=this.dropDown;
if(d&&d.focus){
this.defer(_b.hitch(d,"focus"),1);
}
}
},_onBlur:function(){
var _16=_e.curNode&&this.dropDown&&_4.isDescendant(_e.curNode,this.dropDown.domNode);
this.closeDropDown(_16);
this.inherited(arguments);
},isLoaded:function(){
return true;
},loadDropDown:function(_17){
_17();
},loadAndOpenDropDown:function(){
var d=new _2(),_18=_b.hitch(this,function(){
this.openDropDown();
d.resolve(this.dropDown);
});
if(!this.isLoaded()){
this.loadDropDown(_18);
}else{
_18();
}
return d;
},toggleDropDown:function(){
if(this.disabled||this.readOnly){
return;
}
if(!this._opened){
this.loadAndOpenDropDown();
}else{
this.closeDropDown();
}
},openDropDown:function(){
var _19=this.dropDown,_1a=_19.domNode,_1b=this._aroundNode||this.domNode,_1c=this;
if(!this._preparedNode){
this._preparedNode=true;
if(_1a.style.width){
this._explicitDDWidth=true;
}
if(_1a.style.height){
this._explicitDDHeight=true;
}
}
if(this.maxHeight||this.forceWidth||this.autoWidth){
var _1d={display:"",visibility:"hidden"};
if(!this._explicitDDWidth){
_1d.width="";
}
if(!this._explicitDDHeight){
_1d.height="";
}
_8.set(_1a,_1d);
var _1e=this.maxHeight;
if(_1e==-1){
var _1f=_c.getBox(this.ownerDocument),_20=_7.position(_1b,false);
_1e=Math.floor(Math.max(_20.y,_1f.h-(_20.y+_20.h)));
}
_f.moveOffScreen(_19);
if(_19.startup&&!_19._started){
_19.startup();
}
var mb=_7.getMarginSize(_1a);
var _21=(_1e&&mb.h>_1e);
_8.set(_1a,{overflowX:"visible",overflowY:_21?"auto":"visible"});
if(_21){
mb.h=_1e;
if("w" in mb){
mb.w+=16;
}
}else{
delete mb.h;
}
if(this.forceWidth){
mb.w=_1b.offsetWidth;
}else{
if(this.autoWidth){
mb.w=Math.max(mb.w,_1b.offsetWidth);
}else{
delete mb.w;
}
}
if(_b.isFunction(_19.resize)){
_19.resize(mb);
}else{
_7.setMarginBox(_1a,mb);
}
}
var _22=_f.open({parent:this,popup:_19,around:_1b,orient:this.dropDownPosition,onExecute:function(){
_1c.closeDropDown(true);
},onCancel:function(){
_1c.closeDropDown(true);
},onClose:function(){
_5.set(_1c._popupStateNode,"popupActive",false);
_6.remove(_1c._popupStateNode,"dijitHasDropDownOpen");
_1c._set("_opened",false);
}});
_5.set(this._popupStateNode,"popupActive","true");
_6.add(this._popupStateNode,"dijitHasDropDownOpen");
this._set("_opened",true);
this.domNode.setAttribute("aria-expanded","true");
return _22;
},closeDropDown:function(_23){
if(this._focusDropDownTimer){
this._focusDropDownTimer.remove();
delete this._focusDropDownTimer;
}
if(this._opened){
this.domNode.setAttribute("aria-expanded","false");
if(_23){
this.focus();
}
_f.close(this.dropDown);
this._opened=false;
}
}});
});
