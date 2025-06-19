(global["webpackJsonp"]=global["webpackJsonp"]||[]).push([["components/uni-badge/uni-badge"],{"7eb1":function(t,e,n){"use strict";var i;n.r(e);var a,l=function(){var t=this,e=t.$createElement;t._self._c},u=[],c={name:"UniBadge",props:{type:{type:String,default:"default"},inverted:{type:Boolean,default:!1},text:{type:[String,Number],default:""},size:{type:String,default:"normal"}},data:function(){return{badgeStyle:""}},watch:{text:function(){this.setStyle()}},mounted:function(){this.setStyle()},methods:{setStyle:function(){this.badgeStyle="width: ".concat(8*String(this.text).length+12,"px")},onClick:function(){this.$emit("click")}}},o=c,s=(n("f18e"),n("f0c5")),d=Object(s["a"])(o,l,u,!1,null,"45e1f236",null,!1,i,a);e["default"]=d.exports},d17d:function(t,e,n){},f18e:function(t,e,n){"use strict";var i=n("d17d"),a=n.n(i);a.a}}]);
;(global["webpackJsonp"] = global["webpackJsonp"] || []).push([
    'components/uni-badge/uni-badge-create-component',
    {
        'components/uni-badge/uni-badge-create-component':(function(module, exports, __webpack_require__){
            __webpack_require__('543d')['createComponent'](__webpack_require__("7eb1"))
        })
    },
    [['components/uni-badge/uni-badge-create-component']]
]);
