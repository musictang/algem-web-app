/*
 * @(#) newSQLTemplate.sql Algem Web App 1.6.0 06/10/2017
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
/**
 * Author:  <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * Created: 6 oct. 2017
 */

SELECT l.idper,l.login,l.profil,per.nom,per.prenom,coalesce(prof.actif, false),coalesce(e.idper,0),coalesce(tech.idper,0)
FROM login l INNER JOIN personne per ON (l.idper = per.id)
LEFT OUTER JOIN prof ON (l.idper = prof.idper)
LEFT OUTER JOIN eleve e ON (l.idper = e.idper)
LEFT OUTER JOIN technicien tech ON (l.idper = tech.idper)
WHERE trim(l.login) = 'jm';