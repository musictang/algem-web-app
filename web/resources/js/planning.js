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
//  clickToBook();
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
    autoOpen: false
  });
}

function clickToBook(auth) {
  $(".schedule_col").click(function (e) {
    //var posX = $(this).offset().left;
    var posY = $(this).offset().top;
    //console.log((e.pageX - posX) + ' , ' + (e.pageY - posY));
    var target = e.target || e.srcElement;
    if ("schedule_col" === target.className) {
      var room = $(this).find(".title_col");
      var roomId = $(this).attr("id");
      console.log(e.pageY - posY, $(this).attr("id"), room.text());
      $("#booking").dialog("open");
      $("#booking #room").val(roomId);
      $("#booking #spinner").spinner({
        min: 1,
        max: 8,
        step: 0.5,
        numberFormat: "n"
      });
      $('#booking-form input[type=radio]').change(function() {
          console.log(this.value);
          getGroups($("#booking-form"));
      });

    }
    $("#booking-form input[type='submit']").click(function () {
    console.log("click submit button");

  });

  });
}

function getGroups(form) {
//    form.find(".error").hide();
    var urlPath = $("#booking-form #ajax-url").val();
    console.log(urlPath);
//    var typeData = {};
//		typeData["type"] = $('#bookingType input[type="radio"]:checked').val();
//    console.log(typeData);
//    var dataSend = JSON.stringify(typeData);
//    console.log(dataSend);
    $.get(urlPath, function(data) {
        console.log( "Data Loaded: " + data );
      }, "json");
  }

function initBookingDate(date) {
  var bookDatePicker = $("#bookdate");
  bookDatePicker.datepicker({changeMonth: true, changeYear: true, showOn: "button"});
  bookDatePicker.datepicker('setDate', date);
  bookDatePicker.blur();
}
