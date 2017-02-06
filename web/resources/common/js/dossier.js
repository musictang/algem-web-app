/*
 * @(#) dossier.js Algem Web App 1.6.0 03/02/17
 *
 * Copyright (c) 2015-2017 Musiques Tangentes. All Rights Reserved.
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
var DOSSIER = DOSSIER || {};
var labels = {};
var paths = {};
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
 * @param {String} text content
 * @param {String} note score
 * @param {Boolean} status absence status
 * @param {Boolean} co collective
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

function setDefaultVars (_labels, _path) {
  labels = _labels;
  defPhotoPath = _path;
}

function getFollowUpSchedules(urlPath, user, dateFrom, dateTo) {
  $.get(urlPath, {userId: user, from: dateFrom, to: dateTo}, function (data) {
    if (typeof data === 'undefined' || !data.length) {
      console.log("no data");
      $("#follow-up-result tbody").empty(); // supprimer contenu
    } else {
      var result = "";
      var total = 0;
      //var indTitle = labels.individual_monitoring_action;
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
        var courseInfo = value.ranges[0] ? value.detail['course'].name : "PAUSE";
        //var firstNameName = "";
        var noteCo = value.followUp.content || "";
        // do not include breaks in total
        if (value.collective || (value.ranges[0] && value.ranges[0].person.id > 0)) {
          total += length;
        }
        result += "<tr id=\"" + value.id + "\" class=\"" + (value.collective ? "co" : "notco") +"\">"// scheduleId
          + "<td>" + dateInfo + "</td>"
          + "<td>" + timeInfo + "</td>"
          + "<td>" + getTimeFromMinutes(length) + "</td>"
          + "<td>" + roomInfo + "</td>"
          + "<td title=\""+ courseInfo + "\">"
          + (value.collective ? getMailtoLinkFromRanges(value.ranges, labels.mailto_all_participants_tip, courseInfo) : courseInfo)
          + fillTeacherDocumentPanel(value, labels)
          + "</td><td style=\"min-width: 8em\">";
        if (value.collective) {
          result += "<a href=\"javascript:;\" class=\"expand\" title=\""+labels.expand_collapse+"\"><i>"+labels.student_list+"&nbsp;...</i></a><ul class=\"simple\">";
        } else {
          result += "<ul class=\"simple\">";
        }
        result += fillRanges(value, paths, labels);
        result += "</ul>";

        result += "</td><td id=\"" + value.note + "\" class=\"dlg\" accessKey=\"C\" title=\"" + coTitle + "\"><p>" + $('<div />').text(noteCo).html() + "</p><p class=\"subContent\">" + getFollowUpSubContent(value.followUp) + "</p></td></tr>\n";
      });
      result += "<tr><th colspan=\"2\">Total</th><td colspan=\"5\"><b>" + getTimeFromMinutes(total) + "</b></td></tr>";
      $("#follow-up-result tbody").html(result);
    }
  }, "json");
}

/**
 *
 * @param {Object} value json data
 * @param {Object} paths default locations
 * @param {Object} labels common labels
 * @returns {String} full content html li element
 */
function fillRanges(value, paths, labels) {
  var line = "";
  var indTitle = labels.individual_monitoring_action;
  for (var i = 0, len = value.ranges.length; i < len; i++) {
    var firstNameName = value.ranges[i].person.firstName + " " + value.ranges[i].person.name;
    var nc = value.ranges[i].followUp.content || "";
    var sub = getFollowUpSubContent(value.ranges[i].followUp);
    var photo = value.ranges[i].person.photo;
    line += "<li id=\"" + value.ranges[i].id + "\"><div class=\"monitoring-element\">";// scheduleRange Id

    if (photo != null) {
      line += "<img data-algem-id=\"" + value.ranges[i].followUp.id + "\" class=\"photo-id-thumbnail dlg\" src=\"data:image/jpg;base64," + photo + "\" title=\"" + indTitle + "\"/>";
    } else {
      line += "<img data-algem-id=\"" + value.ranges[i].followUp.id + "\" class=\"photo-id-thumbnail dlg\" src=\"" + paths["def_photo_id"] + "\" />";
    }
    // RANGE ID AND NAME
    line += "<a id=\"" + value.ranges[i].followUp.id + "\" href=\"javascript:;\" class=\"dlg\" title=\"" + indTitle + "\" accessKey=\"D\">" + firstNameName + "</a>";
    //CONTACT INFO
    var emails = value.ranges[i].person.emails;
    var tels = value.ranges[i].person.tels;
    var email = emails.length > 0 ? "<a href=\"mailto:" + emails[0].email + "\" title=\"" + labels.send_email_label + "\">" + emails[0].email + "</a> " : "";
    var tel = tels.length > 0 ? "<a href=\"tel:" + tels[0].number + "\" title=\"" + labels.call_label + "\">" + tels[0].number + "</a>" : "";
    line += email + tel;
    // CONTENT
    line += "<p class=\"follow-up-content\">" + $('<div />').text(nc).html() + "</p>"; // encode entities
    line += "<p class=\"subContent\">" + sub + "</p>";
    line += "</div></li>";
  }
  return line;
}

function fillTeacherDocumentPanel(schedule, labels) {
//  console.log(schedule.idAction);
//  console.log(schedule.documents.length);
  if (schedule.documents) {
    var p = "<div id=\"doc-icon-panel\" style=\"margin-top: 0.5em\">";
    var sDate = new Date(schedule.date);
    // adjust with timezone offset
    sDate.setTime(sDate.getTime() + new Date().getTimezoneOffset() * 60 * 1000);
    for (var i = 0, len = schedule.documents.length; i < len; i++) {
      var doc = schedule.documents[i];
      var dDate = new Date(doc.firstDate);
//      dDate.setHours(24);
      console.log("schedule",sDate);
      console.log("doc",dDate);
      if (sDate.getTime() < dDate.getTime()) {continue;}
      var refTag = "<img data-actionRef=\""+ schedule.idAction +"\" id=\"doc" + doc.id + "\" class=\"img-link doc-ref\" title=\"" + getDocTypeFromNumber(doc.docType, labels) + " : " + doc.name + "\" alt=\"" + doc.name + "\" src=\"../resources/common/img/" + getIconFromDocType(doc.docType) + "\" />";
      if (doc.scheduleId === 0 && doc.memberId === 0) {
        p += refTag;
      } else if (doc.scheduleId > 0) {
        if (schedule.id === doc.scheduleId) {
          p += refTag;
        }
      } else {
        for (var j = 0, rlen = schedule.ranges.length; j < rlen; j++) {
          if (schedule.ranges[j].memberId === doc.memberId) {
            p += refTag;
          }
        }
      }
    }
  }

  p += "<a id=\"doc0\" data-actionRef=\""+ schedule.idAction +"\" class=\"img-link doc-ref doc-plus\" title=\"" + labels["document_add_label"] + "\" href=\"javascript:;\"><img alt=\"" + labels["document_add_label"] + "\" src=\"../resources/common/img/plus.png\" /></a>";
  p += "</div>";
  return p;
}

function fillStudentDocumentPanel(schedule, userId, labels) {
  if (!schedule.documents) {
    return "";
  }
  var panel = "<div id=\"doc-icon-panel\" style=\"margin-top: 0.5em\">";
  var sDate = new Date(schedule.date);
  // adjust with timezone offset
  sDate.setTime(sDate.getTime() + new Date().getTimezoneOffset() * 60 * 1000);
  for (var i = 0, len = schedule.documents.length; i < len; i++) {
    var doc = schedule.documents[i];
    var dDate = new Date(doc.firstDate);
    dDate.setHours(13);
    console.log("schedule", sDate);
    console.log("doc", dDate);
    if (sDate.getTime() < dDate.getTime()) {
      continue;
    }
    var imgTag = "<img class=\"doc-link \" title=\"" + getDocTypeFromNumber(doc.docType, labels) + "\" alt=\"" + getDocTypeFromNumber(doc.docType, labels) + "\" src=\"../resources/common/img/" + getIconFromDocType(doc.docType) + "\" />";
    var docTag = "<a href=\"" + doc.uri + "\" target=\"_blank\">" + (doc.name || "Document") + "</a>";
    if (doc.scheduleId === 0 && doc.memberId === 0) {
      console.log("doc action")
      panel += "<p>" + imgTag + docTag + "</p>";
    } else if (doc.scheduleId > 0) {
      if (schedule.id === doc.scheduleId) {
        console.log("doc planning")
        panel += "<p>" + imgTag + docTag + "</p>";
      }
    } else {
      for (var j = 0, rlen = schedule.ranges.length; j < rlen; j++) {
        console.log("doc eleve")
        if (userId === doc.memberId) {
          panel += "<p>" + imgTag + docTag + "</p>";
        }
      }
    }
  }
  return panel;
}


function getDocTypeFromNumber(n, labels) {
  switch(n) {
    case 0: return labels["document_type_other_label"];
    case 1: return labels["document_type_music_sheet_label"];
    case 2: return labels["document_type_music_label"];
    case 3: return labels["document_type_video_label"];
  }
}

function getIconFromDocType(n) {
  switch(n) {
    case 0: return "share.png";
    case 1: return "music.png";
    case 2: return "audio.png";
    case 3: return "movie.png";
  }
}

function setDocumentDialog(element) {
  var dlg = $("#docEditor");
  var docId = $(element).attr("id");
  var actionRef = $(element).attr("data-actionRef");
  var scheduleRef = $(element).closest("tr").attr("id");
  if ("doc0" == docId) {
    $("#docId").val(0);
    $("#docActionId").val(actionRef);
    $("#docScheduleId").val(0);
    $("#docMemberId").val(0);
    console.log("création", docId,actionRef,scheduleRef)
  }
  else {
    console.log("modification", docId,actionRef,scheduleRef);
    // fill from database
  }
  $(dlg).dialog("open");
}

function getMailtoLinkFromRanges(ranges, title, label) {
  var mailto = null;
  var emails = [];
  for (var i = 0, len = ranges.length; i < len; i++) {
    var e = getFirstMailFromRange(ranges[i]);
    if (e != null) {
      emails.push(e);
    }
  }
  if (emails.length == 0) {
    return "";
  }
  mailto = "<a href=\"mailto:" + emails[0] + (emails.length > 1 ? "?bcc=" : "");
  for (var i = 1, len = emails.length; i < len; i++) {
    mailto += emails[i] + ","; // comma delimiter
  }
  mailto += "\" title=\""+title+"\"><i class=\"fa fa-envelope\"></i>&nbsp;"+label+"</a>";
  return mailto;
}

function getFirstMailFromRange(range) {
  var emails = range.person.emails;
  if (emails.length > 0) {
    return emails[0].email;
  }
  return null;
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
      //console.log(supportLocales)
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
          + "<td>" + courseInfo + fillStudentDocumentPanel(value, userId, labels) + "</td>"
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
 * @param {Object} element node clicked
 * @param {Boolean} co collective
 * @returns {FollowUpObject}
 */
function getAndFillFollowUp(url, element, co) {
  //console.log("image " + element.is('img'));
  var id = 0;
  if (element.is("img")) {
    id = $(element).attr("data-algem-id");
  } else {
    id = $(element).attr("id");
  }
  if (!co) {
      var parent = $(element).closest("li");// <li> element
      $("#follow-up-photo").attr("src", $(parent).find("img").attr("src"));
  } else {
    $("#follow-up-photo").attr("src", paths["def_photo_co"]);
  }
  if (id === 0) {
    return;
  }
  $.get(url, {id: id}, function (data) {
    if (typeof data === 'undefined' || data === null) {
      console.log("no data");
    } else {
      var up = new FollowUpObject(data.id, 0, data.content, data.note, data.status, co);
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
  var abs = (followUp.status === 1);
  var exc = (followUp.status === 2);
  var sub = (followUp.id > 0 && note !== null && note.length > 0) ? "<span class=\"follow-up-note\">"+labels.score_label +" : " + note + "</span>" : "";
  sub += abs ? "<span class=\"absent\">ABS</span>" : "";
  sub += exc ? "<span class=\"excused\">EXC</span>" : "";
  //console.log("sub = " + sub)
  return sub || "";
}

/**
 * Inits followUp dialog editing.
 * @param {type} element
 * @param {Object} labels
 * @returns {undefined}
 */
function initFollowUpDialog(element, labels) {
  $(element).dialog({
    modal: false,
    autoOpen: false,
    maxWidth: 320,
    position: { my: "top", at: "top", of: window },
    buttons: [
      {
        text: labels.abort_label,
        class: "button-secondary",
        click: function () {
          $(this).dialog("close");
        }
      },
      {
        text: labels.save_label,
        click: function () {
          updateFollowUp($("#follow-up-form"));
        }
      }
    ]
  });
}

function initErrorDialog() {
  $("#errorDialog").dialog({
    autoOpen: false,
    buttons: {
      Fermer: function () {
        $(this).dialog("close");
      }
    }
  });
}

function initDocumentDialog() {
  $("#docEditor").dialog({
    autoOpen: false,
    title: "Document",
    buttons: [
      {
        text: labels.abort_label,
        class: "button-secondary",
        click: function () {
          $(this).dialog("close");
        }
      },
      {
        text: labels.save_label,
        click: function () {
          console.log("save");
        }
      }
    ]
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

function resetFollowUpDialog(defPhoto) {
  $("#follow-up-photo").attr("src", defPhoto);
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
      //console.log(up);
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
    $("#" + followUp.scheduleId).each(function () {
      var el = $(this).children("td").last();
      $(el).find("p").first().text(followUp.content);
      $(el).find(".subContent").html(subContent);
      if (operation == 2) {// creation
        $(el).attr("id", followUp.id);
      } else if (operation == 0) {//suppression
        $(el).attr("id", 0);
      }
    });
  } else {
    if (operation == 2) {
      //console.log("creation " + creation)
      var ref = $("#" + followUp.scheduleId); // <li> element
      $(ref).find("p.follow-up-content").text(followUp.content);
      $(ref).find("p.subContent").html(subContent);
      // refresh follow-up id
      $(ref).find("a.dlg").attr("id", followUp.id);
      $(ref).find("img.dlg").attr("data-algem-id", followUp.id);
    } else {
      var parent = $("#" + id).closest("li");// <li> element
      var p1 = $(parent).find('p.follow-up-content');
      $(p1).text(followUp.content);
      $(parent).find("p.subContent").html(subContent);
      if (operation == 0) {//suppression
        $("#" + id).attr("id",0);
        parent.find("img.dlg").attr("data-algem-id", 0);
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
    //console.log(this.value);
    var d = new Date(studentFrom.datepicker('getDate'));
    var wd = getCurrentWeekDates(d);
    getFollowUpStudent(url, idper, dateFormatFR(wd.first), dateFormatFR(wd.last));
    studentFrom.datepicker('setDate', wd.first);
    studentTo.datepicker('setDate', wd.last);
    studentFrom.blur();
  });

  studentTo.change(function () {
    //console.log(this.value);
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
