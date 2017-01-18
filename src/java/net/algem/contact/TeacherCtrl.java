/*
 * @(#) TeacherCtrl.java Algem Web App 1.5.2 18/01/17
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
package net.algem.contact;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import net.algem.planning.FollowUp;
import net.algem.planning.FollowUpException;
import net.algem.planning.Hour;
import net.algem.planning.ScheduleElement;
import net.algem.planning.ScheduleRangeElement;
import static net.algem.util.GemConstants.DATE_FORMAT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.5.2
 * @since 1.4.0 21/06/2016
 */
@Controller
public class TeacherCtrl
{

  private final static int NOTE_LENGTH = 8;
  private final static Logger LOGGER = Logger.getLogger(TeacherCtrl.class.getName());
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
    return getFollowUpSchedules(userId, from, to);
  }


  @RequestMapping(method = RequestMethod.GET, value = "/perso/xFollowUp")
  public @ResponseBody
  FollowUp getFollowUp(@RequestParam("id") String id) {
    return service.getFollowUp(Integer.parseInt(id));
  }

  @RequestMapping(value = "/perso/teacher/saveCSV", method = RequestMethod.GET, produces = "txt/csv")
  public @ResponseBody
  void saveFollowUp(@RequestParam String userId, @RequestParam String from, @RequestParam String to,HttpServletResponse response) throws IOException {

    File csv = getFollowUpAsFile(userId, from, to);
    InputStream in = new FileInputStream(csv);
    response.setHeader("Content-Length", String.valueOf(csv.length()));
    response.setHeader("Content-disposition", "attachment;filename=" + csv.getName());
    response.setContentType("txt/csv");

    FileCopyUtils.copy(in, response.getOutputStream());
  }

  private File getFollowUpAsFile(String userId, String from, String to) throws IOException {
    String path = "/tmp/" + "suivi-" + userId + ".csv";
    File f = new File(path);
    LOGGER.log(Level.INFO, f.getName());
    StringBuilder data = new StringBuilder();
    String nl = "\r\n";
    data.append("Jour;Horaire;Durée;Salle;Cours;Elève;Statut;Note;Suivi individuel;Suivi collectif").append(nl);

    List<ScheduleElement> items = getFollowUpSchedules(userId, from, to);

    for (ScheduleElement e : items) {
      List<ScheduleRangeElement> ranges = new ArrayList<ScheduleRangeElement>((Collection<? extends ScheduleRangeElement>) e.getRanges());

      for (ScheduleRangeElement r : ranges) {
        String status = getStatusFromNumber(r.getFollowUp().getStatus());
        String note = r.getFollowUp().getNote();
        String content1 = r.getFollowUp().getContent();
        String content2 = e.getFollowUp().getContent();
        data.append(e.getDateFr()).append(';')
          .append(r.getStart()).append('-').append(r.getEnd()).append(';')
          .append(new Hour(r.getLength())).append(';')
          .append(e.getDetail().get("room").getName()).append(';')
          .append(e.getDetail().get("course").getName()).append(';')
          .append(r.getPerson().getFirstName()).append(' ').append(r.getPerson().getName()).append(';')
          .append(status).append(';')
          .append(note == null ? "" : note).append(';')
          .append(content1 == null ? "" : content1.replaceAll("[\r\n]", " ")).append(';')
          .append(content2 == null ? "" : content2.replaceAll("[\r\n]", " ")).append(nl);
      }
    }
    Files.write(Paths.get(path), data.toString().getBytes());

    return f;
  }

  private List<ScheduleElement> getFollowUpSchedules(String userId, String from, String to) {
    try {
      Date dateFrom = DATE_FORMAT.parse(from);
      Date dateTo = DATE_FORMAT.parse(to);
      return service.getFollowUpSchedules(Integer.parseInt(userId), dateFrom, dateTo);
    } catch (DataAccessException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    } catch (ParseException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }
    return null;
  }


  private String getStatusFromNumber(short code) {
    switch(code) {
      case 0: return "";
      case 1: return "ABS";
      case 2: return "EXC";
      default: return "";
    }
  }

  @RequestMapping(method = RequestMethod.POST, value = "/perso/xEditFollowUp")
  public @ResponseBody
  FollowUpResponse editFollowUp(
          @RequestParam String id,
          @RequestParam String scheduleId,
          @RequestParam String collective,
          @RequestParam String content,
          @RequestParam String note,
          @RequestParam String status
  ) {
    FollowUp up = new FollowUp();
    try {
      up.setId(Integer.parseInt(id));
      up.setScheduleId(Integer.parseInt(scheduleId));
      up.setCollective(Boolean.parseBoolean(collective));
      up.setContent(content != null && content.length() > 512 ? content.substring(0,512): content);
      if (note.length() > NOTE_LENGTH) {
        throw new FollowUpException("follow-up.note.invalid.length", new Object[]{NOTE_LENGTH});
      }
      if (note.matches("^[\\w,\\.\\+-]*$")) {
        up.setNote(note);
      } else {
        throw new IllegalArgumentException("follow-up.note.invalid.entry");
      }
      up.setStatus(Short.parseShort(status));
    } catch (FollowUpException ex) {
      return getErrorResponse(ex.getMessage(), ex.getArgs());
    } catch (IllegalArgumentException ex) {
      LOGGER.log(Level.SEVERE, ex.getMessage());
      return getErrorResponse(ex.getMessage());
    }
    LOGGER.log(Level.INFO, up.toString());
    int result = service.updateFollowUp(up);
    if (result < 0) {
      return getErrorResponse("error.has.occurred");
    } else {
      return new FollowUpResponse(true, result, up);
    }

  }

  private FollowUpResponse getErrorResponse(String msg) {
    FollowUpResponse response = new FollowUpResponse(false, -1);
    response.setMessage(messageSource.getMessage(msg, null, LocaleContextHolder.getLocale()));
    return response;
  }

  private FollowUpResponse getErrorResponse(String msg, Object[] args) {
    FollowUpResponse response = new FollowUpResponse(false, -1);
    response.setMessage(messageSource.getMessage(msg, args, LocaleContextHolder.getLocale()));
    return response;
  }

  class FollowUpResponse
  {

    private boolean success;
    private int operation;
    private String message;
    private int id;
    private FollowUp followUp;

    public FollowUpResponse() {
    }

    public FollowUpResponse(boolean success, int operation) {
      this.success = success;
      this.operation = operation;
    }

    public FollowUpResponse(boolean success, int operation, FollowUp followUp) {
      this(success, operation);
      this.followUp = followUp;
    }

    public FollowUp getFollowUp() {
      return followUp;
    }

    public void setFollowUp(FollowUp followUp) {
      this.followUp = followUp;
    }

    public int getOperation() {
      return operation;
    }

    public void setOperation(int operation) {
      this.operation = operation;
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
