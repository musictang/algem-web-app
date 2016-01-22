/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @since 09/05/15 09:33
 * @version 1.0.6
 * @returns {void}
 */

setUI = function () {
  setWidth();
  setHoverStyle();
  setDialog();
  setBookingDialog();
};

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

function setBookingDialog() {
  $("#booking").dialog({
    modal: false,
    autoOpen: false,
    "height": "auto",
    autoResize: true
  });
}

function setBooking(groupType, groupLabel, bookingGroupWarning) {
  $(".schedule_col").click(function (e) {
    //var posX = $(this).offset().left;
    var posY = $(this).offset().top;
    //console.log((e.pageX - posX) + ' , ' + (e.pageY - posY));
    var target = e.target || e.srcElement;
    if ("schedule_col" === target.className) {
      var room = $(this).find(".title_col");
      var roomId = $(this).attr("id");
      $("#booking-form #endTime option").eq($("#booking-form #startTime option:selected").index() + 2).prop("selected", true);
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
    }
  });

  //synchronise start time and endtime on modification
  $("#booking-form #startTime").change(function() {
      var startIndex = $(this).find("option:selected").index();
      var value = $("#booking #spinner").spinner("value");
      var endIndex = startIndex + (value * 60 * 16 / 480);
      $("#booking-form #endTime option").eq(endIndex).prop("selected", true);
  });
  // call ajax method
  $('#booking-form input[type=radio]').change(function () {
    $("#groupPanel #groupInfo").remove();
    console.log(this.value);
    getGroups(groupType, groupLabel, bookingGroupWarning);

  });
  $("#booking-form input[type='submit']").click(function () {
    console.log("click submit button");
  });

}

function getGroups(groupType, groupLabel, bookingGroupWarning) {
//    form.find(".error").hide();
  var type = $('#bookingType input[type="radio"]:checked').val();
  console.log("selected type " + type);
  console.log("group type " + groupType);

  if (type != groupType) {
    return;
  }

  var urlPath = $("#booking-form #ajax-url").val();
  console.log(urlPath);
  $.get(urlPath, function (data) {
    console.log(data);
    if (typeof data === 'undefined' || !data.length) {
      console.log("Aucun résultat");
      $("#booking-form #member").prop("checked", true);
      $("<p id=\"groupInfo\" class=\"error\" style=\"font-size: smaller\">"+bookingGroupWarning+"</p>").appendTo("#groupPanel");
    } else {
      $("<p id=\"groupInfo\">").appendTo("#groupPanel");
      $("<label for=\"bookingGroup\">"+(groupLabel === undefined ? "" : groupLabel)+"</label>").appendTo('#groupInfo');
      $("<select id=\"bookingGroup\" name=\"group\">").appendTo('#groupInfo');
      $.each(data, function (index, value) {
        $("<option value=\""+value.id+"\">"+value.name+"</otpion>").appendTo('#bookingGroup');
        console.log("Data Loaded: " + value.id + " " + value.name);
      });
    }

  }, "json");
}

function initBookingDate(date) {
  var bookDatePicker = $("#bookdate");
  bookDatePicker.datepicker({changeMonth: true, changeYear: true, showOn: "button"});
  bookDatePicker.datepicker('setDate', date);
  bookDatePicker.blur();
}
