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
import java.util.List;
import net.algem.group.Group;
import net.algem.security.User;
import net.algem.security.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.0.6
 * @since 1.0.6 20/01/2016
 */
@Controller
public class BookingCtrl {

  @Autowired
  private UserService service;
//
//  @Autowired
//private MappingJackson2HttpMessageConverter jacksonMessageConverter;

  public void setService(UserService service) {
    this.service = service;
  }

  @RequestMapping(method = RequestMethod.GET, value = "/book.html")
  public String doGetBooking(Schedule booking) {
    return "daily";
  }


  @RequestMapping(method = RequestMethod.POST, value = "/xbook.html")
  public @ResponseBody  List<Group> doXPostBooking(Principal p) {
    List<Group> groups = service.getGroups(p.getName());
    return groups;
  }

  @RequestMapping(method = RequestMethod.POST, value = "/book.html")
  public void doPostBooking() {

  }

}
