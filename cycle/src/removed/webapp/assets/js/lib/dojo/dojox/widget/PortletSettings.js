//>>built
define("dojox/widget/PortletSettings",["dojo/_base/declare","dojo/_base/kernel","dojo/fx","dijit/TitlePane"],function(_1,_2,fx,_3){
return _1("dojox.widget.PortletSettings",[dijit._Container,dijit.layout.ContentPane],{portletIconClass:"dojoxPortletSettingsIcon",portletIconHoverClass:"dojoxPortletSettingsIconHover",postCreate:function(){
dojo.style(this.domNode,"display","none");
dojo.addClass(this.domNode,"dojoxPortletSettingsContainer");
dojo.removeClass(this.domNode,"dijitContentPane");
},_setPortletAttr:function(_4){
this.portlet=_4;
},toggle:function(){
var n=this.domNode;
if(dojo.style(n,"display")=="none"){
dojo.style(n,{"display":"block","height":"1px","width":"auto"});
dojo.fx.wipeIn({node:n}).play();
}else{
dojo.fx.wipeOut({node:n,onEnd:dojo.hitch(this,function(){
dojo.style(n,{"display":"none","height":"","width":""});
})}).play();
}
}});
});
