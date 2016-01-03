/*
 * @(#)UserCtrl.java	1.0.6 30/11/15
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
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller for login operations.
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.0.6
 * @since 1.0.0 11/02/13
 */
@Controller
//@Scope("session")
public class UserCtrl
{

  @Autowired
  @Qualifier("authenticationManager")
  AuthenticationManager authenticationManager;

  @Autowired
  SecurityContextRepository repository;

  @Autowired
  private UserService service;

  @Resource(name = "messageSource")
  private MessageSource messageSource;

  public UserCtrl() {
  }

  public void setService(UserService service) {
    this.service = service;
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
   * Manage ajax GET request.
   */
  @RequestMapping(value="/jxlogin.html",method = RequestMethod.GET)
  public String login() {
    return "fragments/xlogin :: login";
  }

  /**
   * Manage ajax POST request.
   * @param username username transmitted
   * @param password clear password
   * @param request httprequest
   * @param response httpresponse
   * @return JSON data as string
   */
  // IMPORTANT HERE : produces="text/html" !! AND NOT application/json or text/plain !
  @RequestMapping(value="/jxlogin.html", method = RequestMethod.POST, produces = "text/html;charset=UTF-8")
  @ResponseBody
  public String performLogin(
    @RequestParam("j_username") String username,
    @RequestParam("j_password") String password,
    HttpServletRequest request, HttpServletResponse response,
    User currentUser) {
    try {
      SecurityContext secuContext = SecurityContextHolder.getContext();
//      UserDetails userDetails = (UserDetails) secuContext.getAuthentication().getPrincipal();

      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth != null && !(auth instanceof AnonymousAuthenticationToken) && auth.isAuthenticated()) {
        String msg = messageSource.getMessage("already.connected.label", null, LocaleContextHolder.getLocale());
        return "{\"msg\":\"" + msg + "\"}";
      }
      UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
      auth = authenticationManager.authenticate(token);
      secuContext.setAuthentication(auth);
//      repository.saveContext(secuContext, request, response);
        String msg = messageSource.getMessage("login.success.label", new Object[]{username}, LocaleContextHolder.getLocale());
//			rememberMeServices.loginSuccess(request, response, auth);
        return "{\"msg\":\"" + msg + "\"}";
    } catch (BadCredentialsException ex) {
//      String err = messageSource.getMessage("login.error.label", new Object[]{username}, LocaleContextHolder.getLocale() );
      return "{\"status\": \"" + ex.getMessage() + "\"}";
    } catch (Exception other) {
      other.printStackTrace();
      return "{\"status\": \"" + other.getMessage() + "\"}";
    }
  }

  @RequestMapping(method = RequestMethod.GET, value = "perso/tuto.html")
  public String tuto() {
    return "tuto";
  }

  @RequestMapping(method = RequestMethod.GET, value = "perso/home.html")
  public String showHome(Principal p, Model model) {
    User u = service.findUserByLogin(p.getName());
    model.addAttribute("user", u);
    return "dossier";
  }

  @RequestMapping(method = RequestMethod.GET, value = "signup.html")
  public String doSignUp(User u) {
    return "signup";
  }

  @RequestMapping(method = RequestMethod.POST, value = "signup.html")
  public String signUp(@Valid User user, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return "signup";
    }
    try {
      if (!service.isPerson(user)) {
        bindingResult.rejectValue("id", "id.not.found");
      }
      List<User> others = service.exist(user);
      if (others != null) {
        for (User o : others) {
          if (o.getId() == user.getId()) {
            bindingResult.rejectValue("id", "", messageSource.getMessage("user.exists", null, LocaleContextHolder.getLocale()));
            return "signup";
          } else if (o.getLogin() != null && o.getLogin().equals(user.getLogin())) {
              bindingResult.rejectValue("login", "", messageSource.getMessage("login.used", null, LocaleContextHolder.getLocale()));
              return "signup";
          }
        }
      }
    } catch(DataAccessException ex) {
      bindingResult.rejectValue("id", "empty.result", ex.getMessage());
      return "signup";
    }

    return "redirect:perso/home.html";
  }

  @RequestMapping(method = RequestMethod.GET, value = "/user.html")
  public String showUserACL(HttpServletRequest request, Model model) {
    List<Map<String, Boolean>> acl = service.getAcl(Integer.parseInt(request.getParameter("id")));
    model.addAttribute("acl", acl);
    return "dossier";
  }

  public void testBefore() {
    System.out.println("test before");
  }
}
