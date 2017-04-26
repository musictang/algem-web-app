SELECT g.id,g.nom FROM groupe g
where g.id in (select gd.id from groupe_det gd join login l on l.idper = gd.musicien and l.login = 'jm');

SELECT p.id,p.jour,p.debut,p.fin,p.ptype,s.nom,e.nom
FROM planning p JOIN salle s ON(p.lieux = s.id)
JOIN personne e ON (e.id = s.etablissement)
WHERE p.jour >= '28-01-2016'
AND (p.ptype = 14 AND p.idper = 16094)
OR (p.ptype = 13 AND p.idper IN (
SELECT g.id FROM groupe g JOIN groupe_det d ON (g.id = d.id)
WHERE d.musicien = 16094)) ORDER BY p.jour;

select p.id,p.jour,p.debut,p.fin,p.ptype,s.nom,e.nom,case when p.ptype = 13 then g.nom else 'Individuelle' end
FROM planning p JOIN reservation r ON (p.action = r.idaction) JOIN salle s ON(p.lieux = s.id)
JOIN personne e ON (e.id = s.etablissement)
LEFT JOIN groupe g ON (p.idper = g.id)
WHERE p.jour >= '28-01-2016'
AND r.idper = 16094;

SELECT e.paye FROM echeancier2 e JOIN login l ON (e.adherent = l.idper)
WHERE l.login = 'duchatel' AND e.echeance BETWEEN '21-09-2015' AND ',30-06-2016' AND e.compte IN(17,14) LIMIT 1;

oid        | 37597
echeance   | 2015-09-07
payeur     | 13857
adherent   | 13857
commande   | 0
reglement  | ESP
libelle    | p13857 a13858
montant    | 1000
piece      | L1526
ecole      | 0
compte     | 17
paye       | t
transfert  | t
monnaie    | E
analytique | MFORLOISIR
facture    | [NULL]
groupe     | 0

insert into echeancier2 values(default,'2015-09-01',13857,13857,0,'ESP','p13857 a13858 test',1000,'',0,17,true,false,'E','MFORLOISIR',null,0);

-- recherche plages semaine
SELECT p.id,p.jour,p.ptype,p.idper,p.action,p.lieux,p.note, c.id, c.titre, c.collectif, c.code, s.nom, t.prenom, t.nom, pl.debut,pl.fin
FROM planning p INNER JOIN action a LEFT OUTER JOIN cours c ON (a.cours = c.id)
ON (p.action = a.id) LEFT OUTER JOIN personne t ON (t.id = p.idper), salle s,plage pl
where p.lieux = s.id
and p.id = pl.idplanning
AND pl.adherent = 21611
AND jour BETWEEN '04-04-2016' AND '10-04-2016'
ORDER BY p.jour, p.debut;

SELECT p.id,p.jour,p.ptype,p.idper,p.action,p.lieux,p.note
FROM planning p INNER JOIN salle s ON (p.lieux = s.id)
where (p.ptype = 3 or p.ptype = 4)
and p.idper = 21611
AND p.jour BETWEEN '04-04-2016' AND '10-04-2016'
ORDER BY p.jour, p.debut;

-- planning hebdo répets, reservations
SELECT p.id,p.jour,p.debut,p.fin,p.ptype,p.idper,p.action,p.lieux,p.note,s.nom
FROM planning p INNER JOIN salle s ON (p.lieux = s.id)
WHERE p.jour BETWEEN '04-04-2016' AND '10-04-2016'
AND (((p.ptype = 4 OR p.ptype = 14) AND p.idper = 21611)
OR ((p.ptype = 3 OR p.ptype = 13) AND p.idper IN (SELECT id FROM groupe_det WHERE musicien = 21611)))
ORDER BY p.jour, p.debut;

-- planning hebdo salarie
SELECT p.id,p.jour,p.debut,p.fin,p.ptype,p.idper,p.action,p.lieux,p.note, c.id, c.titre, c.collectif, c.code, s.nom, t.prenom, t.nom
FROM planning p INNER JOIN action a LEFT OUTER JOIN cours c ON (a.cours = c.id)
ON (p.action = a.id) LEFT OUTER JOIN personne t ON (t.id = p.idper) JOIN salle s ON (p.lieux = s.id)
WHERE p.idper = 16094
AND p.ptype IN (1,3,4,9)
AND jour BETWEEN '04-04-2016' AND '10-04-2016'
ORDER BY p.jour, p.debut;

SELECT p.id,p.jour,p.debut,p.fin,p.ptype,pl.adherent,p.action,p.lieux,p.note, s.nom, t.prenom, t.nom
FROM planning p INNER JOIN plage pl ON (p.id = pl.idplanning) LEFT OUTER JOIN personne t ON (pl.adherent = t.id)
INNER JOIN salle s ON (p.lieux = s.id)
WHERE p.ptype = 8
AND pl.adherent = 16094
AND p.jour BETWEEN '04-04-2016' AND '10-04-2016'
ORDER BY p.jour, p.debut;

SELECT DISTINCT ON (p.jour,pl.debut) p.id,p.jour,pl.debut,pl.fin,a.cours,s1.id,s1.texte AS Suivi ind.,s2.id,s2.texte AS Suivi co.
FROM planning p JOIN plage pl ON p.id=pl.idplanning
JOIN actiON a ON p.actiON = a.id
JOIN cours c ON a.cours = c.id
LEFT JOIN suivi s1 ON (pl.note = s1.id)
LEFT JOIN suivi s2 ON (p.note = s2.id)
WHERE jour BETWEEN '06-06-2016' AND '12-06-2016'
AND p.idper = 12019
ORDER BY p.jour,pl.debut;

SELECT DISTINCT ON (p.jour,pl.debut) p.id,p.jour,pl.debut,pl.fin,c.id,c.id,c.titre,c.collectif,s.nom
FROM planning p JOIN plage pl ON (p.id = pl.idplanning)
JOIN action a ON p.action = a.id
JOIN cours c ON a.cours = c.id
JOIN salle s ON p.lieux = s.id
WHERE jour BETWEEN '06-06-2016' AND '12-06-2016'
AND p.idper = 12019
AND p.ptype IN (1,5,6)
ORDER BY p.jour,pl.debut;

SELECT DISTINCT ON (p.jour,pl.debut) p.id,p.jour,pl.debut,pl.fin,p.lieux,p.note,c.id,c.titre,c.collectif,s.nom
FROM planning p JOIN plage pl ON (p.id = pl.idplanning) JOIN action a ON (p.action = a.id) JOIN cours c ON (a.cours = c.id) JOIN salle s ON (p.lieux = s.id)
WHERE jour BETWEEN '06-06-2016' AND '12-06-2016' AND p.idper = 12019 ORDER BY p.jour,pl.debut;


SELECT DISTINCT ON (p.jour,pl.debut) p.id,p.jour,pl.debut,pl.fin,p.lieux,p.note,c.id,c.titre,c.collectif,s.nom
FROM planning p
JOIN plage pl ON (p.id = pl.idplanning)
 JOIN action a ON (p.action = a.id)
JOIN cours c ON (a.cours = c.id)
JOIN salle s ON (p.lieux = s.id)
WHERE jour BETWEEN '06-06-2016' AND '12-06-2016' AND p.idper = 12019 ORDER BY p.jour,pl.debut;

SELECT DISTINCT ON (p.jour,pl.debut) p.id,p.jour,pl.debut,pl.fin,p.lieux,p.note,c.id,c.titre,c.collectif,s.nom,v.texte
FROM planning p
JOIN plage pl ON (p.id = pl.idplanning)
JOIN action a ON (p.action = a.id)
 JOIN cours c ON (a.cours = c.id)
JOIN salle s ON (p.lieux = s.id)
JOIN suivi v ON (p.note = v.id)
WHERE p.idper = 12019
AND jour BETWEEN '06-06-2016' AND '12-06-2016'
ORDER BY p.jour,pl.debut;

SELECT pl.id,pl.debut,pl.fin,pl.adherent,pl.note,p.nom,p.prenom,p.pseudo,s.id,s.texte
FROM plage pl JOIN personne p ON (pl.adherent = p.id) LEFT JOIN suivi s ON (pl.note = s.id)
WHERE pl.idplanning = 96023 and pl.debut = '15:15'
ORDER BY pl.debut;

SELECT l.idper,l.login,l.profil,per.nom,per.prenom,coalesce(prof.actif, false),coalesce(tech.idper,0),coalesce(e.idper,0)
FROM  login  l INNER JOIN  personne  per ON (l.idper = per.id)
LEFT OUTER JOIN prof ON (l.idper = prof.idper)
LEFT OUTER JOIN technicien tech ON (l.idper = tech.idper)
LEFT OUTER JOIN eleve e ON (l.idper = e.idper)
WHERE l.login = 'laurence';


-- suivi eleve
SELECT p.jour, pl.debut,pl.fin, p.idper, per.nom,per.prenom,p.lieux,s.nom,n1.texte, n2.texte
FROM planning p
JOIN personne per on p.idper = per.id
JOIN plage pl ON p.id = pl.idplanning
JOIN salle s ON p.lieux = s.id
LEFT JOIN suivi n1 ON pl.note = n1.id
LEFT JOIN suivi n2 ON p.note = n2.id
WHERE p.ptype IN (1,5,6)
AND pl.adherent = 13858
AND p.jour BETWEEN '01-06-2016' AND '30-06-2016' ORDER BY p.jour,pl.debut;

SELECT p.id,p.jour,pl.id,pl.debut,pl.fin,p.idper,per.nom,per.prenom,p.lieux,s.nom,c.id,c.titre,n1.id,n1.texte,n1.note,n1.abs,n1.exc,n2.id,n2.texte,n2.abs
FROM planning p JOIN personne per on p.idper = per.id
JOIN action a ON p.action = a.id
JOIN cours c ON a.cours = c.id
JOIN plage pl ON p.id = pl.idplanning
JOIN salle s ON p.lieux = s.id
LEFT JOIN suivi n1 ON pl.note = n1.id
LEFT JOIN suivi n2 ON p.note = n2.id
WHERE p.ptype IN (1,5,6)
AND pl.adherent = 13858
AND p.jour BETWEEN '06-06-2016' AND '12-06-2016'
ORDER BY p.jour,pl.debut;

SELECT p.id,p.jour,p.debut,p.fin,p.ptype,p.idper,p.lieux,s.nom FROM planning p JOIN salle s ON(p.lieux = s.id)
WHERE p.jour = '2016-09-06'
AND p.idper = 16094
AND ((p.debut >= '23:00:00' AND p.debut < '24:00:00')-- // start //end
OR (p.fin > '23:00:00' AND p.fin <= '24:00:00')
OR (p.debut <= '23:00:00' AND p.fin >= '24:00:00'));

-- planning du jour
SELECT p.*, c.id, c.titre, c.collectif, c.code, s.nom, per.prenom, per.nom, g.nom
FROM planning p INNER JOIN action a ON (p.action = a.id) LEFT OUTER JOIN cours c ON (a.cours = c.id)
LEFT OUTER JOIN personne per ON (p.idper = per.id)
LEFT OUTER JOIN groupe g ON (p.idper = g.id)
JOIN salle s ON (p.lieux = s.id)
WHERE jour = '2016-10-12'
AND s.etablissement = 3501
AND s.public = TRUE
ORDER BY s.nom, p.debut;

-- etablissements
SELECT DISTINCT p.id, p.nom FROM personne p
JOIN etablissement e ON (p.id = e.id)
WHERE p.ptype = 5
AND e.actif = TRUE;

SELECT p.id,p.nom FROM personne p WHERE p.ptype = 5 AND p.id IN (SELECT DISTINCT etablissement FROM salle WHERE public = TRUE)

SELECT p.*, c.id, c.titre, c.collectif, c.code, s.nom, per.prenom, per.nom, g.nom
FROM planning p INNER JOIN action a ON (p.action = a.id) LEFT OUTER JOIN cours c ON (a.cours = c.id)
LEFT OUTER JOIN personne per ON (p.idper = per.id)
LEFT OUTER JOIN groupe g ON (p.idper = g.id)
JOIN salle s ON (p.lieux = s.id)
WHERE jour = '2016-10-12'
AND s.etablissement = 3501
ORDER BY s.nom, p.debut;
--       if (!adminAccess) {query +=  AND s.public = TRUE;}
--       query +=

SELECT pl.id,pl.debut,pl.fin,pl.adherent,pl.note,p.nom,p.prenom,p.pseudo,s.id,s.texte,s.note,s.statut,e.email,t.numero
FROM plage pl JOIN personne p ON (pl.adherent = p.id)
LEFT JOIN suivi s ON (pl.note = s.id)
LEFT JOIN email e ON (p.id = e.idper AND e.idx = 0)
LEFT JOIN telephone t ON (p.id = t.idper AND t.idx = 0)
WHERE pl.idplanning = 105266 AND pl.jour = '05-10-2016' ORDER BY pl.debut;

-- detail planning liste des eleves
SELECT pl.id,pl.adherent,pl.debut,pl.fin,pl.note,p.nom,p.prenom,p.pseudo,i.nom
 from plage pl join personne p on (pl.adherent = p.id)
  left join person_instrument pi on (p.id = pi.idper and pi.idx = 0)
  join instrument i on (pi.instrument = i.id)
where idplanning = 104302
and pi.ptype = 1;

-- cours
SELECT pl.id,pl.adherent,pl.debut,pl.fin,pl.note,p.nom,p.prenom,p.pseudo,i.nom
FROM plage pl JOIN personne p ON (pl.adherent = p.id)
LEFT JOIN person_instrument pi ON (p.id = pi.idper AND pi.idx = 0 AND pi.ptype =  1)
JOIN instrument i ON (pi.instrument = i.id)
WHERE idplanning = 110489
ORDER BY pl.debut,p.nom,p.prenom;
-- groupe
SELECT g.id,m.musicien,per.nom,per.prenom,per.pseudo,i.nom
FROM groupe g JOIN planning p ON (g.id = p.idper)
JOIN groupe_det m ON (g.id = m.id)
JOIN personne per ON (m.musicien = per.id)
LEFT JOIN person_instrument pi ON (m.musicien = pi.idper AND pi.idx = 0 AND pi.ptype =  3)
JOIN instrument i ON (pi.instrument = i.id)
WHERE p.id = 110499
ORDER BY per.nom,per.prenom;
--adherent
-- 110491
-- plages élève (planning jardin musical - eleves sans instrument)
SELECT DISTINCT pl.id,pl.adherent,pl.debut,pl.fin,pl.note,p.nom,p.prenom,p.pseudo,i.id, i.nom
FROM plage pl JOIN personne p ON (pl.adherent = p.id)
LEFT JOIN person_instrument pi ON (p.id = pi.idper AND pi.idx = 0
AND pi.ptype =  1 )
LEFT JOIN instrument i ON (pi.instrument = i.id)
WHERE idplanning = 105073
ORDER BY pl.debut,p.nom,p.prenom;

-- bookings pagination test
SELECT r.id,p.action,p.idper,p.jour,p.debut,p.fin,p.ptype,p.lieux,s.nom,e.id,e.nom,
CASE WHEN (p.ptype = 13 OR p.ptype = 14) THEN g.nom ELSE '' END ,r.statut
FROM planning p JOIN reservation r ON (p.action = r.idaction) JOIN salle s ON(p.lieux = s.id)
JOIN personne e ON (e.id = s.etablissement)
LEFT JOIN groupe g ON (p.idper = g.id)
WHERE r.id < 2147483647 AND r.idper = 1199 ORDER BY r.id DESC LIMIT 10;

