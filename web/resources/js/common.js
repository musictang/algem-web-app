/*
 * @(#)common.js    1.4.0 16/07/16
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
 * Global script for algem web module.
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.4.0
 * @since 1.0.6 28/12/15
 */

function setCommonEvents() {
  var delay = 500;
  jQuery.ajaxSetup({
    beforeSend: function () {
      $("#busy").show();
    },
    complete: function () {
      $("#busy").hide();
    },
    cache: false // IMPORTANT : IE,Edge fix
  });
  //$(document).tooltip(); // default tooltip styling
  $(".labels").tooltip({
    content: function (callback) {
      callback($(this).prop('title').replace(/;/g, '<br />'));
    }
  });

  $('#bars').click(function () {
    $('#menu').show(delay);
  });

  $('.close-link').click(function () {
    $(this).parents('.closeable').hide(delay);
    return false;
  });

  $('#menu-close').click(function () {
    $('#menu').hide(delay);
    return false;
  });
  $('#help').click(function () {
    $(this).css('cursor', 'pointer');
    $('#help-panel').show(delay);
  });

  $('#info').click(function () {
    $(this).css('cursor', 'pointer');
    $('#info-panel').show(delay);
  });

  $('#menu-login').click(function () {
    $('#login-panel').show(delay);
  });

  $("#ajax-login-form").submit(function (event) {
    ajaxLogin($("#login-panel form"));
    event.preventDefault();
  });

  $("#signup-form, #reset-form").submit(function (event) {
    if ($(this).find("#password").val() !== $(this).find("#password2").val()) {
      $("#signup-error").text("Les mots de passe ne correspondent pas.")
      event.preventDefault();
    }
  });

  var getHost = function () {
//    var port = (window.location.port == "8080") ? ":8443" : "";
    var port = window.location.port;
    return 'http://' + window.location.hostname + port;
//    return ((secure) ? 'https://' : 'http://') + window.location.hostname + port;
  };

  function ajaxLogin(form) {
    form.find(".error").hide();
    var urlPath = $("#login-panel form").attr('action');
    //jQuery.post( url [, data ] [, success ] [, dataType ] )
    $.post(
      urlPath,
      form.serialize(),
      function (data, textStatus) {
        var err = $("#login-panel p.error");
        var suc = $("#login-panel p.success");
        if (data.msg) {
          //console.log("ajax success");
          if (err.is(':visible')) {
            err.hide();
          }
          suc.html(data.msg);
          suc.show();
          //location.reload();
          window.location.href = "perso/home.html";
          //$("#login-panel").hide(2000);
        } else {
          //console.log("ajax error : " + data.status);
          if (suc.is(':visible')) {
            suc.hide();
          }
          err.html(data.status);
          err.show();
        }
      }, "json");
  }

}

/** From http://detectmobilebrowsers.com/. */
function isMobile() {
  return /(android|bb\d+|meego).+mobile|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\.(browser|link)|vodafone|wap|windows ce|xda|xiino/i.test(navigator.userAgent || navigator.vendor || window.opera) || /1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\-|your|zeto|zte\-/i.test((navigator.userAgent || navigator.vendor || window.opera).substr(0, 4));
}

