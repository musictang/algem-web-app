/*
 * @(#) BookingCtrl.java Algem Web App 1.0.6 20/01/2016
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
import javax.servlet.http.HttpServletRequest;
import net.algem.group.Group;
import net.algem.security.User;
import net.algem.security.UserService;
import net.algem.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.0.6
 * @since 1.0.6 20/01/2016
 */
@Controller
public class BookingCtrl
{

  @Autowired
  private UserService service;
  
  @Autowired
  private PlanningService planningService;

  public void setService(UserService service) {
    this.service = service;
  }

  public void setPlanningService(PlanningService planningService) {
    this.planningService = planningService;
  }

  @RequestMapping(method = RequestMethod.GET, value = "/xbook")
  public @ResponseBody  List<Group> doXPostBooking(Principal p) {
    List<Group> groups = service.getGroups(p.getName());
    return groups;
  }

  @RequestMapping(method = RequestMethod.POST, value = "/book.html")
  public String doPostBooking(
          @ModelAttribute Booking booking,
          @RequestParam int estab,
          RedirectAttributes redirectAttributes, 
          Principal p) {
   
    redirectAttributes.addAttribute("e", estab);
    redirectAttributes.addAttribute("d", booking.getDate());
    User u = service.findUserByLogin(p.getName());
    if (u == null) {
      return "error";
    }
    
    try {
      Date date = Constants.DATE_FORMAT.parse(booking.getDate());
      Calendar cal = Calendar.getInstance();
      cal.setTime(date);
      int dow = cal.get(Calendar.DAY_OF_WEEK);
      DailyTimes dt = planningService.getDailyTimes(booking.getRoom(), dow);
      if (dt.getOpening().after(booking.getStartTime()) || dt.getClosing().before(booking.getEndTime())) {
        return "error";
      }
      
    } catch (ParseException ex) {
      Logger.getLogger(BookingCtrl.class.getName()).log(Level.SEVERE, null, ex);
    }
    
    // todo get person id
    // check params
    // construct schedule
    // email organisation
// end time is disabled in form
    Hour hEnd = new Hour(booking.getStartTime());
    hEnd.incMinute((int) (booking.getTimeLength() * 60));
    booking.setEndTime(hEnd);
    Logger.getLogger(BookingCtrl.class.getName()).log(Level.INFO, booking.toString());
    
    return "redirect:/daily.html";
  }

}
