/*
 * @(#)UserCtrl.java    1.7.4 19/10/18
 *
 * Copyright (c) 2015-2018 Musiques Tangentes. All Rights Reserved.
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
package net.algem.security;

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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import net.algem.config.Config;
import net.algem.config.ConfigKey;
import net.algem.contact.Email;
import net.algem.contact.Person;
import net.algem.planning.BookingScheduleElement;
import net.algem.planning.Hour;
import net.algem.planning.PlanningService;
import net.algem.planning.ScheduleElement;
import net.algem.planning.ScheduleRangeElement;
import net.algem.util.CommonDao;
import net.algem.contact.Family;
import static net.algem.util.GemConstants.DATE_FORMAT;
import net.algem.util.Postit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 * Controller for login operations.
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.7.4
 * @since 1.0.0 11/02/13
 */
@Controller
//@Scope("session")
@SessionAttributes(value = "family", types = {net.algem.contact.Family.class})
public class UserCtrl {

    private final static Logger LOGGER = Logger.getLogger(UserCtrl.class.getName());
    private final static Locale CTX_LOCALE = LocaleContextHolder.getLocale();

    @Value("#{organization}")
    private Map<String, String> organization;

    @Autowired
    @Qualifier("authenticationManager")
    AuthenticationManager authenticationManager;

    @Autowired
    SecurityContextRepository secCtxRepo;

    @Autowired
    private UserService service;

    @Autowired
    private PlanningService planningService;

    @Resource(name = "messageSource")
    private MessageSource messageSource;

    @Autowired
    private MailSender mailSender;

    @Autowired
    private SimpleMailMessage recoverMessage;
    
    //@Autowired 
    //private ApplicationContext appContext;

    public UserCtrl() {
    }

    public void setService(UserService service) {
        this.service = service;
    }

    public void setMailSender(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void setRecoverMessage(SimpleMailMessage recoverMessage) {
        this.recoverMessage = recoverMessage;
    }

    @ModelAttribute("family")
    public Family addFamily() {
        return new Family();
    }

    @RequestMapping(method = RequestMethod.GET, value = "login.html")
    public String logIn() {
        return "login";
    }

    // Login form with error
    @RequestMapping("/login-error.html")
    public String loginError(Model model) {
        model.addAttribute("loginError", true);
        return "index";
    }

    /**
     * Manage ajax POST request.
     *
     * @param username username transmitted
     * @param password clear password
     * @param request httprequest
     * @param response httpresponse
     * @return JSON data as string
     */
// IMPORTANT HERE if mapping ends with html : produces="text/html" !! AND NOT application/json or text/plain !
//@RequestMapping(value = "/jxlogin.html", method = RequestMethod.POST, produces = "text/html;charset=UTF-8")
    @RequestMapping(value = "/jxlogin")
    @ResponseBody
    public String performLogin(
            @RequestParam("j_username") String username,
            @RequestParam("j_password") String password,
            HttpServletRequest request, HttpServletResponse response) {
        try {
        /*
            String[] beans = appContext.getBeanDefinitionNames();
            Arrays.sort(beans);
            for (String b :  beans) {
                LOGGER.info("BEANS:"+b);
            }
        */
            SecurityContext secuContext = SecurityContextHolder.getContext();

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && !(auth instanceof AnonymousAuthenticationToken) && auth.isAuthenticated()) {
                String msg = messageSource.getMessage("already.connected.label", null, CTX_LOCALE);
                return "{\"msg\":\"" + msg + "\"}";
            }
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username.trim(), password);
            auth = authenticationManager.authenticate(token);
            secuContext.setAuthentication(auth);
//      secCtxRepo.saveContext(secuContext, request, response);
            String msg = messageSource.getMessage("login.success.label", new Object[]{username}, CTX_LOCALE);
//          rememberMeServices.loginSuccess(request, response, auth);
            return "{\"msg\":\"" + msg + "\"}";
        } catch (BadCredentialsException ex) {
            return "{\"status\": \"" + ex.getMessage() + "\"}";
        } catch (Exception other) {
            LOGGER.log(Level.SEVERE, null, other);
            return "{\"status\": \"" + other.getMessage() + "\"}";
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "perso/tuto.html")
    public String tuto() {
        return "tuto";
    }

    /**
     * Show home page.
     * @param p
     * @param model
     * @param _prs private postit read status
     * @return a view as string
     */
    @RequestMapping(method = RequestMethod.GET, value = "perso/home.html")
    public String showHome(Principal p, Model model,
            @CookieValue(value = "_PRS", defaultValue = "false") String _prs,
            @RequestParam(value = "section", required = false) Integer section,
            @RequestParam(value = "idchild", required = false) Integer idchild,
            @ModelAttribute("family") Family family) {

        User u = service.findUserByLogin(p.getName());

        if (family.getCurrentId() == 0) {
            family.setParent(u);
            family.setChildrens(service.findChildrenById(u.getId()));
            family.setCurrentId(u.getId());
        }

        if (idchild != null) {
            if (idchild != u.getId()) {
                for (User child : family.getChildrens()) {
                    if (child.getId() == idchild) {
                        u = child;
                        family.setCurrentId(idchild);
                    }
                }
            }
        } else {
            family.setCurrentId(u.getId());
        }

        model.addAttribute("user", u);
        List<BookingScheduleElement> bookings = planningService.getBookings(u.getId(), Integer.MAX_VALUE);
        model.addAttribute("bookings", bookings);
        model.addAttribute("person", service.getPersonFromUser(u.getId()));
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        model.addAttribute("startOfW", cal.getTime());
        Config c = service.getConf(ConfigKey.DEFAULT_ESTABLISHMENT.getKey());
        model.addAttribute("e", c.getValue());

        if ("false".equals(_prs)) {
            List<Postit> postitList = planningService.getPostits(u.getId());
            if (u.isTeacher()) {
                postitList.addAll(planningService.getPostits(Postit.TEACHERS));
            }
            model.addAttribute("postitList", postitList);
        }

        return "dossier";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/perso/jxBookings")
    public @ResponseBody List<BookingScheduleElement> getBookings(
            @RequestParam("idper") int idper, @RequestParam("key") int key) {
        return planningService.getBookings(idper, key);
    }

    @RequestMapping(method = RequestMethod.GET, value = "signup.html")
    public String signUp(User u) {
        return "signup";
    }

    @RequestMapping(method = RequestMethod.POST, value = "signup.html")
    public String doSignUp(@Valid User user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "signup";
        }
        try {
            Person p = null;
            try {
                p = service.getPersonFromUser(user.getId());
            } catch (EmptyResultDataAccessException e) {
                LOGGER.log(Level.SEVERE, null, e);
                bindingResult.rejectValue("id", "user.person.error", new Object[]{user.getId()}, "");
                return "signup";
            }
            List<User> others = service.exist(user);
            if (others != null) {
                for (User o : others) {
                    if (o.getId() == user.getId()) {
                        bindingResult.rejectValue("id", "", messageSource.getMessage("user.exists", null, CTX_LOCALE));
                        return "signup";
                    } else if (o.getLogin() != null && o.getLogin().equals(user.getLogin())) {
                        bindingResult.rejectValue("login", "", messageSource.getMessage("login.used", null, CTX_LOCALE));
                        return "signup";
                    }
                }
            }
            if (p == null) {
                bindingResult.rejectValue("id", "user.person.error", new Object[]{user.getId()}, "");
                return "signup";
            }
            if (p.getType() != Person.PERSON && p.getType() != Person.ROOM) {
                bindingResult.rejectValue("id", "user.type.error");
                return "signup";
            }

            if (!existingEmail(p, user.getEmail())) {
                bindingResult.rejectValue("email", "user.email.error", new Object[]{user.getEmail()}, "");
                return "signup";
            }

            user.setTeacher(service.isTeacher(user.getId())); // detect user status
            service.create(user);
        } catch (DataAccessException | UserException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            bindingResult.rejectValue("id", "user.account.create.exception", new Object[]{ex.getLocalizedMessage()}, ex.getMessage());
            return "signup";
        }

        return "redirect:perso/home.html";
    }

    /**
     * Gets the page for recovery password request.
     *
     * @param user
     * @return a view
     */
    @RequestMapping(method = RequestMethod.GET, value = "recover.html")
    public String recoverPassword(User user) {
        return "recover";
    }

    @RequestMapping(method = RequestMethod.POST, value = "recover.html")
    public String doRecoverPassword(@RequestParam String email, @RequestParam String contactId, HttpServletRequest request, Model model) {
        long userId;
        try {
            userId = Integer.parseInt(contactId);
            if (userId < 0 || userId > Integer.MAX_VALUE) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException nfe) {
            model.addAttribute("errorMessage", messageSource.getMessage("invalid.member.id", null, CTX_LOCALE));
            return "recover";
        }

        User found = service.findAuthenticatedUser(email, (int) userId);
        if (found == null) {
            model.addAttribute("errorMessage", messageSource.getMessage("unknown.user", null, CTX_LOCALE));
            return "recover";
        }

        String token = UUID.randomUUID().toString();
        //FIX https
        //String url = "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
        String url = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();

        try {
            service.setToken(found.getId(), token);
            sendRecoverMessage(url, token, found);
            model.addAttribute("message", messageSource.getMessage("recover.send.info", null, CTX_LOCALE));
        } catch (MailException | DataAccessException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            model.addAttribute("errorMessage", messageSource.getMessage("recover.send.exception", new Object[]{ex.getMessage()}, CTX_LOCALE));
        }

        return "recover";
    }

    /**
     * Method called when the user clicked on the link sent by email.
     *
     * @param u
     * @param id
     * @param token
     * @param model
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "recover.html", params = {"id", "token"})
    public String recover(User u, @RequestParam("id") int id, @RequestParam("token") String token, Model model) {
        PasswordResetToken resetToken = null;
        try {
            resetToken = service.getToken(id);
            if (!token.equals(resetToken.getToken())) {
                model.addAttribute("message", messageSource.getMessage("recover.invalid.token", null, CTX_LOCALE));
                return "error";
            }
        } catch (EmptyResultDataAccessException ex) {
            model.addAttribute("message", messageSource.getMessage("recover.invalid.token", new Object[]{ex.getMessage()}, CTX_LOCALE));
            return "error";
        } catch (DataAccessException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            model.addAttribute("message", messageSource.getMessage("data.exception", new Object[]{ex.getMessage()}, CTX_LOCALE));
            return "error";
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(resetToken.getCreation()));
        cal.add(Calendar.DAY_OF_MONTH, 1);// 24h delay
        LOGGER.log(Level.INFO, "today" + new Date() + " token " + cal.getTime());
        if (new Date().after(cal.getTime())) {
            model.addAttribute("message", messageSource.getMessage("recover.expired.token", null, CTX_LOCALE));
            return "error";
        }

        u.setId(id);

//    Authentication auth = new UsernamePasswordAuthenticationToken(
//      user, null, userDetailsService.loadUserByUsername(user.getEmail()).getAuthorities());
//    SecurityContextHolder.getContext().setAuthentication(auth);
        return "reset";
    }

    @RequestMapping(method = RequestMethod.POST, value = "passreset.html")
    public String doUpdatePassword(@Valid User user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "reset";
        }
        service.updatePassword(user.getId(), user.getPassword());
        return "login";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/user.html")
    public String showUserACL(HttpServletRequest request, Model model) {
        /*
        User u = service.findUserById(Integer.parseInt(request.getParameter("id")));
        model.addAttribute("user", u); //test eric
        model.addAttribute("person", service.getPersonFromUser(u.getId())); //testeric
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        model.addAttribute("startOfW", cal.getTime()); //testeric
        */
        List<Map<String, Boolean>> acl = service.getAcl(Integer.parseInt(request.getParameter("id")));
        model.addAttribute("acl", acl);
        return "dossier";
    }

    @RequestMapping(value = "/xmember")
    public @ResponseBody
    boolean isMember(Principal p, @RequestParam String start, @RequestParam String end) {
        return service.isMember(p.getName(), start, end);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/perso/xFollowUpStudent")
    public @ResponseBody
    List<ScheduleElement> getFollowUp(
            @RequestParam("userId") String userId,
            @RequestParam("from") String from,
            @RequestParam("to") String to,
            Principal p) {
        return getFollowUpSchedules(userId, from, to);
    }

    @RequestMapping(value = "/perso/user/savePDF", method = RequestMethod.GET, produces = "application/pdf")
    public @ResponseBody
    void saveFollowUpAsPDF(@RequestParam String userId, @RequestParam String from, @RequestParam String to, HttpServletResponse response) throws IOException {

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

    private List<ScheduleElement> getFollowUpSchedules(String userId, String from, String to) {
        Date dateFrom = null;
        Date dateTo = null;
        // !important : convert all numbers to long
        long max = 1000L * 60L * 60L * 24L * 365L;// 1year
        try {
            dateFrom = DATE_FORMAT.parse(from);
            dateTo = DATE_FORMAT.parse(to);
        } catch (ParseException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            dateFrom = new Date();
            dateTo = new Date();
        }

        if (dateFrom.after(dateTo)) {
            dateFrom.setTime(dateTo.getTime());
        }
        long interval = dateTo.getTime() - dateFrom.getTime();
        if (interval > max) {
            dateTo.setTime(dateFrom.getTime() + max);
        }

        try {
            return service.getFollowUp(Integer.parseInt(userId), dateFrom, dateTo);
        } catch (DataAccessException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return null;
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
        table.setWidths(new float[]{1.1f, 1.2f, 0.6f, 1.5f, 1.5f, 2f, 0.5f, 0.5f, 1.9f, 1.9f});

        BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, false);
        BaseFont bfb = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, false);
        Font normalFont = new Font(bf, 10);
        Font boldFont = new Font(bfb, 10);

        String fromLabel = messageSource.getMessage("from.label", null, CTX_LOCALE);
        String toLabel = messageSource.getMessage("to.label", null, CTX_LOCALE);
        String prefix = messageSource.getMessage("follow-up.label", null, CTX_LOCALE) + " " + organization.get("name.label");
        String period = fromLabel.toLowerCase() + " " + from + " " + toLabel.toLowerCase() + " " + to;
        PdfPCell headerCell = new PdfPCell(new Phrase(prefix + " " + period, boldFont));

        headerCell.setBackgroundColor(Color.LIGHT_GRAY);
        headerCell.setColspan(10);
        table.addCell(headerCell);

        table.addCell(new PdfPCell(new Phrase(messageSource.getMessage("date.label", null, CTX_LOCALE), boldFont)));
        table.addCell(new PdfPCell(new Phrase(messageSource.getMessage("time.label", null, CTX_LOCALE), boldFont)));
        table.addCell(new PdfPCell(new Phrase(messageSource.getMessage("time.length.label", null, CTX_LOCALE), boldFont)));
        table.addCell(new PdfPCell(new Phrase(messageSource.getMessage("room.label", null, CTX_LOCALE), boldFont)));
        table.addCell(new PdfPCell(new Phrase(messageSource.getMessage("course.label", null, CTX_LOCALE), boldFont)));
        table.addCell(new PdfPCell(new Phrase(messageSource.getMessage("teacher.label", null, CTX_LOCALE), boldFont)));
        String abs = messageSource.getMessage("absence.label", null, CTX_LOCALE);
        table.addCell(new PdfPCell(new Phrase(abs != null ? abs.substring(0, 3) + "." : "", boldFont)));
        table.addCell(new PdfPCell(new Phrase(messageSource.getMessage("score.label", null, CTX_LOCALE), boldFont)));
        table.addCell(new PdfPCell(new Phrase(messageSource.getMessage("individual.logbook.label", null, CTX_LOCALE), boldFont)));
        table.addCell(new PdfPCell(new Phrase(messageSource.getMessage("collective.comment.label", null, CTX_LOCALE), boldFont)));

        fillPdfTable(table, getFollowUpSchedules(userId, from, to), normalFont);

        document.add(table);
        document.close();
        byte[] pdfBytes = byteArrayOutputStream.toByteArray();
        Files.write(Paths.get(path), pdfBytes);
        return f;
    }

    private void fillPdfTable(PdfPTable table, List<ScheduleElement> items, Font font) {
        for (ScheduleElement e : items) {
            List<ScheduleRangeElement> ranges = new ArrayList<ScheduleRangeElement>((Collection<? extends ScheduleRangeElement>) e.getRanges());

            for (ScheduleRangeElement r : ranges) {
                String status = CommonDao.getAbsenceFromNumberStatus(r.getFollowUp().getStatus());
                String note = r.getFollowUp().getNote();
                String content1 = r.getFollowUp().getContent();
                String content2 = e.getFollowUp().getContent();
                table.addCell(new Phrase(e.getDateFr().toString(), font));
                table.addCell(new Phrase(r.getStart() + "-" + r.getEnd(), font));
                table.addCell(new Phrase(new Hour(r.getLength()).toString(), font));
                table.addCell(new Phrase(e.getDetail().get("room").getName(), font));
                table.addCell(new Phrase(e.getDetail().get("course").getName(), font));
                table.addCell(new Phrase(e.getDetail().get("teacher").getName(), font));
                table.addCell(new Phrase(status, font));
                table.addCell(new Phrase(note == null ? "" : note, font));
                table.addCell(new Phrase(content1 == null ? "" : content1.replaceAll("[\r\n]", " "), font));
                table.addCell(new Phrase(content2 == null ? "" : content2.replaceAll("[\r\n]", " "), font));
            }
        }
    }

    private boolean existingEmail(Person p, String email) {
        for (Email e : p.getEmails()) {
            if (e.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }

    private void sendRecoverMessage(String path, String token, User user) throws MailException {
        // Create a thread safe "copy" of the template message and customize it
        SimpleMailMessage mail = new SimpleMailMessage(recoverMessage);
        String args = "/recover.html?id=" + user.getId() + "&token=" + token;
        String msg = messageSource.getMessage("recover.info", new Object[]{user.toString(), user.getLogin()}, CTX_LOCALE);
        String url = path + args;
        String signature = "\r\n\r\n--\r\n" + organization.get("name.label") + "\r\n" + organization.get("address.label");
        mail.setTo(user.getEmail());
        mail.setText(msg + url + signature);
        mailSender.send(mail);
    }
}
