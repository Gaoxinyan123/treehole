(global["webpackJsonp"]=global["webpackJsonp"]||[]).push([["components/uni-pagination/uni-pagination"],{"56e6":function(n,e,t){"use strict";var i=t("b7ac"),c=t.n(i);c.a},"9f41":function(n,e,t){"use strict";t.r(e);var i,c={uniIcons:function(){return Promise.all([t.e("common/vendor"),t.e("components/uni-icons/uni-icons")]).then(t.bind(null,"2ba4"))}},r=function(){var n=this,e=n.$createElement;n._self._c},u=[],o=function(){Promise.all([t.e("common/vendor"),t.e("components/uni-icons/uni-icons")]).then(function(){return resolve(t("2ba4"))}.bind(null,t)).catch(t.oe)},a={name:"UniPagination",components:{uniIcons:o},props:{prevText:{type:String,default:"上一页"},nextText:{type:String,default:"下一页"},current:{type:[Number,String],default:1},total:{type:[Number,String],default:0},pageSize:{type:[Number,String],default:10},showIcon:{type:[Boolean,String],default:!1}},data:function(){return{currentIndex:1}},computed:{maxPage:function(){var n=1,e=Number(this.total),t=Number(this.pageSize);return e&&t&&(n=Math.ceil(e/t)),n}},watch:{current:function(n){this.currentIndex=+n}},created:function(){this.currentIndex=+this.current},methods:{clickLeft:function(){1!==Number(this.currentIndex)&&(this.currentIndex-=1,this.change("prev"))},clickRight:function(){Number(this.currentIndex)!==this.maxPage&&(this.currentIndex+=1,this.change("next"))},change:function(n){this.$emit("change",{type:n,current:this.currentIndex})}}},s=a,l=(t("56e6"),t("f0c5")),h=Object(l["a"])(s,r,u,!1,null,"2db378a7",null,!1,c,i);e["default"]=h.exports},b7ac:function(n,e,t){}}]);
;(global["webpackJsonp"] = global["webpackJsonp"] || []).push([
    'components/uni-pagination/uni-pagination-create-component',
    {
        'components/uni-pagination/uni-pagination-create-component':(function(module, exports, __webpack_require__){
            __webpack_require__('543d')['createComponent'](__webpack_require__("9f41"))
        })
    },
    [['components/uni-pagination/uni-pagination-create-component']]
]);
