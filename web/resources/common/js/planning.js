/*
 * @(#)planning.js	1.7.3 14/02/18
 *
 * Copyright (c) 2015-2018 Musiques Tangentes. All Rights Reserved.
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
 * @version 1.7.3
 * @returns {void}
 */
//var isMobileAndWebkit = isMobile() && 'WebkitAppearance' in document.documentElement.style;

var GEMUTILS = GEMUTILS;
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

function setUI(commonParams) {
  setWidth();
  setScroll();
  setResize();
  setHoverStyle(commonParams);
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
  picker.datepicker({changeMonth: true, changeYear: true, dateFormat: "dd-mm-yy"});
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

function weeklyDatePicker(date, idper) {
  var from = $("#pickerFrom");
  from.datepicker({changeMonth: true, changeYear: true, dateFormat: "dd-mm-yy"});
  from.datepicker('setDate', date);
  from.datepicker("refresh");

  from.change(function () {
    //console.log(this.value);
    var d = new Date(from.datepicker('getDate'));
    var first = new Date(d.setDate(d.getDate() - d.getDay() + 2));// +2 pour lundi
    var arg = GEMUTILS.dateFormatFR(first);
    //console.log(arg);
    window.location = 'weekly.html?d=' + arg + '&id=' + idper;
  });

  var to = $("#pickerTo");
  var d2 = new Date(from.datepicker('getDate'));
  var last = new Date(d2.setDate(d2.getDate() + d2.getDay() + 6));
  to.datepicker({changeMonth: true, changeYear: true, dateFormat: "dd-mm-yy"});
  to.datepicker('setDate', GEMUTILS.dateFormatFR(last));
  to.datepicker("refresh");

  to.change(function () {
    var d = new Date(to.datepicker('getDate'));
    var first = new Date(d.setDate(d.getDate() - d.getDay() +2));// +2 pour lundi
    //console.log(first);
    var arg = GEMUTILS.dateFormatFR(first);
    //console.log(arg);
    window.location = 'weekly.html?d=' + arg + '&id=' + idper;
  });
}

function logDate(d) {
  if (!d instanceof Date) {
    console.log("erreur date");
  }
  console.log("UTC =" + d.toUTCString()
    + ", GMT =" + d.toGMTString()
    + ", ISO =" + d.toISOString()
    + ", FR =" + d.toLocaleDateString("fr")
    );
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
 * @param {Object} commonParams
 * @returns {undefined}
 */
function setHoverStyle(commonParams) {
  $('div.labels,div.schedule').hover(
    function () {
      if (commonParams.hasDetailAccess) {
        $(this).css({cursor: "pointer"});
      } else {
        $(this).css({cursor: "not-allowed"});
      }
      $(this).css({
//        'box-shadow': 'inset 0px 0px 1px 1px rgba(0,0,0,1)',
        opacity: "0.8"
      });
    },
    function () {
      $(this).css({
        cursor: "default",
//        'box-shadow': 'none',
        opacity: "1"
      });
    }
  );
  $('div.schedule_col').hover(
    function () {
      $(this).css({
        cursor: "pointer"
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
    maxHeight: 440,
    position: { my: "top", at: "top", of: window }
  });
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
    var currentDate = $("#bookdate").datepicker("getDate");
    //console.log("currentDate" + currentDate);
    if (checkBookingDelay(currentDate, params.minDelay)) {
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
      $("<div id=\"groupInfo\" class=\"error\" style=\"font-size: smaller\">"+params.groupWarning+"</div>").appendTo("#groupPanel");
    } else {
      $("<div id=\"groupInfo\">").appendTo("#groupPanel");
      $("<label for=\"bookingGroup\">"+(params.groupLabel === undefined ? "" : params.groupLabel)+"</label>").appendTo('#groupInfo');
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
     //console.log(data);
    if (data) {
      $("#pass").prop("checked","checked");
    }
  }, "json");
}

/**
 * Init booking date picker.
 * @param {date} date the date to set
 * @param {Object} params booking parameters
 * @returns {undefined}
 */
function initBookingDate(date, params) {
  var bookDatePicker = $("#bookdate");
  bookDatePicker.datepicker({
    changeMonth: true,
    changeYear: true,
    dateFormat: "dd-mm-yy",
    minDate:0,
    maxDate:params.maxDelay
    //showOn: "button",
    //buttonImage: "images/calendar.gif",
    //buttonImageOnly: true,
    //buttonText: "Select date"
  });

  bookDatePicker.datepicker('setDate', date);
  bookDatePicker.blur();
}



/**
 * ScheduleDetail function constructor.
 * @param {Number} id schedule id
 * @param {Object} person schedule idper
 * @param {String} label schedule label
 * @param {Number} type schedule type
 * @param {boolean} collective
 * @param {String} time time label
 * @param {String} room room label
 * @returns {ScheduleDetail}
 */
function ScheduleDetail(id, person, label, type, collective, time, room) {
  this.id = id;
  this.person = person;
  this.label = label;
  this.type = type;
  this.collective = collective;
  this.time = time;
  this.room = room;
}

function displayScheduleDetail(url, detail, btLabel, breakLabel) {
  $.get(url, {id: detail.id, type: detail.type}, function (data) {
    if (typeof data === 'undefined' || !data.length) {
      console.log("empty detail");
    } else {
      var d = "";
      //console.log(detail.type);
      if (detail.type != 4 && detail.type != 10 && detail.person) {// not member nor meeting
        d += "<p><strong>" + detail.person + "</strong></p>";
      }

      d += "<table class=\"pure-table pure-table-horizontal\" ><tbody>";
      data.forEach(function (value, index) {
        if (detail.collective === "true" || detail.type != 1) {
          var instr = $.isEmptyObject(value.person.instrument.name);
          d += "<tr><td id=\"" + value.id + "\">" + value.person.firstName + " " + value.person.name + (value.person.age && value.person.age > 0 ? " (" + value.person.age + ")" : "") + "</td><td>" + (instr ? "" : value.person.instrument.name) + "</td></tr>";
        } else {
          var st = value.start.hour + ":" + (value.start.minute == 0 ? "00" : value.start.minute);
          var et = value.end.hour + ":" + (value.end.minute == 0 ? "00" : value.end.minute);
          d += "<tr><td id=\"" + value.id + "\">" + st + "-" + et + "</td><td>" + (value.memberId == 0 ? breakLabel : value.person.firstName + " " + value.person.name + (value.person.age > 0 ? " ("+ value.person.age + ")": "")) + "</td></tr>";
        }
      });
      d += "</tbody></table>"
      //console.log(d);
      $("#schedule-detail-dlg").html(d);
      $("#schedule-detail-dlg").dialog({
        title: detail.label + " " + detail.time,
        buttons: [{
            text: btLabel,
            class: "button-secondary",
            click: function () {
              $(this).dialog("close");
            }
          }
          ]
      }).dialog("open");
    }

  }, "json");
}

function initRoomDetailDialog(element, closingLabel) {
  $(element).dialog({
    modal: false,
    autoOpen: false,
    maxHeight: 440,
    position: {my: "top", at: "top", of: window},
    buttons: [{
        text: closingLabel,
        class: "button-secondary",
        click: function () {
          $(this).dialog("close");
        }
      }
    ]
  });
}

function showRoomDetail(element, url, labels) {
  var roomId = $(element).closest("div").attr("id");
  //console.log(roomId);
  $.get(url, {id: roomId}, function (data) {
    //console.log(data);
    if (typeof data === 'undefined' || data === null) {
      console.log("empty detail");
    } else {
      var content = "<h4>"+labels.function_label+"</h4><p>" + data.usage + "</p>";
      content += "<h4>"+labels.features_label+"</h4><table><tr><th>"+labels.surface_label+"</th><td>" + (data.surface > 0 ? data.surface : "NC") + "</td></tr><tr><th>"+labels.number_of_places_label+"</th><td>" + (data.places > 0 ? data.places : "NC") + "</td></tr></table>";
      content += "<h4>"+labels.prices_label +"</h4><table><tr><th>"+labels.offpeak_price_label +"</th><td>" + data.offPeakPrice + " €</td></tr><tr><th>"+labels.full_price_label +"</th><td>" + data.fullPrice + " €</td></tr></table>";
      if (data.equipment && data.equipment.length > 0) {
        content += "<h4>"+labels.equipment_label+"</h4><table><tr><th>"+labels.quantity_label+"</th><th>"+labels.name_label+"</th></tr>";
        for (var i = 0; i < data.equipment.length; i++) {
          content += "<tr><td>" + data.equipment[i].quantity + "</td><td>" + data.equipment[i].name + "</td></tr>";
        }
        content += "</table>";
      }
      $("#roomDetailDialog").html(content);
      $("#roomDetailDialog").dialog({title: "Salle " + data.name}).dialog("open");
    }
  }, "json");
}
