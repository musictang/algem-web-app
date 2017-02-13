/*
 * @(#) dossier.js Algem Web App 1.6.0 11/02/17
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

DOSSIER.getFollowUpSchedules = function(urlPath, user, dateFrom, dateTo, labels, paths) {
  $.get(urlPath, {userId: user, from: dateFrom, to: dateTo}, function (data) {
    if (typeof data === 'undefined' || !data.length) {
      console.log("no data");
      $("#follow-up-result tbody").empty(); // supprimer contenu
    } else {
      var result = "";
      var total = 0;
      //var indTitle = labels.individual_monitoring_action;
      var coTitle = labels.collective_monitoring_action;
      var supportLocales = GEMUTILS.toLocaleStringSupportsLocales();
      // zero-width space (&#8203;) inserted after hyphens to authorize breaks
      $.each(data, function (index, value) {
        var d = new Date(value.date);
        var df = GEMUTILS.dateFormatFR(d);
        var dateInfo = supportLocales ? d.toLocaleString(GEMUTILS.getLocale(), {weekday: 'long'}) + " " + df : df;
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
          + (value.collective ? DOSSIER.getMailtoLinksFromRanges(value.ranges, labels.mailto_all_participants_tip, courseInfo) : courseInfo)
          + DOSSIER.fillTeacherDocumentPanel(value, labels)
          + "</td><td style=\"min-width: 8em\">";
        if (value.collective) {
          result += "<a href=\"javascript:;\" class=\"expand\" title=\""+labels.expand_collapse+"\"><i>"+labels.student_list+"&nbsp;...</i></a><ul class=\"simple\">";
        } else {
          result += "<ul class=\"simple\">";
        }
        result += DOSSIER.fillRanges(value, labels, paths);
        result += "</ul>";

        result += "</td><td id=\"" + value.note + "\" class=\"dlg\" accessKey=\"C\" title=\"" + coTitle + "\"><p>" + $('<div />').text(noteCo).html() + "</p><p class=\"subContent\">" + DOSSIER.getFollowUpSubContent(value.followUp, labels) + "</p></td></tr>\n";
      });
      result += "<tr><th colspan=\"2\">Total</th><td colspan=\"5\"><b>" + getTimeFromMinutes(total) + "</b></td></tr>";
      $("#follow-up-result tbody").html(result);
    }
  }, "json");
};

/**
 * Fills individual monitoring entries.
 * @param {Object} value json data
 * @param {Object} labels common labels
 * @param {Object} paths default locations
 * @returns {String} full content html li element
 */
DOSSIER.fillRanges = function(value, labels, paths) {
  var line = "";
  var indTitle = labels.individual_monitoring_action;
  for (var i = 0, len = value.ranges.length; i < len; i++) {
    var firstNameName = value.ranges[i].person.firstName + " " + value.ranges[i].person.name;
    var nc = value.ranges[i].followUp.content || "";
    var sub = this.getFollowUpSubContent(value.ranges[i].followUp, labels);
    var photo = value.ranges[i].person.photo;
    line += "<li id=\"" + value.ranges[i].id + "\" data-algem-memberid=\"" + value.ranges[i].memberId + "\"><div class=\"monitoring-element\">";// scheduleRange Id
    if (photo != null) {
      line += "<img data-algem-followupid=\"" + value.ranges[i].followUp.id + "\" class=\"photo-id-thumbnail dlg\" src=\"data:image/jpg;base64," + photo + "\" title=\"" + indTitle + "\"/>";
    } else {
      line += "<img data-algem-followupid=\"" + value.ranges[i].followUp.id + "\" class=\"photo-id-thumbnail dlg\" src=\"" + paths["def_photo_id"] + "\" />";
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
};

/**
 * Fills document entries.
 * @param {Object} schedule schedule element
 * @param {Object} labels common labels
 * @returns {String} a tag as string
 */
DOSSIER.fillTeacherDocumentPanel = function(schedule, labels) {
  if (schedule.documents) {
    var p = "<div class=\"doc-icon-panel\" style=\"margin-top: 0.5em\">";
    var sDate = new Date(schedule.date);
    // adjust with timezone offset
    sDate.setTime(sDate.getTime() + new Date().getTimezoneOffset() * 60 * 1000);
    for (var i = 0, len = schedule.documents.length; i < len; i++) {
      var doc = schedule.documents[i];
      var dDate = new Date(doc.firstDate);
      if (sDate.getTime() < dDate.getTime()) {continue;}
//      var refTag = "<img data-algem-actionref=\""+ schedule.idAction +"\" data-algem-docid=\"" + doc.id + "\" class=\"img-link doc-ref\" title=\"" + this.getDocTypeFromNumber(doc.docType, labels) + " : " + doc.name + "\" alt=\"" + doc.name + "\" src=\"../resources/common/img/" + this.getIconFromDocType(doc.docType) + "\" />";
var refTag = "<span class=\"doc-ref "+ this.getIconFromDocType(doc.docType) + "\" data-algem-actionref=\""+ schedule.idAction +"\" data-algem-docid=\"" + doc.id + "\" title=\"" + this.getDocTypeFromNumber(doc.docType, labels) + " : " + doc.name + "\"></span>";
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

  p += "<a data-algem-docid=\"0\" data-algem-actionref=\""+ schedule.idAction +"\" class=\"img-link doc-ref doc-plus\" title=\"" + labels["document_add_label"] + "\" href=\"javascript:;\"><img alt=\"" + labels["document_add_label"] + "\" src=\"../resources/common/img/plus.png\" /></a>";
  p += "</div>";
  return p;
};

DOSSIER.fillStudentDocumentPanel = function(schedule, userId, labels) {
  if (!schedule.documents) {
    return "";
  }
  var panel = "<div class=\"doc-icon-panel\" style=\"margin-top: 0.5em\">";
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
    var imgTag = "<span class=\"doc-icon-ref " + this.getIconFromDocType(doc.docType) + "\" title=\"" + this.getDocTypeFromNumber(doc.docType, labels) + "\"></span>";
    var docTag = "<a href=\"" + doc.uri + "\" target=\"_blank\">" + (doc.name || "Document") + "</a>";
    if (doc.scheduleId === 0 && doc.memberId === 0) {
      panel += "<p>" + imgTag + docTag + "</p>";
    } else if (doc.scheduleId > 0) {
      if (schedule.id === doc.scheduleId) {
        panel += "<p>" + imgTag + docTag + "</p>";
      }
    } else {
      for (var j = 0, rlen = schedule.ranges.length; j < rlen; j++) {
        if (userId === doc.memberId) {
          panel += "<p>" + imgTag + docTag + "</p>";
        }
      }
    }
  }
  return panel;
};

DOSSIER.getDocTypeFromNumber = function(n, labels) {
  switch(n) {
    case 0: return labels["document_type_other_label"];
    case 1: return labels["document_type_music_sheet_label"];
    case 2: return labels["document_type_music_label"];
    case 3: return labels["document_type_video_label"];
  }
};

DOSSIER.getIconFromDocType = function(n) {
  switch(n) {
//    case 0: return "share.png";
//    case 1: return "music.png";
//    case 2: return "audio.png";
//    case 3: return "movie.png";
    case 0: return "icon-share2";
    case 1: return "icon-file-music";
    case 2: return "icon-headphones";
    case 3: return "icon-video-camera";

  }
};

/**
 * Fills form entries before opening document dialog.
 * @param {Object} element element clicked
 * @param {String} url controller url
 * @param {Object} labels translations
 */
DOSSIER.getAndFillDocumentDialog = function(element, url, labels) {
  var row = $(element).closest("tr");
  var actionRef = $(element).attr("data-algem-actionref");
  var scheduleRef = $(row).attr("id");
  var memberRef = 0;
  if ($(row).hasClass("notco")) {
    memberRef = $(row).find("[data-algem-memberid]").attr("data-algem-memberid");
  }
  var dateRef = $(row).children("td:first-child").text();
  var scheduleDate = dateRef.split(" ").pop();
  var dlg = $("#docEditor");
  var docId = $(element).attr("data-algem-docid");
  this.resetDocumentDialog();// clear form
  if ("0" === docId) {
    //creation
    $("#docId").val(0);
    $("#docFirstDate").val(scheduleDate);
    $("#docActionId").val(actionRef);
    $("#docScheduleId").val(0);
    $("#docMemberId").val(memberRef);
  } else {
    // modification
    $("#docFirstDate").val(scheduleDate);
    // fill from database
    $.get(url, {docId: docId}, function (data) {
      if (typeof data === 'undefined') {
        console.log("no document");
      } else {
        $("#docId").val(data.id);
        var dateFr = GEMUTILS.dateFormatFR(new Date(data.firstDate));
        $("#docFirstDate").val(dateFr);
        $("#docActionId").val(data.actionId);
        $("#docScheduleId").val(data.scheduleId);
        $("#docMemberId").val(data.memberId);
        $("#docType").val(data.docType);
        $("#docName").val(data.name);
        $("#docUri").val(data.uri);
        var docActionsTag = "<div id=\"docActions\"><a id=\"docRemoveLink\" data-algem-remove-link-id=\""+data.id+"\" href=\"javascript:;\">"+labels['remove_label']+"</a></div>";
        var lnk = "<p id=\"docUriLink\"><a href=\""+data.uri+"\" target=\"_blank\">"+labels['document_access_label']+"</a></p>";
        $(docActionsTag).insertAfter($("#docUri"));
        $(lnk).insertBefore($("#docRemoveLink"));
      }
    });
  }
  $(dlg).dialog("open");
};

/**
 * Clears form entries.
 * @returns {undefined}
 */
DOSSIER.resetDocumentDialog = function() {
  $("#docId").val(0);
  $("#docFirstDate").val(null);
  $("#docActionId").val(0);
  $("#docScheduleId").val(0);
  $("#docMemberId").val(0);
  $("#docType").val(0);
  $("#docName").val(null);
  $("#docUri").val(null);
  $("#docActions").remove();
};

/**
 * Constructs a mailto link from a list of ranges.
 * @param {type} ranges
 * @param {type} title title tag content
 * @param {type} label link label
 * @returns {String} a string as link
 */
DOSSIER.getMailtoLinksFromRanges = function(ranges, title, label) {
  var getFirstMail = function (range) {
    var emails = range.person.emails;
    if (emails.length > 0) {
      return emails[0].email;
    }
    return null;
  };

  var mailto = null;
  var emails = [];
  for (var i = 0, len = ranges.length; i < len; i++) {
    var e = getFirstMail(ranges[i]);
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

DOSSIER.getFollowUpStudent = function(urlPath, userId, dateFrom, dateTo, labels) {
  $.get(urlPath, {userId: userId, from: dateFrom, to: dateTo}, function (data) {
    if (typeof data === 'undefined' || !data.length) {
      console.log("no data");
      $("#follow-up-student tbody").empty(); // supprimer contenu
    } else {
      var result = "";
      var total = 0;
      var supportLocales = GEMUTILS.toLocaleStringSupportsLocales();
      //console.log(supportLocales)
      $.each(data, function (index, value) {
        var d = new Date(value.date);
        var df = GEMUTILS.dateFormatFR(d);
        // XXX toLocaleString([[locale], options]) not supported on android (excepted chrome)
        var dateInfo = supportLocales ? d.toLocaleString(GEMUTILS.getLocale(), {weekday: 'long'}) + " " + df : df;
        var timeInfo = value.start.hour.pad() + ":" + value.start.minute.pad() + "-&#8203;" + value.end.hour.pad() + ":" + value.end.minute.pad();
        var ms = (value.start.hour * 60) + value.start.minute;
        var me = (value.end.hour * 60) + value.end.minute;
        var length = me - ms;
        var roomInfo = value.detail['room'].name;
        var courseInfo = value.detail['course'].name;
        var teacherInfo = value.detail['teacher'].name;
        var noteCo = value.followUp.content || "";
        var nc = value.ranges[0].followUp.content || "";
        var sub = DOSSIER.getFollowUpSubContent(value.ranges[0].followUp, labels);
        var subCo = DOSSIER.getFollowUpSubContent(value.followUp, labels);
        total += length;

        result += "<tr>"
          + "<td>" + dateInfo + "</td>"
          + "<td>" + timeInfo + "</td>"
          + "<td>" + getTimeFromMinutes(length) + "</td>"
          + "<td>" + roomInfo + "</td>"
          + "<td>" + courseInfo + DOSSIER.fillStudentDocumentPanel(value, userId, labels) + "</td>"
          + "<td>" + teacherInfo + "</td>"
          + "<td>" + nc + "<p class=\"subContent\">" + sub + "</p></td>"
          + "<td>" + noteCo + "<p class=\"subContent\">" + subCo + "</p></td></tr>";
      });
      result += "<tr><th colspan=\"2\">Total</th><td colspan=\"6\"><b>" + getTimeFromMinutes(total) + "</b></td></tr>";
      $("#follow-up-student tbody").html(result);
    }
  }, "json");
};

/**
 * Ajax function to get some followUp.
 * @param {String} url xhttp url
 * @param {Object} element node clicked
 * @param {Boolean} co collective
 * @param {Object} paths default locations
 * @returns {FollowUpObject}
 */
DOSSIER.getAndFillFollowUp = function(url, element, co, paths) {
  var id = 0;
  if (element.is("img")) {
    id = $(element).attr("data-algem-followupid");
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
};

/**
 * Returns the subContent info of the followUp in argument.
 * @param {FollowUpObject} followUp
 * @param {Object} labels translations
 * @returns {String}
 */
DOSSIER.getFollowUpSubContent = function(followUp, labels) {
  var note = followUp.note;
  var abs = (followUp.status === 1);
  var exc = (followUp.status === 2);
  var sub = (followUp.id > 0 && note !== null && note.length > 0) ? "<span class=\"follow-up-note\">"+labels.score_label +" : " + note + "</span>" : "";
  sub += abs ? "<span class=\"absent\">ABS</span>" : "";
  sub += exc ? "<span class=\"excused\">EXC</span>" : "";
  return sub || "";
};

/**
 * Inits followUp dialog editing.
 * @param {type} element
 * @param {Object} labels
 * @returns {undefined}
 */
DOSSIER.initFollowUpDialog = function(element, labels) {
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
          DOSSIER.updateFollowUp($("#follow-up-form"), labels);
        }
      }
    ]
  });
}

DOSSIER.initErrorDialog = function() {
  $("#errorDialog").dialog({
    autoOpen: false,
    buttons: {
      Fermer: function () {
        $(this).dialog("close");
      }
    }
  });
}

DOSSIER.initDocumentDialog = function(labels) {

  var documentForm = $("#documentForm");
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
          DOSSIER.updateActionDocument(documentForm, labels);
        }
      }
    ]
  });
};
/**
 *
 * @param {FollowUpSchedule} f
 * @returns {undefined}
 */
//function showDialog(f) {
//  var dlg = $("#follow-up-dlg");
//  $(dlg).find("legend").html(f.name + " : " + f.time);
//  $(dlg).dialog({title: f.course + " " + f.date}).dialog("open");
//}

DOSSIER.resetFollowUpDialog = function(defPhoto) {
  $("#follow-up-photo").attr("src", defPhoto);
  $("#follow-content").val('');
  $("#follow-status").val('0');
  $("#note").val('');
  $("#follow-up-dlg").find(".error").text('');
};

DOSSIER.updateFollowUp = function(form, labels) {
  var url = $(form).attr("action");
  //update hidden field before sending
  $("#status").val($("#follow-status").val());
  $.post(url, form.serialize(), function (data) {
    if (data && data.success) {
      var content = $("#follow-content").val();
      var co = $("#noteType").val();
      var note = $("#note").prop("readonly") ? null : $("#note").val();
      var up = new FollowUpObject(data.followUp.id, data.followUp.scheduleId, content, note, data.followUp.status, co);
      DOSSIER.refreshFollowContent(data.operation, up, labels);
      $(form).next(".error").text('');
      $("#follow-up-dlg").dialog("close");
    } else {
      console.log("no update");
      $(form).next(".error").text(data.message);
    }
  }, "json");
};

DOSSIER.refreshFollowContent = function(operation, followUp, labels) {
  var subContent = this.getFollowUpSubContent(followUp, labels);
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
      $(ref).find("img.dlg").attr("data-algem-followupid", followUp.id);
    } else {
      var parent = $("#" + id).closest("li");// <li> element
      var p1 = $(parent).find('p.follow-up-content');
      $(p1).text(followUp.content);
      $(parent).find("p.subContent").html(subContent);
      if (operation == 0) {//suppression
        $("#" + id).attr("id",0);
        parent.find("img.dlg").attr("data-algem-followupid", 0);
      }
    }

  }
};

DOSSIER.updateActionDocument = function(form, labels) {
  var updateUrl = $(form).attr("action");
  var creation = $("#docId").val() == "0" ? true : false;

  $.post(updateUrl, form.serialize(), function (data) {
    if (data && data.id > 0) {
      if (creation) {
        //refresh content
        var actionId = data.actionId;
        var memberId = data.memberId;
        var docId = data.id;
        var table = $("#follow-up-result");
        var target = null;
        var refTag = "<img data-algem-actionref=\"" + actionId + "\" data-algem-docid=\"" + docId + "\" class=\"img-link doc-ref\" title=\"" + DOSSIER.getDocTypeFromNumber(data.docType, labels) + " : " + data.name + "\" alt=\"" + data.name + "\" src=\"../resources/common/img/" + DOSSIER.getIconFromDocType(data.docType) + "\" />";
        if (data.memberId > 0) {
          var li = table.find("li[data-algem-memberid='" + memberId + "']");// multiple resultats
          var row = $(li).closest("tr");
          target = row.find("a[data-algem-actionref='" + actionId + "']");
        } else {
          target = table.find("a[data-algem-actionref='" + actionId + "']");
        }
        $(target).each(function() {
          var tr = $(this).closest("tr");
          var dateText = $(tr).children("td:first-child").text();
          var rowDate = GEMUTILS.getDateFromString(dateText.split(" ").pop());
          if (GEMUTILS.isDateIncluded(rowDate, data.firstDate)) {
            $(refTag).insertBefore($(this));
          }
        });
      } else {
        var imgTag = $("img[data-algem-docid="+data.id+"]");
        $(imgTag).each(function() {
          $(this).attr("src", "../resources/common/img/" + DOSSIER.getIconFromDocType(data.docType));
          $(this).attr("title",DOSSIER.getDocTypeFromNumber(data.docType, labels) + " : " + data.name);
          $(this).attr("alt",data.name);
        });
      }
      $("#docEditor").dialog("close");
    } else {
      $(form).find(".error").text(data.name);
    }
  }, "json");

};

DOSSIER.removeActionDocument = function(element, url) {
  var id = $(element).attr("data-algem-remove-link-id");
  $.post(url, {docId: id}, function (data) {
    if (data && data === true) {
      var imgTag = $("[data-algem-docid="+id+"]");
      $(imgTag).remove();
    } else {
      console.log("erreur suppression");
    }
    $("#docEditor").dialog("close");
  });
};

DOSSIER.initWeekDates = function(today) {
  var from = $("#weekFrom");
  var to = $("#weekTo");
  $(from).datepicker({changeMonth: true, changeYear: true, dateFormat: 'dd-mm-yy'}).datepicker('setDate', today);
  $(to).datepicker({changeMonth: true, changeYear: true, dateFormat: 'dd-mm-yy'}).datepicker('setDate', today);
  $(from).val(GEMUTILS.dateFormatFR(today));
  $(to).val(GEMUTILS.dateFormatFR(today));

  var studentFrom = $("#student-weekFrom");
  var studentTo = $("#student-weekTo");
  $(studentFrom).datepicker({changeMonth: true, changeYear: true, dateFormat: 'dd-mm-yy'}).datepicker('setDate', today);
  $(studentTo).datepicker({changeMonth: true, changeYear: true, dateFormat: 'dd-mm-yy'}).datepicker('setDate', today);
  $(studentFrom).val(GEMUTILS.dateFormatFR(today));
  $(studentTo).val(GEMUTILS.dateFormatFR(today));
};

DOSSIER.setWeekDates = function(firstDay, lastDay) {
  $("#weekFrom").datepicker('setDate', firstDay);
  $("#weekTo").datepicker('setDate', lastDay);
};
DOSSIER.setStudentWeekDates = function(firstDay, lastDay) {
  $("#student-weekFrom").datepicker('setDate', firstDay);
  $("#student-weekTo").datepicker('setDate', lastDay);
};
DOSSIER.setWeekChange = function(url, idper, labels, paths) {
  var from = $("#weekFrom");
  var to = $("#weekTo");
  from.change(function () {
    var d = new Date(from.datepicker('getDate'));
    var wd = GEMUTILS.getCurrentWeekDates(d);
    DOSSIER.getFollowUpSchedules(url, idper, GEMUTILS.dateFormatFR(wd.first), GEMUTILS.dateFormatFR(wd.last), labels, paths);
    from.datepicker('setDate', wd.first);
    to.datepicker('setDate', wd.last);
    from.blur();
  });

  to.change(function () {
    var d = new Date(to.datepicker('getDate'));
    var wd = GEMUTILS.getCurrentWeekDates(d);
    DOSSIER.getFollowUpSchedules(url, idper, GEMUTILS.dateFormatFR(wd.first), GEMUTILS.dateFormatFR(wd.last), labels, paths);
    from.datepicker('setDate', wd.first);
    to.datepicker('setDate', wd.last);
    to.blur();
  });
};

DOSSIER.setStudentWeekChange = function(url, idper, labels) {
  var studentFrom = $("#student-weekFrom");
  var studentTo = $("#student-weekTo");
  studentFrom.change(function () {
    var d = new Date(studentFrom.datepicker('getDate'));
    var wd = GEMUTILS.getCurrentWeekDates(d);
    this.getFollowUpStudent(url, idper, GEMUTILS.dateFormatFR(wd.first), GEMUTILS.dateFormatFR(wd.last), labels);
    studentFrom.datepicker('setDate', wd.first);
    studentTo.datepicker('setDate', wd.last);
    studentFrom.blur();
  });

  studentTo.change(function () {
    var d = new Date(studentTo.datepicker('getDate'));
    var wd = GEMUTILS.getCurrentWeekDates(d);
    DOSSIER.getFollowUpStudent(url, idper, GEMUTILS.dateFormatFR(wd.first), GEMUTILS.dateFormatFR(wd.last), labels);
    studentFrom.datepicker('setDate', wd.first);
    studentTo.datepicker('setDate', wd.last);
    studentTo.blur();
  });
};

DOSSIER.setTeacherDateNavigation = function(url, userId, weekDates, labels, paths) {
  $("#follow-d").click(function () {
    var now = new Date();
    var dfnow = GEMUTILS.dateFormatFR(now);
    DOSSIER.getFollowUpSchedules(url, userId, dfnow, dfnow, labels, paths);
    DOSSIER.setWeekDates(dfnow, dfnow);
  });
  $("#follow-m").click(function () {
    var cmd = GEMUTILS.getCurrentMonthDates();
    var dfFirst = GEMUTILS.dateFormatFR(cmd.first);
    var dfLast = GEMUTILS.dateFormatFR(cmd.last);
    DOSSIER.getFollowUpSchedules(url, userId, dfFirst, dfLast, labels, paths);
    DOSSIER.setWeekDates(dfFirst, dfLast);
  });
  $("#follow-w").click(function () {
    var dfFirst = dateFormatFR(weekDates.first);
    var dfLast = dateFormatFR(weekDates.last);
    DOSSIER.getFollowUpSchedules(url, userId, dfFirst, dfLast, labels, paths);
    DOSSIER.setWeekDates(dfFirst, dfLast);
  });
};

DOSSIER.setStudentDateNavigation = function(url, userId, weekDates, labels) {
  $("#student-follow-d").click(function () {
    var now = new Date();
    var dfnow = GEMUTILS.dateFormatFR(now);
    DOSSIER.getFollowUpStudent(url, userId, dfnow, dfnow, labels);
    DOSSIER.setStudentWeekDates(dfnow, dfnow);
  });
  $("#student-follow-m").click(function () {
    var cmd = GEMUTILS.getCurrentMonthDates();
    var dfFirst = GEMUTILS.dateFormatFR(cmd.first);
    var dfLast = GEMUTILS.dateFormatFR(cmd.last);
    DOSSIER.getFollowUpStudent(url, userId, dfFirst, dfLast, labels);
    DOSSIER.setStudentWeekDates(dfFirst, dfLast);
  });
  $("#student-follow-w").click(function () {
    var dfFirst = dateFormatFR(weekDates.first);
    var dfLast = dateFormatFR(weekDates.last);
    DOSSIER.getFollowUpStudent(url, userId, dfFirst, dfLast, labels);
    DOSSIER.setStudentWeekDates(dfFirst, dfLast);
  });
};
