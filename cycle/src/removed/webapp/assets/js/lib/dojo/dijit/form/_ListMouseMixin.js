//>>built
define("dijit/form/_ListMouseMixin",["dojo/_base/declare","dojo/mouse","dojo/on","dojo/touch","./_ListBase"],function(_1,_2,on,_3,_4){
return _1("dijit.form._ListMouseMixin",_4,{postCreate:function(){
this.inherited(arguments);
this.own(on(this.domNode,_3.press,function(_5){
_5.preventDefault();
}));
this._listConnect(_3.press,"_onMouseDown");
this._listConnect(_3.release,"_onMouseUp");
this._listConnect(_2.enter,"_onMouseOver");
this._listConnect(_2.leave,"_onMouseOut");
},_onMouseDown:function(_6,_7){
if(this._hoveredNode){
this.onUnhover(this._hoveredNode);
this._hoveredNode=null;
}
this._isDragging=true;
this._setSelectedAttr(_7);
},_onMouseUp:function(_8,_9){
this._isDragging=false;
var _a=this.selected;
var _b=this._hoveredNode;
if(_a&&_9==_a){
this.onClick(_a);
}else{
if(_b&&_9==_b){
this._setSelectedAttr(_b);
this.onClick(_b);
}
}
},_onMouseOut:function(_c,_d){
if(this._hoveredNode){
this.onUnhover(this._hoveredNode);
this._hoveredNode=null;
}
if(this._isDragging){
this._cancelDrag=(new Date()).getTime()+1000;
}
},_onMouseOver:function(_e,_f){
if(this._cancelDrag){
var _10=(new Date()).getTime();
if(_10>this._cancelDrag){
this._isDragging=false;
}
this._cancelDrag=null;
}
this._hoveredNode=_f;
this.onHover(_f);
if(this._isDragging){
this._setSelectedAttr(_f);
}
}});
});
