/*
 * @(#)LoginCtrl.java	1.0.6 30/11/15
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
 *
 */
package net.algem.security;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller for login operations.
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.0.6
 * @since 1.0.0 11/02/13
 */
@Controller
@Scope("session")
public class LoginCtrl
{

  @Autowired
  private UserDao dao;

  @Autowired
  private UserService service;
//	@Autowired
//	private UserValidator validator;

  public LoginCtrl() {
  }

  public LoginCtrl(UserDao dao) {
    this.dao = dao;
  }

  public UserService getService() {
    return service;
  }

  public void setService(UserService service) {
    this.service = service;
  }

//	public void setValidator(UserValidator validator) {
//		this.validator = validator;
//	}
  @RequestMapping(method = RequestMethod.GET, value = "login.html")
  public String logIn() {
    return "tpl/login";
  }

   // Login form with error
  @RequestMapping("/login-error.html")
  public String loginError(Model model) {
    model.addAttribute("loginError", true);
    return "tpl/login.html";
  }

   @RequestMapping(method = RequestMethod.GET, value = "perso/tuto.html")
  public String tuto() {
    return "tpl/tuto";
  }
//
//	@RequestMapping(method = RequestMethod.POST, value = "login.html")
//	public String login(@ModelAttribute("user") User user, BindingResult result, Model model) {
//		//testBefore();
//		validator.validate(user, result);
//		if (result.hasErrors()) {
//			//model.addAttribute("profiles", Profile.values());
//			return "login";
//		}
//		User found = null;
//		try {
//			found = dao.find(user.getLogin());
//		} catch (DataAccessException e) {
//          System.err.println(e.getMessage());
//		}
//
//		if (found == null) {
//          model.addAttribute("unknown", "unknown.user.label");
//			return "login";
//		}
//		model.addAttribute("user", found);
//		//List<Map<String, Boolean>> list = dao.listMenuAccess(found.getId());
//		//model.addAttribute("acl", list);
//		return "welcome";
//	}

  @RequestMapping(method = RequestMethod.GET, value = "perso/home.html")
  public String showHome(Principal p, Model model) {
    User u = service.findUserByLogin(p.getName());
    model.addAttribute("user", u);
    return "tpl/welcome";
  }

  @RequestMapping(method = RequestMethod.GET, value = "/user.html")
  public String showUserACL(HttpServletRequest request, Model model) {
    User found = dao.findById(Integer.parseInt(request.getParameter("id")));
    model.addAttribute("user", found);
    List<Map<String, Boolean>> list = dao.listMenuAccess(found.getId());
    model.addAttribute("acl", list);
    return "welcome";
  }

  public void testBefore() {
    System.out.println("test before");
  }
}
