/*
 * @(#) BookingCtrl.java Algem Web App 1.1.0 26/02/16
 *
 * Copyright (c) 2015 Musiques Tangentes. All Rights Reserved.
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
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import net.algem.contact.Person;
import net.algem.group.Group;
import net.algem.room.Room;
import net.algem.security.User;
import net.algem.security.UserService;
import net.algem.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
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
 * @version 1.1.0
 * @since 1.0.6 20/01/2016
 */
@Controller
public class BookingCtrl
{

  @Autowired
  private UserService service;

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
    this.service = service;
  }

  public void setPlanningService(PlanningService planningService) {
    this.planningService = planningService;
  }

  @RequestMapping(method = RequestMethod.GET, value = "/xgroups")
  public @ResponseBody
  List<Group> getGroups(Principal p) {
    List<Group> groups = service.getGroups(p.getName());
    return groups;
  }

  @RequestMapping(method = RequestMethod.GET, value = "/xpass")
  public @ResponseBody
  boolean hasPass(Principal p) {
    try {
      return service.hasPass(p.getName());
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

    redirectAttributes.addAttribute("d", booking.getDate());
    redirectAttributes.addAttribute("e", estab);
    SecurityContext secuContext = SecurityContextHolder.getContext();
    if (!secuContext.getAuthentication().isAuthenticated()) {
      model.addAttribute("message", messageSource.getMessage("booking.auth.error", null, LocaleContextHolder.getLocale()));
      return "error";
    }

    User u = service.findUserByLogin(p.getName());
    if (u == null) {
      model.addAttribute("message", messageSource.getMessage("booking.user.error", null, LocaleContextHolder.getLocale()));
      return "error";
    }

    try {
      // end time is disabled in form
      Hour hEnd = new Hour(booking.getStartTime());
      hEnd.incMinute((int) (booking.getTimeLength() * 60));
      booking.setEndTime(hEnd);
      Date date = Constants.DATE_FORMAT.parse(booking.getDate());
      Calendar cal = Calendar.getInstance();
      cal.setTime(date);
      int dow = cal.get(Calendar.DAY_OF_WEEK);
      DailyTimes dt = planningService.getDailyTimes(booking.getRoom(), dow);
      if (dt.getOpening().after(booking.getStartTime()) || dt.getClosing().before(booking.getEndTime())) {
        String msg = messageSource.getMessage("booking.room.closed.error",
                new Object[]{dt.getOpening().toString(), dt.getClosing().toString()},
                LocaleContextHolder.getLocale());
        Logger.getLogger(BookingCtrl.class.getName()).log(Level.INFO, msg);
        model.addAttribute("message", msg);
        return "error";
      }

      booking.setPerson(u.getId());

      List<ScheduleElement> roomConflicts = planningService.getRoomConflicts(booking);
      if (roomConflicts.size() > 0) {
        model.addAttribute("message", messageSource.getMessage("booking.room.conflict.error", null, LocaleContextHolder.getLocale()));
        model.addAttribute("data", roomConflicts);
        return "error";
      }

      List<ScheduleElement> personConflicts = planningService.getPersonConflicts(booking);
      if (personConflicts.size() > 0) {
        model.addAttribute("message", messageSource.getMessage("booking.person.conflict.error", null, LocaleContextHolder.getLocale()));
        model.addAttribute("data", personConflicts);
        return "error";
      }

      Logger.getLogger(BookingCtrl.class.getName()).log(Level.INFO, booking.toString());
      planningService.book(booking);
      sendMessage(booking, "booking.send.info", bookingMessage);
    } catch (ParseException ex) {
      Logger.getLogger(BookingCtrl.class.getName()).log(Level.SEVERE, null, ex);
      model.addAttribute("message", messageSource.getMessage("date.format.error", null, LocaleContextHolder.getLocale()));
      return "error";
    } catch (DataAccessException ex) {
      model.addAttribute("message", messageSource.getMessage("data.exception", new Object[]{ex.getMessage()}, LocaleContextHolder.getLocale()));
      return "error";
    }

    return "redirect:/perso/home.html";
  }

  @RequestMapping(method = RequestMethod.GET, value = "/perso/book-cancel.html")
  public String cancelBooking(Model model, @RequestParam int id, @RequestParam int action, @RequestParam String date, @RequestParam String start) {
    try {
      String info = action + " " + date + " " + start;
      Logger.getLogger(BookingCtrl.class.getName()).log(Level.INFO, info);
      Date d = Constants.DATE_FORMAT.parse(date);
      Calendar cal = Calendar.getInstance();
      cal.setTime(d);
      Date now = new Date();
//      BookingConf conf = planningService.getBookingConf();
//      long delay = conf.getMinDelay() * 60 * 60 * 1000;
      Booking b = planningService.getBooking(id);
      if (b != null) {
        b.setDate(date);
        if (b.getStatus() == 1) {
          model.addAttribute("message", messageSource.getMessage("booking.confirmed.cancel.warning", null, LocaleContextHolder.getLocale()));
          return "error";
        }
      }

      if (now.getTime() > d.getTime()) {
        model.addAttribute("message", messageSource.getMessage("booking.cancel.delay.warning", null, LocaleContextHolder.getLocale()));
        return "error";
      }
      // n'annuler que si confirmé
      if (planningService.cancelBooking(action)) {
        sendMessage(b, "booking.cancel.info", bookingCancelMessage);
        return "redirect:/perso/home.html";
      } else {
        model.addAttribute("message", "Erreur sql");
        return "error";
      }
    } catch (ParseException ex) {
      Logger.getLogger(BookingCtrl.class.getName()).log(Level.SEVERE, null, ex);
      return "error";
    } catch (DataAccessException de) {
      Logger.getLogger(BookingCtrl.class.getName()).log(Level.SEVERE, null, de);
      if (de instanceof EmptyResultDataAccessException) {
        model.addAttribute("message", messageSource.getMessage("booking.not.found.warning", null, LocaleContextHolder.getLocale()));
      } else {
        model.addAttribute("message", de.getMessage());
      }
      return "error";
    }

  }

  /**
   * Post mail info.
   * @param booking booking instance
   * @param msgKey property key
   * @param template template message
   */
  private void sendMessage(Booking booking, String msgKey, SimpleMailMessage template) {
    SimpleMailMessage mail = new SimpleMailMessage(template);
    Person p = service.getPersonFromUser(booking.getPerson());
    String from = "info@localhost";
    try {
      if (p != null && p.getEmail().size() > 0) {
        from = p.getEmail().get(0).getEmail();
      }
    } catch (EmptyResultDataAccessException ex) {
      Logger.getLogger(BookingCtrl.class.getName()).log(Level.SEVERE, null, ex);
    }
    Room room = planningService.getRoom(booking.getRoom());
    String now = Constants.DATE_FORMAT.format(new Date());
    Object[] args = new Object[]{room.getName(), p == null ? "Anonymous" : p.toString(), now, booking.getDate()};
    String msg = messageSource.getMessage(msgKey, args, LocaleContextHolder.getLocale());

    mail.setFrom(from);
    mail.setText(msg);
    mailSender.send(mail);
  }

  private void addMessageAttribute(Model m, String key, Object[] params) {
    m.addAttribute("message", messageSource.getMessage(key, params, LocaleContextHolder.getLocale()));
  }

}
