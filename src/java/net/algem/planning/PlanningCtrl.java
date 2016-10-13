/*
 * @(#)PlanningCtrl.java	1.2.0 14/04/16
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

import java.security.Principal;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import net.algem.util.AuthUtil;
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

  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy", Locale.FRANCE);

  @Autowired
  private PlanningService service;

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
  String loadDaySchedule(HttpServletRequest request, Model model, Booking booking, Principal p) throws ParseException {
    SimpleDateFormat dayNameFormat = new SimpleDateFormat("EEE");
    Date date = DATE_FORMAT.parse(request.getParameter("d"));
    String dayName = dayNameFormat.format(date);
    int estab = Integer.parseInt(request.getParameter("e"));
    booking.setTimeLength(1);
    boolean onlyPublic = AuthUtil.isAdministrativeMember();

    Map<String, Collection<ScheduleElement>> schedules = service.getDaySchedule(date, estab, onlyPublic);

    model.addAttribute("dayName", dayName);
    model.addAttribute("planning", schedules);
    model.addAttribute("estabList", service.getEstablishments(getEstabFilter(),  p == null ? "": p.getName()));
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
  String loadEstablishment(Model model, Principal p) {
    model.addAttribute("now", DATE_FORMAT.format(new Date()));
    model.addAttribute("estabList", service.getEstablishments(getEstabFilter(), p == null ? "": p.getName()));
    return "index";
  }

  @RequestMapping(method = RequestMethod.GET, value={ "perso/weekly.html"})
  String loadWeekSchedule(Model model, HttpServletRequest request) throws ParseException {
    int idper = Integer.parseInt(request.getParameter("id"));
    String sow = request.getParameter("d");
    Calendar cal = Calendar.getInstance();
    Date start = DATE_FORMAT.parse(sow);
    cal.setTime(start);
    cal.add(Calendar.DATE, 6);
    int week = cal.get(Calendar.WEEK_OF_YEAR);
    Date end = cal.getTime();

    Map<Integer, Collection<ScheduleElement>> schedules = service.getWeekSchedule(start, end, idper);

    cal.add(Calendar.DATE,-13);
    Date prev = cal.getTime();
    cal.add(Calendar.DATE, 14);
    Date next = cal.getTime();

    model.addAttribute("planning", schedules);
    DateFormatSymbols dfs = new DateFormatSymbols(Locale.FRANCE);
    model.addAttribute("weekDays", dfs.getWeekdays());
    model.addAttribute("start", start);
    model.addAttribute("end", end);
    model.addAttribute("prevDate", prev);
    model.addAttribute("nextDate", next);
    model.addAttribute("w", week);
    model.addAttribute("timeOffset", service.getTimeOffset());
    model.addAttribute("colorDefs", service.getDefaultColorCodes());
    return "weekly";
  }

  private String getEstabFilter() {
    return AuthUtil.isAdministrativeMember() ? ""
      : " AND p.id IN (SELECT DISTINCT etablissement FROM salle WHERE public = TRUE)";
  }

}
