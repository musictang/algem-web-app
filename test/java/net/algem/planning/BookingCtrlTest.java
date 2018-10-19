/*
 * @(#) BookingCtrlTest.java Algem Web App 1.7.3 13/02/18
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

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 */
public class BookingCtrlTest {

  public BookingCtrlTest() {
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

  /**
   * Test of doXPostBooking method, of class BookingCtrl.
   */
  @Ignore
  public void testDoXPostBooking() {
    System.out.println("doXPostBooking");

//    Principal p = null;
//    BookingCtrl instance = new BookingCtrl();
//    instance.doXPostBooking(p);
  }

  /**
   * Test if booking length is between {@value net.algem.planning.BookingCtrl.BookingValidator#MIN_TIME_LENGTH} and {@value net.algem.planning.BookingCtrl.BookingValidator#MAX_TIME_LENGTH} values.
   *
   */
  @Test
  public void testIsValidTimeLength() {
    BookingCtrl ctrl = new BookingCtrl();
    BookingCtrl.BookingValidator validator = ctrl.new BookingValidator();
    assertFalse(validator.isValidTimeLength(0.2f));
    assertFalse(validator.isValidTimeLength(8.5f));
    assertTrue(validator.isValidTimeLength(0.5f));
    assertTrue(validator.isValidTimeLength(4.5f));
    assertTrue(validator.isValidTimeLength(7.9f));
    assertTrue(validator.isValidTimeLength(8f));

  }

  /**
   * Test if a booking is after a minimal delay in hours.
   */
  @Test
  public void testIsValidMinBookingDate() {
    BookingCtrl ctrl = new BookingCtrl();
    BookingCtrl.BookingValidator validator = ctrl.new BookingValidator();
    int min = 2; // 2 hours
    Date now = new Date();
    Calendar cal = Calendar.getInstance();
    cal.setTime(now);
    cal.add(Calendar.HOUR_OF_DAY, min);
    Date d = cal.getTime();
    System.out.println("min now = " + now.toString() + " d = " + d.toString());
    assertTrue("now = " + now.toString() + " d = " + d.toString(),validator.isValidMinBookingDate(d, min));
    cal.setTime(now);
    cal.add(Calendar.HOUR_OF_DAY, min-1);
    d = cal.getTime();
    System.out.println("min now = " + now.toString() + " d = " + d.toString());
    assertFalse("now = " + now.toString() + " d = " + d.toString(),validator.isValidMinBookingDate(d, min));
    cal.setTime(now);
    cal.add(Calendar.HOUR_OF_DAY, 1);
    d = cal.getTime();
    System.out.println("min now = " + now.toString() + " d = " + d.toString());
    assertFalse("now = " + now.toString() + " d = " + d.toString(),validator.isValidMinBookingDate(d, min));
    cal.setTime(now);

    cal.add(Calendar.HOUR_OF_DAY, min +1);

    d = cal.getTime();
    System.out.println("min now = " + now.toString() + " d = " + d.toString());
    assertTrue("now = " + now.toString() + " d = " + d.toString(),validator.isValidMinBookingDate(d, min));
  }

  /**
   * Test if booking is before some date from now.
   */
  @Test
  public void testIsValidMaxBookingDate() {
    BookingCtrl ctrl = new BookingCtrl();
    BookingCtrl.BookingValidator validator = ctrl.new BookingValidator();
    int max = 10; // 10 days
    Date now = new Date();
    Calendar cal = Calendar.getInstance();
    cal.setTime(now);
    cal.add(Calendar.DATE, 2);
    Date d = cal.getTime();
    System.out.println("now = " + now.toString() + " d = " + d.toString());
    assertTrue("now = " + now.toString() + " d = " + d.toString(),validator.isValidMaxBookingDate(d, max));
    cal.setTime(now);
    cal.add(Calendar.DATE, max);
    d = cal.getTime();
    System.out.println("now = " + now.toString() + " d = " + d.toString());
    assertTrue("now = " + now.toString() + " d = " + d.toString(),validator.isValidMaxBookingDate(d, max));
    cal.setTime(now);

    cal.add(Calendar.DATE, max);
    cal.add(Calendar.MINUTE, 1);
    d = cal.getTime();
    System.out.println("now = " + now.toString() + " d = " + d.toString());
    assertFalse("now = " + now.toString() + " d = " + d.toString(),validator.isValidMaxBookingDate(d, max));
  }

  /**
   * Test if booking between or equal opening and closing times.
   */
  @Test
  public void testIsValidBookingTimes() {
    DailyTimes dt = new DailyTimes(1);
    dt.setOpening(new Hour("14:00"));
    dt.setClosing(new Hour("20:00"));
    BookingCtrl ctrl = new BookingCtrl();
    BookingCtrl.BookingValidator validator = ctrl.new BookingValidator();
    Date now = new Date();
    Calendar cal = Calendar.getInstance();
    cal.setTime(now);
    cal.set(Calendar.DAY_OF_WEEK, 1);

    Date d = cal.getTime();
    Booking b = new Booking();
    b.setStartTime(new Hour("13:59"));
    b.setEndTime(new Hour("16:59"));
    assertNotNull(validator.isValidBookingTimes(b, dt, buildMessageSource()));

    b.setStartTime(new Hour("14:00"));
    b.setEndTime(new Hour("16:00"));
    assertNull(validator.isValidBookingTimes(b, dt, buildMessageSource()));

    b.setEndTime(new Hour("20:00"));
    assertNull(validator.isValidBookingTimes(b, dt, buildMessageSource()));

    b.setStartTime(new Hour("15:00"));
    b.setEndTime(new Hour("16:00"));
    assertNull(validator.isValidBookingTimes(b, dt, buildMessageSource()));

    b.setEndTime(new Hour("20:01"));
    assertNotNull(validator.isValidBookingTimes(b, dt, buildMessageSource()));

  }

  private MessageSource buildMessageSource() {
    return new MessageSource() {
      @Override
      public String getMessage(String string, Object[] os, String string1, Locale locale) {
        return("error"); //To change body of generated methods, choose Tools | Templates.
      }

      @Override
      public String getMessage(String string, Object[] os, Locale locale) throws NoSuchMessageException {
        return("error"); //To change body of generated methods, choose Tools | Templates.
      }

      @Override
      public String getMessage(MessageSourceResolvable msr, Locale locale) throws NoSuchMessageException {
        return("error"); //To change body of generated methods, choose Tools | Templates.
      }
    };
  }

}
