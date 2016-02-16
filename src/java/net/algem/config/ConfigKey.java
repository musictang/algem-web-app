/*
 * @(#) ConfigKey.java Algem Web App 1.1.0 31/01/2016
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

package net.algem.config;

/**
 * Configuration keys enumeration.
 * First arg represents key in the table, second arg represents the label.
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.1.0
 * @since 1.1.0 31/01/2016
 */
public enum ConfigKey {

  /** Organization name. *//** Organization name. *//** Organization name. *//** Organization name. */
  ORGANIZATION_NAME("Organisation.Nom"),

  /** Organization first address. */
  ORGANIZATION_ADDRESS1("Organisation.Adresse1"),

  /** Organization second address. */
  ORGANIZATION_ADDRESS2("Organisation.Adresse2"),

  /** Organization postal code. */
  ORGANIZATION_ZIPCODE("Organisation.Cdp"),

  /** Organization city. */
  ORGANIZATION_CITY("Organisation.Ville"),


  /** Start of school year. */
  START_OF_YEAR("Date.DebutAnnee"),

  /** End of school year. */
  END_OF_YEAR("Date.FinAnnee"),

  /** Start of period. */
  BEGINNING_PERIOD("Date.DebutPeriode"),

  /** End of period. */
  END_PERIOD("Date.FinPeriode"),

  /** End peak hour - start full rate for rehearsals. */
  OFFPEAK_HOUR("FinHeureCreuse"),

  /** Default start time. */
  START_TIME("Heure.ouverture"),

  /** Teacher management. */
  TEACHER_MANAGEMENT("GestionProf"),

  /** Course management. */
  COURSE_MANAGEMENT("GestionCours"),

  /** Workshop management. */
  WORKSHOP_MANAGEMENT("GestionAtelier"),

  /** Default establishment. */
  DEFAULT_ESTABLISHMENT("Etablissement.par.defaut"),

  ORGANIZATION_DOMAIN("Organisation.domaine"),

  SCHEDULE_RANGE_NAMES("Afficher.noms.plages"),

  PERSON_SORT_ORDER("Ordre.tri.personne"),

  ADMINISTRATIVE_MANAGEMENT("Planification.administrative"),

  BOOKING_MIN_DELAY("Reservation.delai.min"),
  BOOKING_MAX_DELAY("Reservation.delai.max"),
  BOOKING_CANCEL_DELAY("Reservation.annulation.delai"),
  MEMBERSHIP_ACCOUNT("ADHÉSIONS"),
  PRO_MEMBERSHIP_ACCOUNT("ADHÉSIONS PRO");
  
  private final String key;

  ConfigKey(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }

}
