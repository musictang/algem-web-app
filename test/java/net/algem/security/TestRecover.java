/*
 * @(#) TestRecover.java Algem Web App 1.0.6 15/01/2016
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
package net.algem.security;

import java.util.Calendar;
import java.util.Date;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 */
public class TestRecover
{
  
  public TestRecover() {
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
   public void testExpirationDate() {
     Calendar cal = Calendar.getInstance();
     
     Date now = new Date();//15-01-2016
     
     // saved token date
     Date expired = new Date(now.getTime());//15-01-2016
     cal.setTime(expired);
     cal.add(Calendar.DAY_OF_MONTH, 1);//16-01-2016
     // saved token date + 1 day
     expired = cal.getTime();
     assertFalse("now = " + now + " cal = " + cal.getTime(), now.after(expired));
     cal.setTime(now);
     cal.add(Calendar.DAY_OF_MONTH, 1);//16-01-2016
     now = cal.getTime();
     assertFalse("now = " + now + " cal = " + cal.getTime(), now.after(expired));

     cal.add(Calendar.DAY_OF_MONTH, 1);//17-01-2016
     now = cal.getTime();
     assertTrue("now = " + now + " cal = " + cal.getTime(), now.after(expired));
     
   }
}
