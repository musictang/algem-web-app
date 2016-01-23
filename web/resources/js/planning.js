/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @since 09/05/15 09:33
 * @version 1.0.6
 * @returns {void}
 */

/**
 * Group constructor.
 * @param {type} id group Id
 * @param {type} name group name
 * @returns {Group}
 */
function Group(id, name) {
  this.id = id;
  this.name = name;
}

function logVars(commonParams, groupParams) {
  $.each(commonParams, function(key, value) {
    console.log(key, value);
  });
  $.each(groupParams, function(key, value) {
    console.log(key, value);
  });
}

setUI = function () {
  setWidth();
  setHoverStyle();
  setDialog();
  setBookingDialog();
};

/**
 * Init navigation elements.
 * @param {type} estabId establishment id
 * @param {type} date request parameter date
 * @returns {undefined}
 */
function setDatePicker(estabId, date) {

  var picker = $("#datepicker");
  //picker.datepicker({ appendText: "(jj-mm-yyyy)", changeMonth: true, changeYear: true, autoSize: true })
  picker.datepicker({changeMonth: true, changeYear: true});
  picker.datepicker('setDate', date);
  picker.datepicker("refresh");
  $('#estabSelection').val(estabId);
  document.title = 'Planning ' + $('#estabSelection option:selected').text();
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

  $('#estabSelection').change(function () {
    var eId = $('#estabSelection option:selected').val();
    window.location = 'daily.html?d=' + $("#datepicker").val() + '&e=' + eId;
  });

}

/**
 * Auto-resize the canvas if its width is larger than window width.
 * @returns {undefined}
 */
function setWidth() {
  var cols = $('.schedule_col').length;
  //var cols = 15;
  //console.log( "nombre de colonnes : " + cols );
  var emSize = parseFloat($("#canvas").css("font-size"));
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
    $("#canvas").css({
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
  $("#dialog").dialog({
    modal: false,
    autoOpen: false
  });

  $('div.labels').click(function () {
    //var regex = /<br\s*[\/]?>/gi;
    //var text = $(this).html().replace(regex,'\n');
    $("#dialog").html($(this).html());
    $("#dialog").dialog("open");
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
    width: 310,
    height: 410,
    autoResize: true
  });
}

/**
 * Set booking actions.
 * @param {type} groupParams
 * @returns {undefined}
 */
function setBooking(groupParams) {
  //TODO detect valid date current date >= now + delay;
  //TODO set start time by click position
  $(".schedule_col").click(function (e) {
    //var posX = $(this).offset().left;
    var posY = $(this).offset().top;
    //console.log((e.pageX - posX) + ' , ' + (e.pageY - posY));
    var target = e.target || e.srcElement;
    if ("schedule_col" === target.className) {
      var room = $(this).find(".title_col");
      var roomId = $(this).attr("id");
      console.log(e.pageY - posY, $(this).attr("id"), room.text());
      $("#groupPanel #groupInfo").remove();
      $("#booking-form #member").prop("checked", true);
      $("#booking").dialog("open");
      $("#booking #room").val(roomId);
      $("#booking #spinner").spinner({
        min: 1,
        max: 8,
        step: 0.5,
        numberFormat: "n",
        spin : function(event,ui){
        //Gives Previous value
        console.log($(this).val());
        //Gives current value
        console.log(ui.value);
        var startIndex = $("#booking-form #startTime option:selected").index();
        var endIndex = startIndex + (ui.value * 60 * 16 / 480);
         $("#booking-form #endTime option").eq(endIndex).prop("selected", true);
        }
      });
      setEndIndex($("#booking-form #startTime"));
    }
  });

  //synchronise start time and endtime on modification
  $("#booking-form #startTime").change(function() {
    setEndIndex($(this));
  });
  // call ajax method
  $('#booking-form input[type=radio]').change(function () {
    $("#groupPanel #groupInfo").remove();
    console.log(this.value);
    getGroups(groupParams);
  });
  $("#booking-form input[type='submit']").click(function () {
    console.log("click submit button");
  });

}

/**
 * Auto-select end time.
 * @param {type} element start time element
 * @returns {undefined}
 */
function setEndIndex(element) {
  var maxIndex = $(element).children("option").length;
  console.log("count :" + maxIndex);
  var startIndex = $(element).children("option:selected").index();
  var value = $("#booking #spinner").spinner("value");
  var endIndex = startIndex + (value * 60 * 16 / 480);
  if (endIndex > maxIndex) {
    endIndex = maxIndex;
  }
  $("#booking-form #endTime option").eq(endIndex).prop("selected", true);
}

/**
 * Ajax call to retrieve the list of groups the person belong to.
 * @param {type} params group parameters object
 * @returns {undefined}
 */
function getGroups(params) {
  var type = $('#bookingType input[type="radio"]:checked').val();
  console.log("selected type " + type);
  console.log("group type " + params.type);

  if (type != params.type) {
    return;
  }

  var urlPath = $("#booking-form #ajax-url").val();
  console.log(urlPath);
  $.get(urlPath, function (data) {
    console.log(data);
    if (typeof data === 'undefined' || !data.length) {
      console.log("Aucun résultat");
      $("#booking-form #member").prop("checked", true);
      $("<p id=\"groupInfo\" class=\"error\" style=\"font-size: smaller\">"+params.warning+"</p>").appendTo("#groupPanel");
    } else {
      $("<p id=\"groupInfo\">").appendTo("#groupPanel");
      $("<label for=\"bookingGroup\">"+(params.label === undefined ? "" : params.label)+"</label>").appendTo('#groupInfo');
      $("<select id=\"bookingGroup\" name=\"group\">").appendTo('#groupInfo');
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
