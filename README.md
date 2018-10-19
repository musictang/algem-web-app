# ALGEM-WEB-APP
## Licence
```
/*
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
 *
 */
```
## Présentation
**Algem Web App** est une extension web au logiciel **Algem**.
Fondée sur le framework Spring, cette extension a pour objectif de doter
Algem d'une interface publique afin de pouvoir effectuer
des réservations, visualiser son planning personnel, éditer le suivi des
élèves.

## Intégration
Algem Web App peut être déployé indépendamment du logiciel Algem mais ne
s'y substitue en aucun cas.\
Les deux applications peuvent fonctionner conjointement à condition que
la base de donnée (qui leur est commune) soit hébergée sur le même
groupe de serveurs.

La configuration peut varier suivant le client. Des fichiers d'exemple
sont donnés dans le dossier samples.
```
context.xml -> /web/META-INF/context.xml
client.xml -> /web/WEB-INF/client.xml
sender.xml -> /web/WEB-INF/sender.xml
```
Le thème est paramétrable dans le fichier `/web/WEB-INF/dispatcher.xml`

