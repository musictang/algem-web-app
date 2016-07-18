/*
 * @(#) TeacherCtrl.java Algem Web App 1.4.0 13/07/16
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
package net.algem.contact;

import java.security.Principal;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import net.algem.planning.FollowUp;
import net.algem.planning.FollowUpException;
import net.algem.planning.ScheduleElement;
import static net.algem.util.Constants.DATE_FORMAT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.4.0
 * @since 1.4.0 21/06/2016
 */
@Controller
public class TeacherCtrl
{

  private final static int NOTE_LENGTH = 16;
  @Autowired
  private TeacherService service;

  @Resource(name = "messageSource")
  private MessageSource messageSource;

  public void setService(TeacherService service) {
    this.service = service;
  }

  @RequestMapping(method = RequestMethod.GET, value = "/perso/xFollowUpSchedules")
  public @ResponseBody
  List<ScheduleElement> getFollowUp(
          @RequestParam("userId") String userId,
          @RequestParam("from") String from,
          @RequestParam("to") String to,
          Principal p) {
    List<ScheduleElement> f = null;
    try {
      Date dateFrom = DATE_FORMAT.parse(from);
      Date dateTo = DATE_FORMAT.parse(to);
      f = service.getFollowUpSchedules(Integer.parseInt(userId), dateFrom, dateTo);
    } catch (DataAccessException ex) {
      Logger.getLogger(TeacherCtrl.class.getName()).log(Level.SEVERE, null, ex);
    } catch (ParseException ex) {
      Logger.getLogger(TeacherCtrl.class.getName()).log(Level.SEVERE, null, ex);
    }
    return f;
  }

  @RequestMapping(method = RequestMethod.GET, value = "/perso/xFollowUp")
  public @ResponseBody
  FollowUp getFollowUp(@RequestParam("id") String id) {
    return service.getFollowUp(Integer.parseInt(id));
  }

  @RequestMapping(method = RequestMethod.POST, value = "/perso/xEditFollowUp")
  public @ResponseBody
  FollowUpResponse editFollowUp(
          @RequestParam String id,
          @RequestParam String scheduleId,
          @RequestParam String collective,
          @RequestParam String content,
          @RequestParam String note,
          @RequestParam String abs,
          @RequestParam String exc
  ) {
    FollowUp up = new FollowUp();
    int paramId = Integer.parseInt(id);
    try {
      up.setId(Integer.parseInt(id));
      up.setScheduleId(Integer.parseInt(scheduleId));
      up.setCollective(Boolean.parseBoolean(collective));
      up.setContent(content);
      if (note.length() > NOTE_LENGTH) {
        throw new FollowUpException("follow-up.note.invalid.length", new Object[]{NOTE_LENGTH});
      }
      if (note.matches("^[\\w,\\.\\+-]*$")) {
        up.setNote(note);
      } else {
        throw new IllegalArgumentException("follow-up.note.invalid.entry");
      }
      up.setAbsent(Boolean.parseBoolean(abs));
      up.setExcused(Boolean.parseBoolean(exc));
    } catch (FollowUpException ex) {
      return getErrorResponse(ex.getMessage(), ex.getArgs());
    } catch (IllegalArgumentException ex) {
      Logger.getLogger(TeacherCtrl.class.getName()).log(Level.SEVERE, ex.getMessage());
      return getErrorResponse(ex.getMessage());
    }
    Logger.getLogger(TeacherCtrl.class.getName()).log(Level.INFO, up.toString());
    if (service.updateFollowUp(up)) {
      return new FollowUpResponse(true, paramId == 0, up);
    } else {
      return getErrorResponse("error.has.occurred");
    }

  }

  private FollowUpResponse getErrorResponse(String msg) {
    FollowUpResponse response = new FollowUpResponse(false, false);
    response.setMessage(messageSource.getMessage(msg, null, LocaleContextHolder.getLocale()));
    return response;
  }

  private FollowUpResponse getErrorResponse(String msg, Object[] args) {
    FollowUpResponse response = new FollowUpResponse(false, false);
    response.setMessage(messageSource.getMessage(msg, args, LocaleContextHolder.getLocale()));
    return response;
  }

  private class FollowUpResponse
  {

    private boolean success;
//    private boolean collective;
    private boolean creation;
    private String message;
    private int id;
    private FollowUp followUp;

    public FollowUpResponse() {
    }

    public FollowUpResponse(boolean success, boolean creation) {
      this.success = success;
//      this.collective = collective;
      this.creation = creation;
    }

    public FollowUpResponse(boolean success, boolean creation, FollowUp followUp) {
      this(success, creation);
      this.followUp = followUp;
    }

    public FollowUp getFollowUp() {
      return followUp;
    }

    public void setFollowUp(FollowUp followUp) {
      this.followUp = followUp;
    }

//
//    public boolean isCollective() {
//      return collective;
//    }
//
//    public void setCollective(boolean collective) {
//      this.collective = collective;
//    }

    public boolean isCreation() {
      return creation;
    }

    public void setCreation(boolean creation) {
      this.creation = creation;
    }

    public boolean isSuccess() {
      return success;
    }

    public void setSuccess(boolean success) {
      this.success = success;
    }

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }

  }

}
