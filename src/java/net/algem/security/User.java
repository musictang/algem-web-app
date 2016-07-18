/*
 * @(#)User.java	1.4.0 16/07/16
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
package net.algem.security;

import java.util.List;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import net.algem.group.Group;
import org.hibernate.validator.constraints.Email;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.4.0
 * @since 1.0.0 11/02/13
 */
@Component
//@Scope("session")
public class User
{

  @Min(value = 1, message = "{id.required}")
  @Max(value = Integer.MAX_VALUE, message = "{id.required}")
  private int id;

  /** Login user name. */
  @Size(min = 2, max = 16, message = "{login.required}")
  private String login;

  /** Login password. */
  @Size(min = 8, max = 16, message = "{password.required}")
  private String password;

  /** User role. */
  private Profile profile;

  private String firstName;

  //@NotNull
  private String name;

  /** Login email. */
  @Email(message = "{email.required}")
  private String email;

  /** Groups the user belongs to. */
  private List<Group> groups;

  /** Teacher status. */
  private boolean teacher;

  /** Student status. */
  private boolean student;

  /** Technician status. */
  private boolean tech;

  private UserPass passInfo;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Profile getProfile() {
    return profile;
  }

  public void setProfile(Profile profile) {
    this.profile = profile;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public List<Group> getGroups() {
    return groups;
  }

  public void setGroups(List<Group> groups) {
    this.groups = groups;
  }

  public boolean isTeacher() {
    return teacher;
  }

  public void setTeacher(boolean teacher) {
    this.teacher = teacher;
  }

  public boolean isStudent() {
    return student;
  }

  public void setStudent(boolean student) {
    this.student = student;
  }

  public boolean isTech() {
    return tech;
  }

  public void setTech(boolean tech) {
    this.tech = tech;
  }

  public UserPass getPass() {
    return passInfo;
  }

  public void setPass(UserPass passInfo) {
    this.passInfo = passInfo;
  }

  @Override
  public String toString() {
    String n = (name == null || name.isEmpty() ? login : name);
    return firstName == null || firstName.isEmpty() ? n : firstName + " " + n;
  }

}
