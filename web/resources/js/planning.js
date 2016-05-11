/*
 * @(#)planning.js	1.2.1 09/05/16
 *
 * Copyright (c) 2015-2016 Musiques Tangentes. All Rights Reserved.
 *
 * This file is part of Algem Web App.
 * Algem Web App is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Algem Web App is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Algem Web App. If not, see <http://www.gnu.org/licenses/>.
 *
 */

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @since 09/05/15
 * @version 1.2.0
 * @returns {void}
 */
//var isMobileAndWebkit = isMobile() && 'WebkitAppearance' in document.documentElement.style;
/**
 * Group constructor.
 * @param {Number} id group Id
 * @param {String} name group name
 * @returns {Group}
 */
function Group(id, name) {
  this.id = id;
  this.name = name;
}

function logVars() {
  var l = arguments.length;
  if (l == 0) {return;}
  for (var i = 0 ;i < l; i++) {
    $.each(arguments[i], function (key, value) {
      console.log(key, value);
    });
  }
}

function setUI() {
  setWidth();
  setScroll();
  setResize();
  setHoverStyle();
  setDialog();
  setBookingDialog();
}

function setViewPortSize(timeOffset) {
  $("#canvas, #grid, .schedule_col").css("height", (1440 - timeOffset) +'px');
  $("footer").css("top", (1442 - timeOffset) +'px');
}

/**
 * Init navigation elements.
 * @param {Object} commonParams global parameters
 * @returns {undefined}
 */
function setDatePicker(commonParams) {

  var picker = $("#datepicker");
  var estabSelect = $("#estabSelection");
  //picker.datepicker({ appendText: "(jj-mm-yyyy)", changeMonth: true, changeYear: true, autoSize: true })
  picker.datepicker({changeMonth: true, changeYear: true});
  picker.datepicker('setDate', commonParams.date);
  picker.datepicker("refresh");
  $(estabSelect).val(commonParams.estab);
  document.title = 'Planning ' + $(estabSelect).children("option:selected").text();
  picker.change(function () {
    //console.log(this.value);
    window.location = 'daily.html?d=' + this.value + '&e=' + commonParams.estab;
  });

  //Next Day Link
  $('a#next').click(function () {
    var date = new Date(picker.datepicker('getDate'));
    date.setDate(date.getDate() + 1);
    picker.datepicker('setDate', date).change();
    return false;
  });
  //Previous Day Link
  $('a#previous').click(function () {
    var date = new Date(picker.datepicker('getDate'));
    date.setDate(date.getDate() - 1);
    picker.datepicker('setDate', date).change();
    return false;
  });

  $(estabSelect).change(function () {
    var eId = $(this).children("option:selected").val();
    window.location = 'daily.html?d=' + $(picker).val() + '&e=' + eId;
  });

}

/**
 * Auto-resize the canvas if its width is larger than window width.
 * @returns {undefined}
 */
function setWidth() {
  var cols = $('.schedule_col').length;
  var canvas = $("#canvas");
  var ww = $(window).width();
  var leftMargin = getMarginGrid();
  var actualColWidth = parseInt($('.schedule_col').css('width'));
  var cw = leftMargin + ((cols + 1) * actualColWidth);
  //console.log("canvas :"+ cw+" window : "+ww)
  var gridWidth = leftMargin + cw;
  if (cw > ww) {
    $("#grid").css({width: gridWidth + "px"});
  } else {
    $("#grid").css({width: "100%"});
  }
  $(canvas).css({width: cw + "px"});
}

/**
 * Manage scroll event.
 * @returns {undefined}
 */
function setScroll() {
  $(window).scroll(function () {
    setTopBar();
  });
}

function setTopBar() {
  var headerHeight = parseInt($("header").css('height'));
  var titleBar = $(".title_col");
  var emSize = parseFloat($(titleBar).css("font-size")) * 1.5;
  //console.log(emSize);
  var offset = $(window).scrollTop();
  //console.log(offset);
  if (offset > headerHeight) {
      $(titleBar).css('transform', 'translateY('+(offset+emSize-headerHeight)+'px)');
  } else {
      $(titleBar).css('transform', 'translateY('+0+'px)');
  }
}

function setResize() {
  $(window).resize(function() {
    setWidth();
    setTopBar();
  });
}

/**
 * Gets the left margin size.
 * @returns {getMarginGrid.emSize|Number}
 */
function getMarginGrid() {
  var emSize = parseFloat($(canvas).css("font-size"));
  return 2.2 * emSize;
}

/**
 * Set the style of hovered schedules.
 * @returns {undefined}
 */
function setHoverStyle() {
  $('div.labels,div.schedule,p.title_col').hover(
    function () {
      $(this).css({
        cursor: "not-allowed",
//        'box-shadow': 'inset 0px 0px 1px 1px rgba(0,0,0,1)',
//        opacity: "0.8"
      });
    },
    function () {
      $(this).css({
        cursor: "default",
//        'box-shadow': 'none',
//        opacity: "1"
      });
    }
  );
  $('div.schedule_col').hover(
      function () {
        $(this).css({
          cursor: "pointer"
//          cursor: "cell"
        });
      },
      function () {
        $(this).css({
          cursor: "default"
        });
      }
  );
  $('div.closed').hover(
      function () {
        $(this).css({
          cursor: "not-allowed",
        });
      },
      function () {
        $(this).css({
          cursor: "default"
        });
      }
  );
}

/**
 * Init main and error dialogs.
 * @returns {undefined}
 */
function setDialog() {
  var mainDialog = $("#dialog");
  $(mainDialog).dialog({
    modal: false,
    autoOpen: false
  });

//  $('div.labels').click(function () {
//    ////var regex = /<br\s*[\/]?>/gi;
//    ////var text = $(this).html().replace(regex,'\n');
//    $(mainDialog).html($(this).html());
//    $(mainDialog).dialog("open");
//  });
  $("#errorDialog").dialog({
    modal: true,
    autoOpen: false,
    position: {my: "bottom", at: "top+50%", of: window},
    buttons: {
      Ok: function () {
        $(this).dialog("close");
      }
    }
  });
}

/**
 * Init booking dialog.
 * @returns {undefined}
 */
function setBookingDialog() {
  $("#booking").dialog({
    modal: false,
    autoOpen: false,
//    width: 380,
    maxHeight: 440,
    position: { my: "top", at: "top+25%", of: window }
  });
//  $("#pass").button({
//    text: true
//  }).change(function () {
//    $(this).button("option", {
//      icons: {primary: this.checked ? 'ui-icon-check' : ''}
//    });
//  });
}

/**
 *
 * @param {Object} params booking params
 * @param {Object} steps height and step rules
 * @returns {undefined}
 */
function setBooking(params, steps) {
  var date = new Date($("#datepicker").datepicker('getDate'));
  $(".schedule_col").click(function (e) {
    //var posX = $(this).offset().left;
    //console.log((e.pageX - posX) + ' , ' + (e.pageY - posY));
    var target = e.target || e.srcElement;
    if ("schedule_col" === target.className) {
      var roomId = $(this).attr("id");
      var posY = $(this).offset().top;
      var idx = getStartTimeIndex(steps.height, e.pageY - posY);
      //console.log("index start time" + idx);
      $("#startTime option").eq(idx).prop("selected", true);
      //console.log("check booking");
      if (params.memberShipRequired && !checkMember(params)) {
        //console.log("Not an active member");
        $("#errorDialog").html("<p>" + params.bookingMemberWarning + "</p>");
        $("#errorDialog").dialog("open");
        return;
      }

      if (!checkBookingDelay(date, params.minDelay)) {
        //console.log("Hors delai");
        $("#errorDialog").html("<p>" +params.bookingMinDelayWarning +"</p>");
        $("#errorDialog").dialog("open");
        return;
      }
      if (!checkBookingDate(date, params.maxDelay)) {
        //console.log("Hors limite");
        $("#errorDialog").html("<p>" +params.bookingMaxDelayWarning +"</p>");
        $("#errorDialog").dialog("open");
        return;
      }

      //console.log("position : " + (e.pageY - posY), $(this).attr("id"), room.text());
      $("#groupInfo").remove();
      $("#member").prop("checked", true);//SET MEMBER BY DEFAULT
      //*******OPEN DIALOG********
      $("#booking").dialog("open");
      //**************************

      $("#room").val(roomId);
      $("#booking #timelength").change(function() {
        var t = $(this).val();
        var startIndex = $("#startTime option:selected").index();
        var endIndex = startIndex + (t * 60 * (steps.max * 2) / (steps.max * 60));
        var lastIndex = $("#endTime option:last-child").index();
        if (endIndex > lastIndex) endIndex = lastIndex;
        $("#endTime option").eq(endIndex).prop("selected", true);
      });

      setEndIndex($("#startTime"), steps);
      setPassChecked();
    }
  });

  //synchronise start time and endtime on modification
  $('#startTime').focus(function() {
    //Store old value
    $(this).data("lastValue",$(this).val());
  });
  $("#startTime").change(function(event) {
    if (checkBookingDelay(date, params.minDelay)) {
      setEndIndex($(this), steps);
    } else {
      var last = $(this).data("lastValue");
      $(this).val(last);
      alert("Hors delai");
      event.preventDefault();
    }
  });
  // call ajax method
  $('#booking-form input[type=radio]').change(function () {
    $("#groupInfo").remove();
    getGroups(params);
  });

  /*$("#booking-form input[type='submit']").click(function () {
    console.log("booking submit button");
  });*/

  //unactive ENTER key on booking dialog
  $("#booking-form").bind("keypress", function (e) {
    if (e.keyCode == 13 || e.keyCode == 169) {
      e.preventDefault();
      return false;
    }
  });

}

function checkMember(params) {
 if (isInactive()) {
    return true;
  }
  var urlPath = $("#booking-form #xmember-url").val();
  var result = false;
  $.ajax({
        url: urlPath,
        type: 'get',
        data:  {start: params.preEnrolmentStart, end: params.endOfYear},
        dataType: 'json',
        async: false,
        success: function(data) {
          result = data;
        }
     });
     //console.log(result);
     return result;
}

function checkBookingDelay(date, minDelay) {
  var t = $("#startTime").val();
  if (t === undefined) {
    return true; // important : true ! let open dialog
  }
  var now = new Date();
  //console.log(t);
  date.setHours(t.substr(0, 2));
  date.setMinutes(t.substr(3, 2));
  if (now.getTime() + (minDelay * 60 * 60 * 1000) > date.getTime()) {
    return false;
  }
  return true;
}

function checkBookingDate(date, maxDelay) {
  var t = $("#startTime").val();
  if (t === undefined) {
    return true; // important : true ! let open dialog
  }
  //console.log(t);
  var now = new Date();
  date.setHours(t.substr(0, 2));
  date.setMinutes(t.substr(3, 2));
  var last = now.getTime() + maxDelay * 86400000;
  if (date.getTime() > last) {
    return false;
  }
  return true;
}

function isInactive() {
  // important : true ! let open dialog
  return $("#startTime").val() === undefined;
}

/**
 * Auto-select end time.
 * @param {DOMelement} element start time element
 * @param {Object} steps dimensions rules
 * @returns {undefined}
 */
function setEndIndex(element, steps) {
  var maxIndex = $(element).children("option").length;
  var startIndex = $(element).children("option:selected").index();
  //var value = $("#booking #spinner").slider("value"); // spinner replaced by plain select component
  var value = $("#booking #timelength").val();
  var endIndex = startIndex + (value * 60 * (steps.max * 2) / (steps.max * 60));
  if (endIndex > maxIndex) {
    endIndex = maxIndex;
  }
  $("#endTime option").eq(endIndex).prop("selected", true);
}

function getStartTimeIndex(height, yPos) {
  var rows = $("#endTime").children("option").length;
  return Math.round(yPos * rows / height) -1;
}

/**
 * Ajax call to retrieve the list of groups the person belong to.
 * @param {Object} params group parameters object
 * @returns {undefined}
 */
function getGroups(params) {
  var type = $('#bookingType input[type="radio"]:checked').val();

  if (type != params.groupType) {
    $("#passInfo").show();
    return;
  }

  var urlPath = $("#booking-form #xgroups-url").val();
  $.get(urlPath, function (data) {
    if (typeof data === 'undefined' || !data.length) {
      //console.log("Aucun résultat");
      $("#passInfo").show();
      $("#member").prop("checked", true);
      $("<p id=\"groupInfo\" class=\"error\" style=\"font-size: smaller\">"+params.groupWarning+"</p>").appendTo("#groupPanel");
    } else {
      $("<p id=\"groupInfo\">").appendTo("#groupPanel");
      $("<label style=\"padding-right: 0.25em\" for=\"bookingGroup\">"+(params.groupLabel === undefined ? "" : params.groupLabel)+"</label>").appendTo('#groupInfo');
      $("<select id=\"bookingGroup\" name=\"group\">").appendTo('#groupInfo');
      $.each(data, function (index, value) {
        $("<option value=\""+value.id+"\">"+value.name+"</otpion>").appendTo('#bookingGroup');
      });
      $("#passInfo").hide();
    }

  }, "json");
}

/**
 * Automatic selection of subscription pass.
 * Ajax call to check if the person has a valid subscription card.
 * @returns {undefined}
 */
function setPassChecked() {
  var urlPath = $("#booking-form #xpass-url").val();
   $.get(urlPath, function (data) {
     console.log(data);
    if (data) {
      $("#pass").prop("checked","checked");
    }
  }, "json");
}

/**
 * Init booking date picker.
 * @param {type} date the date to set
 * @returns {undefined}
 */
function initBookingDate(date) {
  var bookDatePicker = $("#bookdate");
  bookDatePicker.datepicker({
    changeMonth: true,
    changeYear: true,
    //showOn: "button",
    //buttonImage: "images/calendar.gif",
    //buttonImageOnly: true,
    //buttonText: "Select date"
  });

  bookDatePicker.datepicker('setDate', date);
  bookDatePicker.blur();
}
