/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @since 09/05/15 09:33
 * @version 1.1.0
 * @returns {void}
 */

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
  setHoverStyle();
  setDialog();
  setBookingDialog();
}

/**
 * Init navigation elements.
 * @param {Number} estabId establishment id
 * @param {String} date request parameter date
 * @returns {undefined}
 */
function setDatePicker(estabId, date) {

  var picker = $("#datepicker");
  var estabSelect = $("#estabSelection");
  //picker.datepicker({ appendText: "(jj-mm-yyyy)", changeMonth: true, changeYear: true, autoSize: true })
  picker.datepicker({changeMonth: true, changeYear: true});
  picker.datepicker('setDate', date);
  picker.datepicker("refresh");
  $(estabSelect).val(estabId);
  document.title = 'Planning ' + $(estabSelect).children("option:selected").text();
  picker.change(function () {
    console.log(this.value);
    window.location = 'daily.html?d=' + this.value + '&e=' + estabId;
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
  var emSize = parseFloat($(canvas).css("font-size"));
  var leftMarginGrid = (2.2 * emSize);
  var windowWidth = $(window).width();
  var actualWidth = parseInt($('.schedule_col').css('width'));
  var canvasWidth = (cols * actualWidth) + leftMarginGrid;
  if (canvasWidth > windowWidth) {
    var gridWidth = canvasWidth + leftMarginGrid;
    $("#grid").css({
      width: gridWidth + "px"
    });
    $(canvas).css({
      width: canvasWidth + "px"
    });
  }
}

/**
 * Set the style of hovered schedules.
 * @returns {undefined}
 */
function setHoverStyle() {
  $('div.labels').hover(
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
 * @param {Object} height and step rules
 * @param {Number} bookingDelay booking delay (in hours)
 * @returns {undefined}
 */
function setBooking(params, steps) {
  //TODO detect valid date current date >= now + delay;

  var date = new Date($("#datepicker").datepicker('getDate'));
  $(".schedule_col").click(function (e) {
    //var posX = $(this).offset().left;
    //console.log((e.pageX - posX) + ' , ' + (e.pageY - posY));
    var target = e.target || e.srcElement;
    if ("schedule_col" === target.className) {
      var room = $(this).children(".title_col");
      var roomId = $(this).attr("id");
      var posY = $(this).offset().top;
      var idx = getStartTimeIndex(steps.height, e.pageY - posY);
      console.log("index start time" + idx);
      $("#startTime option").eq(idx).prop("selected", true);
      console.log("check booking");
      if (!checkBookingDelay(date, params.minDelay)) {
        console.log("Hors delai");
        $("#errorDialog").html("<p>" +params.bookingMinDelayWarning +"</p>");
        $("#errorDialog").dialog("open");
        return;
      }
       if (!checkBookingDate(date, params.maxDelay)) {
        console.log("Hors limite");
        $("#errorDialog").html("<p>" +params.bookingMaxDelayWarning +"</p>");
        $("#errorDialog").dialog("open");
        return;
      }

      console.log("position : " + (e.pageY - posY), $(this).attr("id"), room.text());
      $("#groupInfo").remove();
      $("#member").prop("checked", true);//SET MEMBER BY DEFAULT
      //*******OPEN DIALOG********
      $("#booking").dialog("open");
      //**************************

      $("#room").val(roomId);
      $("#booking #spinner").spinner({
        min: 1,
        max: steps.max,
        step: 0.5,
        numberFormat: "n",
        spin : function(event,ui) {
          //Gives Previous value
          console.log($(this).val());
          //Gives current value
          var startIndex = $("#startTime option:selected").index();
          var endIndex = startIndex + (ui.value * 60 * (steps.max * 2) / (steps.max * 60));
          $("#endTime option").eq(endIndex).prop("selected", true);
        }
      });

      setEndIndex($("#startTime"), steps);
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
    // IMPORTANT!: Firefox will not act properly without this:
//    $(this).blur();
  });
  // call ajax method
  $('#booking-form input[type=radio]').change(function () {
    $("#groupInfo").remove();
    getGroups(params);
  });

  $("#booking-form input[type='submit']").click(function () {
    console.log("click submit button");
  });

}

function checkBookingDelay(date, minDelay) {
  var t = $("#startTime").val();
  if (t === undefined) {
    return true; // important : true ! let open dialog
  }
  var now = new Date();
  console.log(t);
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
  console.log(t);
  var now = new Date();
  date.setHours(t.substr(0, 2));
  date.setMinutes(t.substr(3, 2));
  var last = now.getTime() + maxDelay * 86400000;
  if (date.getTime() > last) {
    return false;
  }
  return true;
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
  var value = $("#booking #spinner").spinner("value");
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

  var urlPath = $("#booking-form #ajax-url").val();
  $.get(urlPath, function (data) {
    if (typeof data === 'undefined' || !data.length) {
      console.log("Aucun résultat");
      $("#passInfo").show();
      $("#member").prop("checked", true);
      $("<p id=\"groupInfo\" class=\"error\" style=\"font-size: smaller\">"+params.groupWarning+"</p>").appendTo("#groupPanel");
    } else {
      $("<p id=\"groupInfo\">").appendTo("#groupPanel");
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
 * Init booking date picker.
 * @param {type} date the date to set
 * @returns {undefined}
 */
function initBookingDate(date) {
  var bookDatePicker = $("#bookdate");
  bookDatePicker.datepicker({
    changeMonth: true,
    changeYear: true,
    showOn: "button",
//    buttonImage: "images/calendar.gif",
//    buttonImageOnly: true,
//    buttonText: "Select date"
  });

  bookDatePicker.datepicker('setDate', date);
  bookDatePicker.blur();
}
