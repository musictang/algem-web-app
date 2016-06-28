/*
 * @(#) TeacherIO.java Algem Web App 1.4.0 27/06/2016
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
 */
package net.algem.contact;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import net.algem.planning.DateFr;
import net.algem.planning.Hour;
import net.algem.planning.ScheduleDao;
import net.algem.planning.ScheduleElement;
import net.algem.planning.ScheduleRangeElement;
import net.algem.planning.ScheduleRangeIO;
import net.algem.util.AbstractGemDao;
import net.algem.util.NamedModel;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.4.0
 * @since 1.0.6 06/01/2016
 */
@Repository
public class TeacherIO
        extends AbstractGemDao
{

  public static final String TABLE = "prof";
  private final static String FOLLOWUP_STATEMENT = "SELECT DISTINCT ON (p.jour,pl.debut) p.id,p.jour,pl.debut,pl.fin,p.lieux,p.note,c.id,c.titre,c.collectif,s.nom"
          + " FROM " + ScheduleDao.TABLE + " p"
          + " JOIN " + ScheduleRangeIO.TABLE + " pl ON (p.id = pl.idplanning)"
          + " JOIN " + ScheduleDao.T_ACTION + " a ON (p.action = a.id)"
          + " JOIN cours c ON (a.cours = c.id)"
          + " JOIN salle s ON (p.lieux = s.id)"
          + " WHERE p.idper = ?"
          + " AND jour BETWEEN ? AND ?"
          + " ORDER BY p.jour,pl.debut";

  public List<ScheduleElement> findFollowUp(final int teacher, Date from, Date to) {
    System.out.println(FOLLOWUP_STATEMENT);
    return jdbcTemplate.query(FOLLOWUP_STATEMENT, new RowMapper<ScheduleElement>()
    {
      @Override
      public ScheduleElement mapRow(ResultSet rs, int rowNum) throws SQLException {
        ScheduleElement d = new ScheduleElement();
        d.setId(rs.getInt(1));
        d.setDateFr(new DateFr(rs.getString(2)));
        d.setStart(new Hour(rs.getString(3)));
        d.setEnd(new Hour(rs.getString(4)));
        d.setIdPerson(teacher);
        d.setPlace(rs.getInt(5));
        d.setNote(rs.getInt(6));
        d.setDetail("course", new NamedModel(rs.getInt(7), rs.getString(8)));
        d.setCollective(rs.getBoolean(9));
        d.setDetail("room", new NamedModel(d.getPlace(), rs.getString(10)));
        d.setDetail("estab", null);

        Collection<ScheduleRangeElement> ranges = getRanges(d.getId());
        d.setRanges(ranges);

        return d;
      }
    }, teacher, from, to);
  }

  private Collection<ScheduleRangeElement> getRanges(final int id) {
    String query = " SELECT pl.id,pl.debut,pl.fin,pl.adherent,pl.note,p.nom,p.prenom,p.pseudo"
            + " FROM " + ScheduleRangeIO.TABLE + " pl JOIN " + PersonIO.TABLE + " p ON (pl.adherent = p.id)"
            + " WHERE pl.idplanning = ? ORDER BY pl.debut";
    System.out.println(query);
    return jdbcTemplate.query(query, new RowMapper<ScheduleRangeElement>()
    {

      @Override
      public ScheduleRangeElement mapRow(ResultSet rs, int rowNum) throws SQLException {
        ScheduleRangeElement r = new ScheduleRangeElement();
        r.setId(rs.getInt(1));
        r.setScheduleId(id);
        r.setStart(new Hour(rs.getString(2)));
        r.setEnd(new Hour(rs.getString(3)));
        r.setMemberId(rs.getInt(4));
        r.setNote(rs.getInt(5));
        Person p = new Person(r.getMemberId());
        p.setName(rs.getString(6));
        p.setFirstName(rs.getString(7));
        p.setNickName(rs.getString(8));
        r.setPerson(p);

        return r;
      }
    }, id);
  }

}
