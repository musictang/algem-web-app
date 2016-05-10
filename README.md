# algem-web-app
Présentation
============
**Algem Web App** est une extension web au logiciel **Algem**.
Fondée sur le framework Spring, cette extension a pour objectif de doter
Algem d'une interface publique afin de pouvoir effectuer
des réservations, visualiser son planning personnel, éditer le suivi des
élèves.
Les sources du projet étaient auparavant intégrées à Algem dans le
répertoire agenda. Pour des raisons de lisibilité, nous avons préféré
créer un dépôt distinct.

Intégration
============
Algem Web App peut être déployé indépendamment du logiciel Algem mais ne
s'y substitue en aucun cas. 
Les deux applications peuvent fonctionner conjointement à condition que
la base de donnée (qui leur est commune) soit hébergée sur le même
serveur.
Dans le cas contraire, il est possible d'utiliser les mécanismes de
réplication afin de synchroniser les deux bases. Postgresql, à partir de
la version 9 répond en partie à cette problématique puisqu'il permet une
réplication maître-esclave en streaming. Deux inconvéniants cependant :
- la réplication se fait sur l'ensemble du cluster
- le serveur esclave est en lecture seule.

La configuration peut varier suivant le client. Des fichiers d'exemple
sont donnés dans le dossier samples.

context.xml -> /web/META-INF/context.xml
client.xml -> /web/WEB-INF/client.xml
sender.xml -> /web/WEB-INF/sender.xml

Le thème est paramétrable dans le fichier /web/WEB-INF/dispatcher.xml

Copyright 2015-2016 Musiques Tangentes
