/*
 * @(#) ErrorCtrl.java Algem Web App 1.0.6 02/01/2016
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
package net.algem.util;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.0.6
 * @since 1.0.6 02/01/2016
 */
@Controller
public class ErrorCtrl {

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @RequestMapping(value = "/404", method = RequestMethod.GET)
  public String pageNotFound() {
    return "404";
  }
  
    
  @RequestMapping(method = RequestMethod.GET, value = "error.html")
  public String genericErrorPage() {
    return "error";
  }

}
