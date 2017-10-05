/*
 * @(#) TeacherService.java Algem Web App 1.7.0 04/10/17
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

import java.util.Date;
import java.util.List;
import net.algem.planning.FollowUp;
import net.algem.planning.ActionDocument;
import net.algem.planning.ScheduleElement;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.7.0
 * @since 1.4.0 21/06/2016
 */
public interface TeacherService
{

  List<ScheduleElement> getFollowUpSchedules(int teacher, Date from, Date to);

  FollowUp getFollowUp(int id);

  int updateFollowUp(FollowUp up);

  int updateAbsenceStatus(FollowUp up);

  ActionDocument getDocument(int docId);

  int createDocument(ActionDocument doc);

  void updateDocument(ActionDocument doc);

  void removeDocument(int docId);

}
