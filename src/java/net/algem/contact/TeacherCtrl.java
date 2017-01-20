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

import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
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
  private String CSV_HEADER;
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
  void saveFollowUpAsCSV(@RequestParam String userId, @RequestParam String from, @RequestParam String to,HttpServletResponse response) throws IOException {

    File csv = getFollowUpAsCSV(userId, from, to);
    InputStream in = new FileInputStream(csv);
    response.setHeader("Content-Length", String.valueOf(csv.length()));
    response.setHeader("Content-disposition", "attachment;filename=" + csv.getName());
    response.setContentType("txt/csv");

    FileCopyUtils.copy(in, response.getOutputStream());
  }
  
  @RequestMapping(value = "/perso/teacher/savePDF", method = RequestMethod.GET, produces = "application/pdf")
  public @ResponseBody
  void saveFollowUpAsPDF(@RequestParam String userId, @RequestParam String from, @RequestParam String to,HttpServletResponse response) throws IOException {

    try {
      File pdf = getFollowUpAsPDF(userId, from, to);
      InputStream in = new FileInputStream(pdf);
      response.setHeader("Content-Length", String.valueOf(pdf.length()));
      response.setHeader("Content-disposition", "attachment;filename=" + pdf.getName());
      response.setContentType("application/pdf");
      
      FileCopyUtils.copy(in, response.getOutputStream());
    } catch (DocumentException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }
  }

  private File getFollowUpAsCSV(String userId, String from, String to) throws IOException {
    String path = "/tmp/" + "suivi-" + userId + ".csv";
    File f = new File(path);
    LOGGER.log(Level.INFO, f.getName());
    StringBuilder data = new StringBuilder();
    String nl = "\r\n";
    data.append(messageSource.getMessage("csv.header", null, LocaleContextHolder.getLocale())).append(nl);

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
  
  private File getFollowUpAsPDF(String userId, String from, String to) throws IOException, BadElementException, DocumentException {
    String path = "/tmp/" + "suivi-" + userId + ".pdf";
    File f = new File(path);
    LOGGER.log(Level.INFO, f.getName());
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    Document document = new Document(PageSize.A4.rotate());
    PdfWriter.getInstance(document, byteArrayOutputStream);  // Do this BEFORE document.open()
    document.open();

    PdfPTable table = new PdfPTable(10);
    table.setWidthPercentage(100);
    table.setWidths(new float[] {1.1f,1.2f,0.6f,1.5f,1.5f,2f,0.5f,0.5f,1.9f,1.9f});
    
    BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, false);
    BaseFont bfb = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, false);
    Font normalFont = new Font(bf, 10);
    Font boldFont = new Font(bfb, 10);
    
    String fromDate = messageSource.getMessage("from.label", null, LocaleContextHolder.getLocale());
    String toDate = messageSource.getMessage("to.label", null, LocaleContextHolder.getLocale());
    PdfPCell headerCell = new PdfPCell(new Phrase(fromDate + " " + from + " " + toDate.toLowerCase() + " " + to, boldFont));

    headerCell.setBackgroundColor(Color.LIGHT_GRAY);
    headerCell.setColspan(10);
    table.addCell(headerCell);
    
    table.addCell(new PdfPCell(new Phrase(messageSource.getMessage("date.label", null, LocaleContextHolder.getLocale()), boldFont)));
    table.addCell(new PdfPCell(new Phrase(messageSource.getMessage("time.label", null, LocaleContextHolder.getLocale()), boldFont)));
    table.addCell(new PdfPCell(new Phrase(messageSource.getMessage("time.length.label", null, LocaleContextHolder.getLocale()), boldFont)));
    table.addCell(new PdfPCell(new Phrase(messageSource.getMessage("room.label", null, LocaleContextHolder.getLocale()), boldFont)));
    table.addCell(new PdfPCell(new Phrase(messageSource.getMessage("course.label", null, LocaleContextHolder.getLocale()), boldFont)));
    table.addCell(new PdfPCell(new Phrase(messageSource.getMessage("student.label", null, LocaleContextHolder.getLocale()), boldFont)));
    table.addCell(new PdfPCell(new Phrase(messageSource.getMessage("presence.label", null, LocaleContextHolder.getLocale()), boldFont)));
    table.addCell(new PdfPCell(new Phrase(messageSource.getMessage("score.label", null, LocaleContextHolder.getLocale()), boldFont)));
    table.addCell(new PdfPCell(new Phrase(messageSource.getMessage("individual.monitoring.label", null, LocaleContextHolder.getLocale()), boldFont)));
    table.addCell(new PdfPCell(new Phrase(messageSource.getMessage("collective.monitoring.label", null, LocaleContextHolder.getLocale()), boldFont)));

    List<ScheduleElement> items = getFollowUpSchedules(userId, from, to);
    //LOGGER.log(Level.INFO, items.toString());
    for (ScheduleElement e : items) {
      List<ScheduleRangeElement> ranges = new ArrayList<ScheduleRangeElement>((Collection<? extends ScheduleRangeElement>) e.getRanges());

      for (ScheduleRangeElement r : ranges) {
        String status = getStatusFromNumber(r.getFollowUp().getStatus());
        String note = r.getFollowUp().getNote();
        String content1 = r.getFollowUp().getContent();
        String content2 = e.getFollowUp().getContent();
        table.addCell(new Phrase(e.getDateFr().toString(),normalFont));
        table.addCell(new Phrase(r.getStart() + "-" + r.getEnd(),normalFont));
        table.addCell(new Phrase(new Hour(r.getLength()).toString(),normalFont));
        table.addCell(new Phrase(e.getDetail().get("room").getName(),normalFont));
        table.addCell(new Phrase(e.getDetail().get("course").getName(),normalFont));
        table.addCell(new Phrase(r.getPerson().getFirstName() + " " + r.getPerson().getName(), normalFont));
        table.addCell(new Phrase(status,normalFont));
        table.addCell(new Phrase(note == null ? "" : note, normalFont));
        table.addCell(new Phrase(content1 == null ? "" : content1.replaceAll("[\r\n]", " "),normalFont));
        table.addCell(new Phrase(content2 == null ? "" : content2.replaceAll("[\r\n]", " "),normalFont));
      }
    }

    document.add(table);
    document.close();
    byte[] pdfBytes = byteArrayOutputStream.toByteArray();
    Files.write(Paths.get(path), pdfBytes);
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
