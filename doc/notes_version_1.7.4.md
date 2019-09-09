---
lang: fr-FR
title: Notes de version Algem-Web-App 1.7.4
...

# 1.7.4

## RÉCUPÉRATION DU MOT DE PASSE

### CORRECTIFS
La récupération du mot de passe posait deux problèmes :

* Il était difficile de récupérer un mot de passe lorsqu'une adresse email était partagée entre plusieurs personnes, notamment dans le cas des enfants dont l'adresse email est encore celle des parents.
* Bien qu'ayant récupéré leur mot de passe, les personnes ne pouvaient toujours pas se connecter car elles avaient oublié aussi leur **login**.

Cette version tente de pallier à ces deux inconvénients :

* En imposant d'une part la saisie du **numéro d'adhérent** dans la fenêtre de récupération, afin de déterminer sans ambiguïté la personne qui fait la demande.
* **En rappelant à la personne son login** dans le mail de récupération.

## DURÉE DE CONNEXION
La **durée de connexion** par défaut est maintenant de **45 minutes**. Il fallait trouver un compromis entre un temps de connexion assez long pour que les professeurs et élèves mènent à bien leur activité et un temps assez court pour que les professeurs n'intervertissent pas leur compte lorsque le poste de travail est partagé entre plusieurs personnes.
