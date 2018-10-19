/*
 * @(#) BookingCtrl.java Algem Web App 1.7.3 15/02/18
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
 */
package net.algem.planning;

import java.security.Principal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import net.algem.contact.Person;
import net.algem.group.Group;
import net.algem.room.Room;
import net.algem.security.User;
import net.algem.security.UserService;
import net.algem.util.GemConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.7.3
 * @since 1.0.6 20/01/2016
 */
@Controller
public class BookingCtrl {

  private final static Logger LOGGER = Logger.getLogger(BookingCtrl.class.getName());
  private final static Locale CTX_LOCALE = LocaleContextHolder.getLocale();

  @Autowired
  private UserService userService;

  @Autowired
  private PlanningService planningService;

  @Resource(name = "messageSource")
  private MessageSource messageSource;

  @Autowired
  private MailSender mailSender;

  @Autowired
  private SimpleMailMessage bookingMessage;

  @Autowired
  private SimpleMailMessage bookingCancelMessage;

  public void setService(UserService service) {
    this.userService = service;
  }

  public void setPlanningService(PlanningService planningService) {
    this.planningService = planningService;
  }

  @RequestMapping(method = RequestMethod.GET, value = "/xgroups")
  public @ResponseBody
  List<Group> getGroups(Principal p) {
    List<Group> groups = userService.getGroups(p.getName());
    return groups;
  }

  @RequestMapping(method = RequestMethod.GET, value = "/xpass")
  public @ResponseBody
  boolean hasPass(Principal p) {
    try {
      return userService.hasPass(p.getName());
    } catch (DataAccessException de) {
      return false;
    }
  }

  @RequestMapping(method = RequestMethod.POST, value = "/book.html")
  public String doPostBooking(
    @ModelAttribute Booking booking,
    @RequestParam int estab,
    RedirectAttributes redirectAttributes,
    Principal p,
    Model model) {

    //redirectAttributes.addAttribute("d", booking.getDate());
    //redirectAttributes.addAttribute("e", estab);
    redirectAttributes.addAttribute("section", "2");

    SecurityContext secuContext = SecurityContextHolder.getContext();
    if (!secuContext.getAuthentication().isAuthenticated()) {
      model.addAttribute("message", messageSource.getMessage("booking.auth.error", null, CTX_LOCALE));
      return "error";
    }

    User u = userService.findUserByLogin(p.getName());
    if (u == null) {
      model.addAttribute("message", messageSource.getMessage("booking.user.error", null, CTX_LOCALE));
      return "error";
    }

    try {
      BookingValidator validator = new BookingValidator();
      validator.isValidTimeLength(booking.getTimeLength());

      // end time is disabled in form
      booking.setEndTime(getBookingEndTime(booking.getStartTime(), booking.getTimeLength()));

      DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm");
      Date date = df.parse(booking.getDate() + " " + booking.getStartTime().toString());
      //LOGGER.log(Level.INFO, date.toString());
      BookingConf conf = planningService.getBookingConf();

      StringBuilder msgBuilder = new StringBuilder();
      if (!validator.isValidMaxBookingDate(date, conf.getMaxDelay())) {
        msgBuilder.append(messageSource.getMessage("booking.max.delay.warning", new Object[]{conf.getMaxDelay()}, CTX_LOCALE));
      }
      if (!validator.isValidMinBookingDate(date, conf.getMinDelay())) {
        msgBuilder.append(messageSource.getMessage("booking.min.delay.warning", new Object[]{conf.getMinDelay()}, CTX_LOCALE));
      }

      Calendar cal = Calendar.getInstance();
      cal.setTime(date);
      int dow = cal.get(Calendar.DAY_OF_WEEK);
      String msg = validator.isValidBookingTimes(booking, planningService.getDailyTimes(booking.getRoom(), dow), messageSource);
      if (msg != null) {
        msgBuilder.append(msg);
      };
      if (msgBuilder.length() > 0) {
        LOGGER.log(Level.INFO, msgBuilder.toString());
        model.addAttribute("message", msgBuilder.toString());
        return "error";
      }

      booking.setPerson(u.getId());

      List<ScheduleElement> roomConflicts = planningService.getRoomConflicts(booking);
      if (roomConflicts.size() > 0) {
        model.addAttribute("message", messageSource.getMessage("booking.room.conflict.error", null, CTX_LOCALE));
        model.addAttribute("data", roomConflicts);
        return "error";
      }

      List<ScheduleElement> personConflicts = planningService.getPersonConflicts(booking);
      if (personConflicts.size() > 0) {
        model.addAttribute("message", messageSource.getMessage("booking.person.conflict.error", null, CTX_LOCALE));
        model.addAttribute("data", personConflicts);
        return "error";
      }
      //person may also attend some course or group rehearsal : TODO check conflicts ?

      LOGGER.log(Level.INFO, booking.toString());
      planningService.book(booking);
      sendMessage(booking, "booking.send.info", bookingMessage);
    } catch (ParseException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
      model.addAttribute("message", messageSource.getMessage("date.format.error", null, CTX_LOCALE));
      return "error";
    } catch (DataAccessException ex) {
      model.addAttribute("message", messageSource.getMessage("data.exception", new Object[]{ex.getMessage()}, CTX_LOCALE));
      return "error";
    }

    return "redirect:/perso/home.html";
  }

  private Hour getBookingEndTime(Hour start, float timeLength) {
    Hour end = new Hour(start);
    int tl = (int) (timeLength * 60F);//!IMPORTANT 60F (float)
    // change end time if after midnight
    if (end.toMinutes() + tl > 1440) {
      tl = 1440 - end.toMinutes();
    }
    end.incMinute(tl);
    if (Hour.NULL_HOUR.equals(end.toString())) {
      end = new Hour("24:00");
    }
    return end;
  }

  @RequestMapping(method = RequestMethod.GET, value = "/perso/book-cancel.html")
  public String cancelBooking(Model model, @RequestParam int id, @RequestParam int action, @RequestParam String date, @RequestParam String start) {
    try {
      String info = action + " " + date + " " + start;
      LOGGER.log(Level.INFO, info);

      Booking b = planningService.getBooking(id);
      if (b == null) {
        return "redirect:/perso/home.html";
      }
      //b.setDate(date);
      if (Booking.CONFIRMED == b.getStatus()) {
        model.addAttribute("message", messageSource.getMessage("booking.confirmed.cancel.warning", null, CTX_LOCALE));
        return "error";
      }

      Calendar cal = Calendar.getInstance();
      cal.setTime(GemConstants.DATE_FORMAT.parse(date));
      cal.set(Calendar.HOUR_OF_DAY, b.getStartTime().getHour());
      cal.set(Calendar.MINUTE, b.getStartTime().getMinute());

      BookingConf conf = planningService.getBookingConf();
      long delay = conf.getCancelDelay() * 60 * 60 * 1000;
      Date now = new Date();
      if (now.getTime() > cal.getTimeInMillis() - delay) {
        model.addAttribute("message", messageSource.getMessage("booking.cancel.delay.warning", null, CTX_LOCALE));
        return "error";
      }

      if (planningService.cancelBooking(action)) {
        sendMessage(b, "booking.cancel.info", bookingCancelMessage);
        return "redirect:/perso/home.html";
      } else {
        model.addAttribute("message", "Erreur sql");
        return "error";
      }
    } catch (ParseException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
      return "error";
    } catch (DataAccessException de) {
      LOGGER.log(Level.SEVERE, null, de);
      if (de instanceof EmptyResultDataAccessException) {
        model.addAttribute("message", messageSource.getMessage("booking.not.found.warning", null, CTX_LOCALE));
      } else {
        model.addAttribute("message", de.getMessage());
      }
      return "error";
    }

  }

  /**
   * Post mail info.
   *
   * @param booking booking instance
   * @param msgKey property key
   * @param template template message
   */
  private void sendMessage(Booking booking, String msgKey, SimpleMailMessage template) {
    SimpleMailMessage mail = new SimpleMailMessage(template);
    Person p = userService.getPersonFromUser(booking.getPerson());
    String from = "info@localhost";
    try {
      if (p != null && p.getEmails().size() > 0) {
        from = p.getEmails().get(0).getEmail();
      }
    } catch (EmptyResultDataAccessException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }
    Room room = planningService.getRoom(booking.getRoom());
    String now = GemConstants.DATE_FORMAT.format(new Date());
    Object[] args = new Object[]{room.getName(), p == null ? "Anonymous" : p.toString(), now, booking.getDate()};
    String msg = messageSource.getMessage(msgKey, args, CTX_LOCALE);

    mail.setFrom(from);
    mail.setText(msg);
    mailSender.send(mail);
  }

  private void addMessageAttribute(Model m, String key, Object[] params) {
    m.addAttribute("message", messageSource.getMessage(key, params, CTX_LOCALE));
  }

  class BookingValidator {

    private final static float MIN_TIME_LENGTH = 0.5f;
    private final static float MAX_TIME_LENGTH = 8f;

    boolean isValidTimeLength(float length) {
      return length >= MIN_TIME_LENGTH && length <= MAX_TIME_LENGTH;
    }

    boolean isValidMinBookingDate(Date d, int minDelay) {
      Calendar cal = Calendar.getInstance();

      cal.setTime(new Date());
      cal.add(Calendar.HOUR_OF_DAY, minDelay);
      Date min = cal.getTime();

      return !d.before(min);
    }

    boolean isValidMaxBookingDate(Date d, int maxDelay) {
      Calendar cal = Calendar.getInstance();

      cal.setTime(new Date());
      cal.add(Calendar.DATE, maxDelay);
      Date max = cal.getTime();
      return !d.after(max);
    }

    String isValidBookingTimes(Booking b, DailyTimes dt, MessageSource msgSource) {
      String msg = null;

      if (dt.getOpening().after(b.getStartTime())
        || dt.getClosing().before(b.getEndTime())) {
        if (dt.getOpening().equals(dt.getClosing())) {
          msg = msgSource.getMessage("booking.room.closed.error", null, CTX_LOCALE);
        } else {
          msg = msgSource.getMessage("booking.room.closed.warning",
            new Object[]{dt.getOpening().toString(), dt.getClosing().toString()},
            CTX_LOCALE);
        }
      }
      return msg;
    }

  }

}
