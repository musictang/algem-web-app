/*
 * @(#) TestUserService.java Algem Web App 1.7.4 19/10/2018
 *
 * Copyright (c) 2015-2018 Musiques Tangentes. All Rights Reserved.
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
package net.algem.security;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.ContextConfiguration;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.7.4
 */
@RunWith(org.mockito.runners.MockitoJUnitRunner.class)
@ContextConfiguration
public class TestUserService {

  @Mock
  private UserDao userDao;

  @Autowired
  @InjectMocks
  private CommonUserService userService;

  public TestUserService() {
  }

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void userShouldBeAuthenticated() {
    String email = "test@localhost.local";
    int id = 1234;
    User authenticated = new User();
    authenticated.setId(id);
    authenticated.setEmail(email);
    authenticated.setName("TEST");
    authenticated.setFirstName("My");
    authenticated.setLogin("mytest");
    authenticated.setProfile(Profile.Member);

    when(userDao.findAuthenticated(email, id)).thenReturn(authenticated);

    User found = userService.findAuthenticatedUser(email, id);

    assertEquals(authenticated, found);
    assertTrue(found.getId() == id);
    assertNotNull(found.getEmail());
    assertNotNull(found.getLogin());
    assertNotNull(found.getName());

  }

  @Test
  public void userShouldNotBeAuthenticated() {
    String email = "hacker@localhost.local";
    int id = 1234;
    when(userDao.findAuthenticated(email, id)).thenReturn(null);

    User found = userService.findAuthenticatedUser(email, id);
    assertNull(found);

  }

  @Rule
  public ExpectedException exceptionRule = ExpectedException.none();

  @Test
  public void exceptionShouldBeThrownWhenUserIsNotFound() {
    String email = "hacker@localhost.local";
    int id = 1234;
    when(userDao.findAuthenticated(email, id)).thenThrow(new DataAccessException("") {
    });

    exceptionRule.expect(DataAccessException.class);
    userDao.findAuthenticated(email, id);

  }

  @Test
  public void verifyBeanAConfigured() {
    assertNotNull(userService);
  }

  @Configuration
  static class Config {

    @Bean
    UserService userService() {
      return new CommonUserService();
    }

    @Bean
    UserDao userDao() {
      return new UserDaoImpl();
    }
  }

}
