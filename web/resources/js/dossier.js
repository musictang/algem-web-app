/*
 * @(#) dossier.js Algem Web App 1.4.2 31/08/2016
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
 */
var labels = {};
/**
 * FollowUpSchedule constructor.
 * @param {Number} id
 * @param {String} date
 * @param {String} time
 * @param {String} course
 * @returns {FollowUpSchedule}
 */
function FollowUpSchedule(id, date, time, course) {
  this.id = id;
  this.date = date;
  this.time = time;
  this.course = course;
}

/**
 * FollowUpObject constructor.
 * @param {Number} id
 * @param {Number} scheduleId
 * @param {String} text
 * @param {String} note
 * @param {Boolean} abs
 * @param {Boolean} exc
 * @param {Boolean} co
 * @returns {FollowUpObject}
 */
function FollowUpObject(id, scheduleId, text, note, status, co) {
  this.id = id;
  this.scheduleId = scheduleId;
  this.content = text;
  this.note = note;
  this.status = status;
  this.collective = co;
}

function setPersonalHomePageLabels (_labels) {
  labels = _labels;
}

function getFollowUpSchedules(urlPath, user, dateFrom, dateTo) {
  $.get(urlPath, {userId: user, from: dateFrom, to: dateTo}, function (data) {
    if (typeof data === 'undefined' || !data.length) {
      console.log("no data");
      $("#follow-up-result tbody").empty(); // supprimer contenu
    } else {
      var result = "";
      var total = 0;
      var indTitle = labels.individual_monitoring_action;
      var coTitle = labels.collective_monitoring_action;
      var supportLocales = toLocaleStringSupportsLocales();
      // zero-width space (&#8203;) inserted after hyphens to authorize breaks
      $.each(data, function (index, value) {
        var d = new Date(value.date);
        var dateInfo = supportLocales ? d.toLocaleString(getLocale(), {weekday: 'long'}) + " " + dateFormatFR(d) : dateFormatFR(d);
        var timeInfo = value.start.hour.pad() + ":" + value.start.minute.pad() + "-&#8203;" + value.end.hour.pad() + ":" + value.end.minute.pad();
        var ms = (value.start.hour * 60) + value.start.minute;
        var me = (value.end.hour * 60) + value.end.minute;
        var length = me - ms;
        var roomInfo = value.detail['room'].name;
        var courseInfo = value.detail['course'].name;
        var firstNameName = "";
        var noteCo = value.followUp.content || "";
        total += length;

        result += "<tr class=\"" + value.id + " " + (value.collective ? "co" : "notco") +"\">"// id planning
          + "<td>" + dateInfo + "</td>"
          + "<td>" + timeInfo + "</td>"
          + "<td>" + getTimeFromMinutes(length) + "</td>"
          + "<td>" + roomInfo + "</td>"
          + "<td>" + courseInfo + "</td><td style=\"min-width: 8em\">";
        if (value.collective) {
          result += "<a href=\"javascript:;\" class=\"expand\" title=\""+labels.expand_collapse+"\"><i>"+labels.student_list+"&nbsp;...</i></a><ul class=\"simple\">";
        } else {
          result += "<ul class=\"simple\">";
        }
        for (var i = 0, len = value.ranges.length; i < len; i++) {
          firstNameName = value.ranges[i].person.firstName + " " + value.ranges[i].person.name;
          var nc = value.ranges[i].followUp.content || "";
          var sub = getFollowUpSubContent(value.ranges[i].followUp);
          result += "<li id=\"" + value.ranges[i].id + "\"><a id=\"" + value.ranges[i].followUp.id + "\" href=\"javascript:;\" class=\"dlg\" title=\"" + indTitle + "\" accessKey=\"D\">" + firstNameName + "</a>";
          result += "<p class=\"follow-up-content\">" + $('<div />').text(nc).html() + "</p>"; // encode entities
          result += "<p class=\"subContent\">" + sub + "</p>";
          result += "</li>";
        }
        result += "</ul>";
        result += "</td><td id=\"" + value.note + "\" class=\"dlg\" accessKey=\"C\" title=\"" + coTitle + "\"><p>" + $('<div />').text(noteCo).html() + "</p><p class=\"subContent\">" + getFollowUpSubContent(value.followUp) + "</p></td></tr>\n";
      });
      result += "<tr><th colspan=\"2\">Total</th><td colspan=\"5\"><b>" + getTimeFromMinutes(total) + "</b></td></tr>";
      $("#follow-up-result tbody").html(result);
    }
  }, "json");
}

function getFollowUpStudent(urlPath, userId, dateFrom, dateTo) {
  $.get(urlPath, {userId: userId, from: dateFrom, to: dateTo}, function (data) {
    if (typeof data === 'undefined' || !data.length) {
      console.log("no data");
      $("#follow-up-student tbody").empty(); // supprimer contenu
    } else {
      var result = "";
      var total = 0;
      var supportLocales = toLocaleStringSupportsLocales();
      console.log(supportLocales)
      $.each(data, function (index, value) {
        var d = new Date(value.date);
        // XXX toLocaleString([[locale], options]) not supported on android (excepted chrome)
        var dateInfo = supportLocales ? d.toLocaleString(getLocale(), {weekday: 'long'}) + " " + dateFormatFR(d) : dateFormatFR(d);
        var timeInfo = value.start.hour.pad() + ":" + value.start.minute.pad() + "-&#8203;" + value.end.hour.pad() + ":" + value.end.minute.pad();
        var ms = (value.start.hour * 60) + value.start.minute;
        var me = (value.end.hour * 60) + value.end.minute;
        var length = me - ms;
        var roomInfo = value.detail['room'].name;
        var courseInfo = value.detail['course'].name;
        var teacherInfo = value.detail['teacher'].name;
        var noteCo = value.followUp.content || "";
        var nc = value.ranges[0].followUp.content || "";
        var sub = getFollowUpSubContent(value.ranges[0].followUp);
        var subCo = getFollowUpSubContent(value.followUp);
        total += length;

        result += "<tr>"
          + "<td>" + dateInfo + "</td>"
          + "<td>" + timeInfo + "</td>"
          + "<td>" + getTimeFromMinutes(length) + "</td>"
          + "<td>" + roomInfo + "</td>"
          + "<td>" + courseInfo + "</td>"
          + "<td>" + teacherInfo + "</td>"
          + "<td>" + nc + "<p class=\"subContent\">" + sub + "</p></td>"
          + "<td>" + noteCo + "<p class=\"subContent\">" + subCo + "</p></td></tr>";
      });
      result += "<tr><th colspan=\"2\">Total</th><td colspan=\"6\"><b>" + getTimeFromMinutes(total) + "</b></td></tr>";
      //console.log(result);
      $("#follow-up-student tbody").html(result);
    }
  }, "json");
}

/**
 * Ajax function to get some followUp.
 * @param {String} url xhttp url
 * @param {Number} id followUp id
 * @param {Boolean} co collective
 * @returns {FollowUpObject}
 */
function getFollowUp(url, id, co) {
  if (id === 0) {
    return;
  }
  $.get(url, {id: id}, function (data) {
    if (typeof data === 'undefined' || data === null) {
      console.log("no data");
    } else {
      var up = new FollowUpObject(data.id, 0, data.content, data.note, data.status, co);
//      console.log(up);
      $("#follow-content").val(up.content);
      $("#note").val(up.note);
      $("#follow-status").val(up.status);
    }
  });
}

/**
 * Returns the subContent info of the followUp in argument.
 * @param {FollowUpObject} followUp
 * @returns {String}
 */
function getFollowUpSubContent(followUp) {
  var note = followUp.note;
  var abs = (followUp.status == 1);
  var exc = (followUp.status == 2);
  var sub = (followUp.id > 0 && note !== null && note.length > 0) ? "<span class=\"follow-up-note\">NOTE : " + note + "</span>" : "";
  sub += abs ? "<span class=\"absent\">ABS</span>" : "";
  sub += exc ? "<span class=\"excused\">EXC</span>" : "";
  //console.log("sub = " + sub)
  return sub || "";
}

/**
 * Inits followUp dialog editing.
 * @param {type} element
 * @returns {undefined}
 */
function initFollowUpDialog(element) {
  $(element).dialog({
    modal: false,
    autoOpen: false,
    maxWidth: 320,
    buttons: {
      Abandonner: function () {
        $(this).dialog("close");
      },
      Enregistrer: function () {
        updateFollowUp($("#follow-up-form"));
      }
    }
  });
}

/**
 *
 * @param {FollowUpSchedule} f
 * @returns {undefined}
 */
function showDialog(f) {
  var dlg = $("#follow-up-dlg");
  $(dlg).find("legend").html(f.name + " : " + f.time);
  $(dlg).dialog({title: f.course + " " + f.date}).dialog("open");
}

function resetFollowUpDialog() {
  $("#follow-content").val('');
  $("#follow-status").val('0');
  $("#note").val('');
  $("#follow-up-dlg").find(".error").text('');
}

function updateFollowUp(form) {
  var url = $(form).attr("action");
  //update hidden field before sending
  $("#status").val($("#follow-status").val());
  $.post(url, form.serialize(), function (data) {
    if (data && data.success) {
      var content = $("#follow-content").val();
      var co = $("#noteType").val();
      var note = $("#note").prop("readonly") ? null : $("#note").val();
      var up = new FollowUpObject(data.followUp.id, data.followUp.scheduleId, content, note, data.followUp.status, co);
      console.log(up);
      refreshFollowContent(data.operation, up);
      $(form).next(".error").text('');
      $("#follow-up-dlg").dialog("close");
    } else {
      console.log("no update");
      $(form).next(".error").text(data.message);
    }
  }, "json");
}

function refreshFollowContent(operation, followUp) {
  var subContent = getFollowUpSubContent(followUp);
  var id = followUp.id;
  if (followUp.collective === 'true') {
    $("." + followUp.scheduleId).each(function () {
      var el = $(this).children("td").last();
      $(el).children("p").first().text(followUp.content);
      $(el).find(".subContent").html(subContent);
      $(el).attr("id", followUp.id);
    });

  } else {
    if (operation == 2) {
      //console.log("creation " + creation)
      var ref = $("#" + followUp.scheduleId);
      $(ref).children('p').first().text(followUp.content);
      $(ref).find(".subContent").html(subContent);
      $(ref).children('a').first().attr("id", followUp.id);
    } else {
      var parent = $("#" + id).closest("li");
      var p1 = $(parent).children('p').first();
      $(p1).text(followUp.content);
      $(parent).find(".subContent").html(subContent);
      if (operation == 0) {
        $("#" + id).attr("id",0);
      }
    }

  }
}

function initWeekDates(today) {
  var from = $("#weekFrom");
  var to = $("#weekTo");
  $(from).datepicker({changeMonth: true, changeYear: true, dateFormat: 'dd-mm-yy'}).datepicker('setDate', today);
  $(to).datepicker({changeMonth: true, changeYear: true, dateFormat: 'dd-mm-yy'}).datepicker('setDate', today);
  $(from).val(dateFormatFR(today));
  $(to).val(dateFormatFR(today));

  var studentFrom = $("#student-weekFrom");
  var studentTo = $("#student-weekTo");
  $(studentFrom).datepicker({changeMonth: true, changeYear: true, dateFormat: 'dd-mm-yy'}).datepicker('setDate', today);
  $(studentTo).datepicker({changeMonth: true, changeYear: true, dateFormat: 'dd-mm-yy'}).datepicker('setDate', today);
  $(studentFrom).val(dateFormatFR(today));
  $(studentTo).val(dateFormatFR(today));

}

function setWeekDates(firstDay, lastDay) {
  $("#weekFrom").datepicker('setDate', firstDay);
  $("#weekTo").datepicker('setDate', lastDay);
}
function setStudentWeekDates(firstDay, lastDay) {
  $("#student-weekFrom").datepicker('setDate', firstDay);
  $("#student-weekTo").datepicker('setDate', lastDay);
}
function setWeekChange(url, idper) {
  var from = $("#weekFrom");
  var to = $("#weekTo");
  from.change(function () {
    //console.log(this.value);
    var d = new Date(from.datepicker('getDate'));
    var wd = getCurrentWeekDates(d);
    getFollowUpSchedules(url, idper, dateFormatFR(wd.first), dateFormatFR(wd.last));
    from.datepicker('setDate', wd.first);
    to.datepicker('setDate', wd.last);
    from.blur();
  });

  to.change(function () {
    //console.log(this.value);
    var d = new Date(to.datepicker('getDate'));
    var wd = getCurrentWeekDates(d);
    getFollowUpSchedules(url, idper, dateFormatFR(wd.first), dateFormatFR(wd.last));
    from.datepicker('setDate', wd.first);
    to.datepicker('setDate', wd.last);
    to.blur();
  });
}

function setStudentWeekChange(url, idper) {
  var studentFrom = $("#student-weekFrom");
  var studentTo = $("#student-weekTo");
  studentFrom.change(function () {
    console.log(this.value);
    var d = new Date(studentFrom.datepicker('getDate'));
    var wd = getCurrentWeekDates(d);
    getFollowUpStudent(url, idper, dateFormatFR(wd.first), dateFormatFR(wd.last));
    studentFrom.datepicker('setDate', wd.first);
    studentTo.datepicker('setDate', wd.last);
    studentFrom.blur();
  });

  studentTo.change(function () {
    console.log(this.value);
    var d = new Date(studentTo.datepicker('getDate'));
    var wd = getCurrentWeekDates(d);
    getFollowUpStudent(url, idper, dateFormatFR(wd.first), dateFormatFR(wd.last));
    studentFrom.datepicker('setDate', wd.first);
    studentTo.datepicker('setDate', wd.last);
    studentTo.blur();
  });
}

function setTeacherDateNavigation(url, userId, weekDates) {
  $("#follow-d").click(function () {
    var now = new Date();
    getFollowUpSchedules(url, userId, dateFormatFR(now), dateFormatFR(now));
    setWeekDates(dateFormatFR(now), dateFormatFR(now));
  });
  $("#follow-m").click(function () {
    var cmd = getCurrentMonthDates();
    getFollowUpSchedules(url, userId, dateFormatFR(cmd.first), dateFormatFR(cmd.last));
    setWeekDates(dateFormatFR(cmd.first), dateFormatFR(cmd.last));
  });
  $("#follow-w").click(function () {
    getFollowUpSchedules(url, userId, dateFormatFR(weekDates.first), dateFormatFR(weekDates.last));
    setWeekDates(dateFormatFR(weekDates.first), dateFormatFR(weekDates.last));
  });
}

function setStudentDateNavigation(url, userId, weekDates) {
  $("#student-follow-d").click(function () {
    var now = new Date();
    getFollowUpStudent(url, userId, dateFormatFR(now), dateFormatFR(now));
    setStudentWeekDates(dateFormatFR(now), dateFormatFR(now));
  });
  $("#student-follow-m").click(function () {
    var cmd = getCurrentMonthDates();
    getFollowUpStudent(url, userId, dateFormatFR(cmd.first), dateFormatFR(cmd.last));
    setStudentWeekDates(dateFormatFR(cmd.first), dateFormatFR(cmd.last));
  });
  $("#student-follow-w").click(function () {
    getFollowUpStudent(url, userId, dateFormatFR(weekDates.first), dateFormatFR(weekDates.last));
    setStudentWeekDates(dateFormatFR(weekDates.first), dateFormatFR(weekDates.last));
  });
}

