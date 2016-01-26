/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @since 09/05/15 09:33
 * @version 1.0.6
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
 * @param {type} estabId establishment id
 * @param {type} date request parameter date
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
  //var cols = 15;
  //console.log( "nombre de colonnes : " + cols );
  var emSize = parseFloat($(canvas).css("font-size"));
  var leftMarginGrid = (2.2 * emSize);
  var windowWidth = $(window).width();
  //console.log( "emsize : " + emSize );
  var actualWidth = parseInt($('.schedule_col').css('width'));
  //console.log( "actualWidth : " + actualWidth );
  var canvasWidth = (cols * actualWidth) + leftMarginGrid;
  //console.log( "largeur grille : " + canvasWidth + " window.width : " + windowWidth);
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
        cursor: "pointer",
        'box-shadow': 'inset 0px 0px 1px 1px rgba(0,0,0,1)',
        opacity: "0.8"
      });
    },
    function () {
      $(this).css({
        cursor: "default",
        'box-shadow': 'none',
        opacity: "1"
      });
    }
  );
}

/**
 * Init main dialog.
 * @returns {undefined}
 */
function setDialog() {
  var mainDialog = $("#dialog");
  $(mainDialog).dialog({
    modal: false,
    autoOpen: false
  });

  $('div.labels').click(function () {
    //var regex = /<br\s*[\/]?>/gi;
    //var text = $(this).html().replace(regex,'\n');
    $(mainDialog).html($(this).html());
    $(mainDialog).dialog("open");
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
    maxWidth: 310,
    maxHeight: 410
  });
}

/**
 *
 * @param {Object} params booking params
 * @param {Object} height and step rules
 * @param {Number} bookingDelay booking delay (in hours)
 * @returns {undefined}
 */
function setBooking(params, steps, bookingDelay) {
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
      console.log("idx " + idx);
      $("#startTime option").eq(idx).prop("selected", true);
      
      console.log(e.pageY - posY, $(this).attr("id"), room.text());
      $("#groupInfo").remove();
      $("#member").prop("checked", true);
      $("#booking").dialog("open");
      if (!checkBookingDelay(date, bookingDelay)) {
        console.log("Hors delai");
        $("#booking").html("<p>" +params.bookingDelayWarning +"</p>");
//        alert(params.bookingDelayWarning);
        return;
      }
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
    if (checkBookingDelay(date, bookingDelay)) {
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
    $(this).addClass("buzy");
    $("#groupInfo").remove();
    getGroups(params);
    $(this).removeClass("buzy");
  });

  $("#booking-form input[type='submit']").click(function () {
    console.log("click submit button");
  });

}

function checkBookingDelay(date, bookingDelay) {
  var t = $("#startTime").val();
  if (t === undefined) {
    return true; // important : true ! let open dialog
  }
  var now = new Date();
//  console.log("now = " + now);
//  console.log(t);
  date.setHours(t.substr(0, 2));
  date.setMinutes(t.substr(3, 2));
  //console.log(now.getTime() + (bookingDelay * 60 * 60 * 1000) + " || date.time : " + date.getTime());
  if (now.getTime() + (bookingDelay * 60 * 60 * 1000) > date.getTime()) {
    return false;
  }
  return true;
}

/**
 * Auto-select end time.
 * @param {DOMelement} element start time element
 * @param {Object} steps
 * @returns {undefined}
 */
function setEndIndex(element, steps) {
  console.log(steps);
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
 * @param {type} params group parameters object
 * @returns {undefined}
 */
function getGroups(params) {
  var type = $('#bookingType input[type="radio"]:checked').val();

  if (type != params.groupType) {
    return;
  }

  var urlPath = $("#booking-form #ajax-url").val();
  console.log(urlPath);
  $.get(urlPath, function (data) {
    console.log(data);
    if (typeof data === 'undefined' || !data.length) {
      console.log("Aucun résultat");
      $("#member").prop("checked", true);
      $("<p id=\"groupInfo\" class=\"error\" style=\"font-size: smaller\">"+params.groupWarning+"</p>").appendTo("#groupPanel");
    } else {
      $("<p id=\"groupInfo\">").appendTo("#groupPanel");
      $("<label for=\"bookingGroup\">"+(params.groupLabel === undefined ? "" : params.groupLabel)+"</label>").appendTo('#groupInfo');
      $("<select id=\"bookingGroup\" th:field=\"*{group}\">").appendTo('#groupInfo');
      $.each(data, function (index, value) {
        $("<option value=\""+value.id+"\">"+value.name+"</otpion>").appendTo('#bookingGroup');
        console.log("Data Loaded: " + value.id + " " + value.name);
      });
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
  bookDatePicker.datepicker({changeMonth: true, changeYear: true, showOn: "button"});
  bookDatePicker.datepicker('setDate', date);
  bookDatePicker.blur();
}
