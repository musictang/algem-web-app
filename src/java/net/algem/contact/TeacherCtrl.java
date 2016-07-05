/*
 * @(#) TeacherCtrl.java Algem Web App 1.4.0 27/06/2016
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
import net.algem.planning.FollowUp;
import net.algem.planning.ScheduleElement;
import static net.algem.util.Constants.DATE_FORMAT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
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
public class TeacherCtrl {
  
  @Autowired
  private TeacherService service;

  public void setService(TeacherService service) {
    this.service = service;
  }
  
 @RequestMapping(method = RequestMethod.GET, value = "/perso/xFollowUp")
  public @ResponseBody List<ScheduleElement> getFollowUp(
          @RequestParam("userId") String userId,
          @RequestParam("from") String from,
          @RequestParam("to") String to,
          Principal p) {
    List<ScheduleElement> f = null;
    try {
      Date dateFrom = DATE_FORMAT.parse(from);
      Date dateTo = DATE_FORMAT.parse(to);
      f = service.getFollowUp(Integer.parseInt(userId), dateFrom, dateTo);
    } catch(DataAccessException ex) {
      Logger.getLogger(TeacherCtrl.class.getName()).log(Level.SEVERE, null, ex);
    } catch (ParseException ex) {
      Logger.getLogger(TeacherCtrl.class.getName()).log(Level.SEVERE, null, ex);
    }
    return f;
  }
  
  @RequestMapping(method = RequestMethod.POST, value = "/perso/xEditFollowUp")
  public @ResponseBody boolean editFollowUp(
          @RequestParam("noteId") int id,
          @RequestParam("scheduleId") int scheduleId,
          @RequestParam("noteType") boolean type,
          @RequestParam("content") String content) {
    FollowUp up = new FollowUp();
    up.setId(id);
    up.setContent(content);
    System.out.println(up.getId());
    System.out.println(up.getContent());
    if (id == 0) {
      // create
    } else {
      //service.updateFollowUp(up);
    }
    
    return false;
  }
  
  
          
}
