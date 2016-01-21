SELECT g.id,g.nom FROM groupe g
where g.id in (select gd.id from groupe_det gd join login l on l.idper = gd.musicien and l.login = 'jm');