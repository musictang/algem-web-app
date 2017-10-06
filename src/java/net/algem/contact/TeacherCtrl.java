/*
 * @(#) TeacherCtrl.java Algem Web App 1.7.1 06/10/17
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
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import net.algem.planning.FollowUp;
import net.algem.planning.FollowUpException;
import net.algem.planning.Hour;
import net.algem.planning.ActionDocument;
import net.algem.planning.ScheduleElement;
import net.algem.planning.ScheduleRangeElement;
import net.algem.util.CommonDao;
import net.algem.util.GemConstants;
import static net.algem.util.GemConstants.DATE_FORMAT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
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
 * @version 1.7.0
 * @since 1.4.0 21/06/2016
 */
@Controller
public class TeacherCtrl
{

  private final static int NOTE_LENGTH = 8;
  private final static Logger LOGGER = Logger.getLogger(TeacherCtrl.class.getName());
  private final static Locale CTX_LOCALE = LocaleContextHolder.getLocale();
  private String CSV_HEADER;
  @Autowired
  private TeacherService service;

  @Resource(name = "messageSource")
  private MessageSource messageSource;

  @Value("#{organization}")
  private Map<String, String> organization;

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
    data.append(messageSource.getMessage("csv.header", null, CTX_LOCALE)).append(nl);

    List<ScheduleElement> items = getFollowUpSchedules(userId, from, to);

    for (ScheduleElement e : items) {
      List<ScheduleRangeElement> ranges = new ArrayList<ScheduleRangeElement>((Collection<? extends ScheduleRangeElement>) e.getRanges());

      for (ScheduleRangeElement r : ranges) {
        String status = CommonDao.getAbsenceFromNumberStatus(r.getFollowUp().getStatus());
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
    Files.write(Paths.get(path), data.toString().getBytes("UTF-16LE"));// Excel hack argument (UTF-16LE)

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

    String fromLabel = messageSource.getMessage("from.label", null, CTX_LOCALE);
    String toLabel = messageSource.getMessage("to.label", null, CTX_LOCALE);
    String prefix = messageSource.getMessage("follow-up.label", null, CTX_LOCALE) + " " + organization.get("name.label");
    String period = fromLabel.toLowerCase() + " " + from + " " + toLabel.toLowerCase() + " " + to;
    PdfPCell headerCell = new PdfPCell(new Phrase( prefix + " " + period, boldFont));

    headerCell.setBackgroundColor(Color.LIGHT_GRAY);
    headerCell.setColspan(10);
    table.addCell(headerCell);

    table.addCell(new PdfPCell(new Phrase(messageSource.getMessage("date.label", null, CTX_LOCALE), boldFont)));
    table.addCell(new PdfPCell(new Phrase(messageSource.getMessage("time.label", null, CTX_LOCALE), boldFont)));
    table.addCell(new PdfPCell(new Phrase(messageSource.getMessage("time.length.label", null, CTX_LOCALE), boldFont)));
    table.addCell(new PdfPCell(new Phrase(messageSource.getMessage("room.label", null, CTX_LOCALE), boldFont)));
    table.addCell(new PdfPCell(new Phrase(messageSource.getMessage("course.label", null, CTX_LOCALE), boldFont)));
    table.addCell(new PdfPCell(new Phrase(messageSource.getMessage("student.label", null, CTX_LOCALE), boldFont)));
    String abs = messageSource.getMessage("absence.label", null, CTX_LOCALE);
    table.addCell(new PdfPCell(new Phrase(abs != null ? abs.substring(0,3) + "." : "", boldFont)));
    table.addCell(new PdfPCell(new Phrase(messageSource.getMessage("score.label", null, CTX_LOCALE), boldFont)));
    table.addCell(new PdfPCell(new Phrase(messageSource.getMessage("individual.logbook.label", null, CTX_LOCALE), boldFont)));
    table.addCell(new PdfPCell(new Phrase(messageSource.getMessage("collective.comment.label", null, CTX_LOCALE), boldFont)));

    List<ScheduleElement> items = getFollowUpSchedules(userId, from, to);
    //LOGGER.log(Level.INFO, items.toString());
    for (ScheduleElement e : items) {
      List<ScheduleRangeElement> ranges = new ArrayList<ScheduleRangeElement>((Collection<? extends ScheduleRangeElement>) e.getRanges());

      for (ScheduleRangeElement r : ranges) {
        String status = CommonDao.getAbsenceFromNumberStatus(r.getFollowUp().getStatus());
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
    Date dateFrom = null;
    Date dateTo = null;
    try {
      dateFrom = DATE_FORMAT.parse(from);
      dateTo = DATE_FORMAT.parse(to);
    } catch (ParseException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
      dateFrom = new Date();
      dateTo = new Date();
    }
    try {
      return service.getFollowUpSchedules(Integer.parseInt(userId), dateFrom, dateTo);
    } catch (DataAccessException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }
    return null;
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
    int op = service.updateFollowUp(up);
    if (op < 0) {
      return getErrorResponse("error.has.occurred");
    } else {
      return new FollowUpResponse(true, op, up);
    }

  }

  @RequestMapping(method = RequestMethod.POST, value = "/perso/xUpdateAbsenceStatus")
  public @ResponseBody
  FollowUpResponse updateAbsenceStatus(
    @RequestParam String id,
    @RequestParam String scheduleId,
    @RequestParam String status
  ) {
    FollowUp up = new FollowUp();
    try {
      up.setId(Integer.parseInt(id));
      up.setScheduleId(Integer.parseInt(scheduleId));
      up.setStatus(Short.parseShort(status));

      int op = service.updateAbsenceStatus(up);
      return new FollowUpResponse(op >= 0, op, up);
    } catch (IllegalArgumentException ex) {
      return new FollowUpResponse(false, 0, up);
    }

  }

@RequestMapping(method = RequestMethod.GET, value = "/perso/xGetActionDocument")
  public @ResponseBody
  ActionDocument getDocument(@RequestParam("docId") String id) {
    return service.getDocument(Integer.parseInt(id));
  }

  @RequestMapping(method = RequestMethod.POST, value = "/perso/xUpdateActionDocument")
  public @ResponseBody
  ActionDocument updateActionDocument(
          @RequestParam String docId,
          @RequestParam String docDate,
          @RequestParam String actionId,
          @RequestParam String scheduleId,
          @RequestParam String memberId,
          @RequestParam String docType,
          @RequestParam String docName,
          @RequestParam String docUri
  ) {
    ActionDocument doc = new ActionDocument();
    try {
      int id = Integer.parseInt(docId);
      int action = Integer.parseInt(actionId);
      int schedule = Integer.parseInt(scheduleId);
      int member = Integer.parseInt(memberId);
      short type = Short.parseShort(docType);
      LOGGER.log(Level.INFO, docDate);
      Date date = GemConstants.DATE_FORMAT.parse(docDate);
      doc.setId(id);
      doc.setFirstDate(date);
      doc.setActionId(action);
      doc.setScheduleId(schedule);
      doc.setMemberId(member);
      doc.setDocType(type);
      doc.setName(docName);
      if (!isValidURI(docUri)) {
        throw new IllegalArgumentException(messageSource.getMessage("document.invalid.uri.warning", null,CTX_LOCALE));
      }
      doc.setUri(docUri);
      if (doc.getId() == 0) {
        doc.setId(service.createDocument(doc));
      } else {
        service.updateDocument(doc);
      }
    } catch (IllegalArgumentException | ParseException | DataAccessException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
      doc.setId(0); doc.setName(ex.getMessage());
    }
    return doc;
  }

  boolean isValidURI(String uri) {
    boolean validPrefix = false;
    int idx = 0;
    char forbiddenChars[] = {'<', '>', '"'};
    String prefixes [] = {"file://", "ftp://", "ftps://", "ftpes://", "http://", "https://", "sftp://", "www."};
    for(String p : prefixes) {
      if (uri.startsWith(p)) {
        validPrefix = true;
        idx = p.length();
        break;
      }
    }
    if (!validPrefix) return false;
    uri = uri.substring(idx);
    LOGGER.info(uri);
    for (int i = 0; i < uri.length(); i++) {
      for (int j = 0; j < forbiddenChars.length; j++) {
        if (uri.charAt(i) == forbiddenChars[j]) {
          return false;
        }
      }
    }
    return validPrefix && !uri.matches("^.*(javascript|=alert|:alert).*$");
  }

  @RequestMapping(method = RequestMethod.POST, value = "/perso/xRemoveActionDocument")
  public @ResponseBody
  Boolean removeActionDocument(@RequestParam String docId) {
    try {
      int id = Integer.parseInt(docId);
      service.removeDocument(id);
      return true;
    } catch (IllegalArgumentException | DataAccessException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
      return false;
    }
  }


  private FollowUpResponse getErrorResponse(String msg) {
    FollowUpResponse response = new FollowUpResponse(false, -1);
    response.setMessage(messageSource.getMessage(msg, null, CTX_LOCALE));
    return response;
  }

  private FollowUpResponse getErrorResponse(String msg, Object[] args) {
    FollowUpResponse response = new FollowUpResponse(false, -1);
    response.setMessage(messageSource.getMessage(msg, args, CTX_LOCALE));
    return response;
  }

  public class FollowUpResponse
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
