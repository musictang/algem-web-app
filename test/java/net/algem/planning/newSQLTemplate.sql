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