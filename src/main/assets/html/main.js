(function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
var details = require('./details');
var score = require('./score');
var search = require('./search');
var core = require('./core');

$(function() {
  details();
  score();
  search();
});

},{"./core":2,"./details":3,"./score":4,"./search":5}],2:[function(require,module,exports){
module.exports = (function(){

    function getMetaContent(property){
        var tags = document.getElementsByTagName('meta');
        var txt = "";
        console.log(tags.length);
        for(var t=0; t<tags.length; t++){

            if(tags[t].getAttribute("property") ==property){

                txt = tags[t].content;
                break;
            }
        }
        return txt;
    }
    window.getMetaContent = getMetaContent;

    var logAppCacheChangeState = function(){
      // set of listeners to deal with caching status
        function logEvent(event) {
            console.log(event.type)
            window.location = "action:"+event.type;
        }
        //reading manifest for the first time of check for change
        window.applicationCache.addEventListener('checking', logEvent, false);
        //new version of the cache is ready o be swapped in
        window.applicationCache.addEventListener('updateready', logEvent, false);
        //no change to manifest
        window.applicationCache.addEventListener('noupdate', logEvent, false);
        // downloading resource
        window.applicationCache.addEventListener('downloading', logEvent, false);
        // all listed resources cached
        window.applicationCache.addEventListener('cached', logEvent, false);
        // code 404 or 410 cache has been deleted
        window.applicationCache.addEventListener('obsolete', logEvent, false);

        //error occured downloading manifest and so aborted
        window.applicationCache.addEventListener('error', logEvent, false);        
    }

    
    var goToDetails = function(){
        // Go to details page on list item click.
        $('.item[data-url]').click(function(){
           window.location.href = $(this).attr('data-url');
        });
    }


    // Events
    $( document ).ready(function(){
        logAppCacheChangeState();
        goToDetails();
    });

    return {
        

    }

}());
},{}],3:[function(require,module,exports){
/**
 * Standard Details
 * ----------------
 * Fcuntionality for the standard details page
 **/

module.exports = function() {
  var descriptionHeight = 150;
  var init = function () {
    partialHide($("#description"));
    tabs();
  }

  var tabs = function() {
    $("#standard #tabs article").hide();
    $("#standard #tabs article").first().show();


    var showTab = function(index) {
      // change active tab
      $("#standard #tabs li").removeClass("active");
      $("#standard #tabs li").eq(index).addClass("active");
      // show the correct content
      $("#standard #tabs article").hide();
      $("#standard #tabs article").eq(index).show();
    }

    $("#tabs nav ul li").click(function(e) {
      showTab($(this).index());
    });

  }

  var partialHide = function(el) {
    var div;

    if(el.height() <= descriptionHeight) {
      return;
    }
    var createWrapperDiv = function() {
      if(el.find("div").length) {
        div = el.find("div");
      }
      else {
        el.wrapInner("<div>");
        div = el.find("div");
      }
    }
    var addOverflow = function() {
      div.height(descriptionHeight+"px");
      div.addClass("fade");
      div.css('overflow', 'hidden');
    }
    var addButton = function() {
      el.find("button").remove();
      el.append("<button class='expand'>Show more</button>");
      el.find("button").click(function() {
        partialShow(el);
      });
    }

    createWrapperDiv();
    addOverflow();
    addButton();
  }
  var partialShow = function(el) {
    var div = el.find("div");
    div.height("auto");
    div.removeClass("fade");
    el.find("button").remove();
    el.append("<button class='hide'>Show less</button>");
    el.find("button").click(function() {
      partialHide(el);
    });
  }

  init();
};

},{}],4:[function(require,module,exports){
/**********************************************************************
 Score bar charts
**********************************************************************/
module.exports = function() {
  var maxValue = 100;
  var valueStringWidth = 70;

  var supported = (document.createElement('progress').max !== undefined);

  var progressBar = function() {

    /** Progress element IS supported **/
    var progressStyle = function(el) {
      var colourBar = function(el) {
        var colour = progressGetColour(el);
        var id = $(el).attr("id");
        // nasty hack, but the only way to do it!
        document.styleSheets[0].insertRule("progress#"+id+"::-webkit-progress-value { background: "+colour+" !important}", 1);
      }


      var values = progressGetValues(el);
      $(el).after(setScoreHtml(values));
      $(el).width(getBarWidth($(el).parent().width()))
      colourBar(el);

    }

    /** Progress element IS NOT supported **/
    var progressShiv = function(el) {
      var colour = progressGetColour(el);
      var values = progressGetValues(el);

      var shiv = $('<span class="progress"/>', {
        style: "background: "+colour,
      }).width(getBarWidth($(el).parent().width()))

      $(el).after(shiv);
      shiv.after(setScoreHtml(values));

      $(el).remove();
    }

    var progressGetColour = function(el) {
      return $(el).attr("data-color");
    }
    var progressGetValues = function(el) {
      var max = $(el).attr("max");
      var value = $(el).attr("value");
      return {'max': max, 'value': value};
    }
    var setScoreHtml = function(values) {
      return "<strong>"+values.value+"</strong>/"+maxValue;
    }
    var getBarWidth = function(availableWidth) {
      return availableWidth - valueStringWidth;

    }

    $("progress").each(function() {
      if(supported) {
        progressStyle(this);
      }
      else {
        progressShiv(this);
      }
    });
  }

  progressBar();
}

},{}],5:[function(require,module,exports){
/**********************************************************************
 Search functionality
 This is a "type forward" search for the Standard List page
**********************************************************************/

module.exports = function() {
  var data = false;

  var init = function() {
    if(!document.getElementById('standards-list')) { return; }
    data = JSON.parse(document.getElementById('standards-list').innerHTML);
    if(!data) { return; }

    var el = $('#search input');
    el.bind('keyup', function(event) {
      $(".item").hide();
      $(".no-results").hide();

      var re = new RegExp(el.val().trim(), "i");
      var results = 0;
      for(var i=0; i<data.length; i++) {
        if(data[i].name.match(re)) {
          $(".item[data-id='"+data[i].id+"']").show();
          results += 1;
        }
      }

      if(!results) {
        $(".no-results").show();
      }
    });
  }

  init();
}

},{}]},{},[1]);
