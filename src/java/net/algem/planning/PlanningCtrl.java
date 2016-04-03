/*
 * @(#)PlanningCtrl.java	1.2.0 02/04/16
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
package net.algem.planning;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * MVC Controller for planning view.
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.2.0
 * @since 1.0.0 11/02/13
 */
@Controller
public class PlanningCtrl
{

  @Autowired
  private PlanningService service;

  private final String estabFilter = " AND id IN (SELECT DISTINCT etablissement FROM salle WHERE public = TRUE)";

  public void setService(PlanningService service) {
    this.service = service;
  }

  /**
   * Adds attributes to model for displaying day's schedule.
   *
   * @param request http request
   * @param model Spring MVC model
   * @return a string representing the view
   * @throws ParseException
   */
  @RequestMapping(method = RequestMethod.GET, value = "/daily.html")
  String loadDaySchedule(HttpServletRequest request, Model model, Booking booking) throws ParseException {
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.FRANCE);
    SimpleDateFormat dayNameFormat = new SimpleDateFormat("EEE");
    Date date = dateFormat.parse(request.getParameter("d"));
    String dayName = dayNameFormat.format(date);
    int estab = Integer.parseInt(request.getParameter("e"));
    booking.setTimeLength(1);

    Map<Integer, Collection<ScheduleElement>> schedules = service.getDaySchedule(date, estab);

    model.addAttribute("dayName", dayName);
    model.addAttribute("planning", schedules);
    model.addAttribute("estabList", service.getEstablishments(estabFilter));
    model.addAttribute("freeplace", service.getFreePlace(date, estab));

    model.addAttribute("conf", service.getConf());
    model.addAttribute("timeOffset", service.getTimeOffset());
    model.addAttribute("bookingConf", service.getBookingConf());
    model.addAttribute("roomInfo", service.getRoomInfo(estab));
    model.addAttribute("colorDefs", service.getDefaultColorCodes());

    return "daily";
  }

  /**
   * Adds to model the list of establishments.
   *
   * @param model
   */
  @RequestMapping(method = RequestMethod.GET, value={ "/", "index.html"})
  String loadEstablishment(Model model) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.FRANCE);
    model.addAttribute("now", dateFormat.format(new Date()));
    model.addAttribute("estabList", service.getEstablishments(estabFilter));
    return "index";
  }

  @RequestMapping(method = RequestMethod.GET, value={ "perso/weekly.html"})
  String loadWeekSchedule(Model model, HttpServletRequest request) {
    int id = Integer.parseInt(request.getParameter("id"));
    int week = Integer.parseInt(request.getParameter("w"));

    Calendar cal = Calendar.getInstance();

    cal.set(Calendar.WEEK_OF_YEAR, week-1);
    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
    Date start = cal.getTime();
    cal.add(Calendar.DATE, 6);
    Date end = cal.getTime();
    Logger.getLogger(ScheduleDao.class.getName()).log(Level.INFO, start.toString());
    Logger.getLogger(ScheduleDao.class.getName()).log(Level.INFO, end.toString());
    Map<Integer, Collection<ScheduleElement>> schedules = service.getWeekSchedule(start, end, id);

    model.addAttribute("planning", schedules);
    DateFormatSymbols dfs = new DateFormatSymbols(Locale.FRANCE);
    model.addAttribute("weekDays", dfs.getWeekdays());
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.FRANCE);
    model.addAttribute("start", dateFormat.format(start));
    model.addAttribute("end", dateFormat.format(end));
    StringBuilder sb = new StringBuilder();
    for (String s : dfs.getWeekdays()) {
      sb.append(s);
    }
    Logger.getLogger(PlanningCtrl.class.getName()).log(Level.INFO, sb.toString());
//    model.addAttribute("estabList", service.getEstablishments(estabFilter));
    return "weekly";
  }
}
