/*
 * @(#) TeacherCtrlTest.java Algem Web App 1.6.0 15/02/2017
 *
 * Copyright (c) 2015-2017 Musiques Tangentes. All Rights Reserved.
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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @since 1.6.0 15/02/17
 */
public class TeacherCtrlTest {

  public TeacherCtrlTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

   @Test
   public void testUri() {
     TeacherCtrl ctrl = new TeacherCtrl();
     String uri = "http://www.algem.net";
     assertTrue(ctrl.isValidURI(uri));

     uri = "http://algem.net";
     assertTrue(ctrl.isValidURI(uri));

     uri="www.algem.net";
     assertTrue(ctrl.isValidURI(uri));

     uri="file://C:\\Users\\jm\\fichier.txt";
     assertTrue(ctrl.isValidURI(uri));

     uri="sftp://jm@mustang:2422:/home/jm/";
     assertTrue(ctrl.isValidURI(uri));

     uri = "htp://algem.net";
     assertFalse(ctrl.isValidURI(uri));

     uri = "http://\"algem.net\"";
     assertFalse(ctrl.isValidURI(uri));

     uri = "http://<script>console.log('bad');</script>";
     assertFalse(ctrl.isValidURI(uri));

     uri = "http://algem.net?test=alert('hello')";
     assertFalse(ctrl.isValidURI(uri));

     uri = "http://algem.net#alert";
     assertTrue(ctrl.isValidURI(uri));

     uri = "http://algem.net?test=javascript:console.log('hello');";
     assertFalse(ctrl.isValidURI(uri));
   }
}
