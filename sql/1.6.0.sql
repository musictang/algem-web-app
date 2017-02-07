-- 1.6.0
-- documents de cours
CREATE TABLE document_action(
    id serial PRIMARY KEY,
    datedebut date NOT NULL DEFAULT CURRENT_DATE,
    idaction integer REFERENCES action(id) ON DELETE CASCADE,
    idplanning integer NOT NULL DEFAULT 0,
    idper integer NOT NULL DEFAULT 0,
    doctype smallint NOT NULL DEFAULT 0,
    nom varchar(64),
    uri varchar(2083) NOT NULL
);

COMMENT ON TABLE document_action IS 'Liens vers les documents (principalement, à destination pédagogique) : un document est lié à une ou plusieurs sessions de planning';
COMMENT ON COLUMN document_action.datedebut IS 'Date de la première session incluant le document';
COMMENT ON COLUMN document_action.idaction IS 'Rattachement au cours (toutes les séances)';
COMMENT ON COLUMN document_action.idplanning IS 'Rattachement au planning (tous les participants)';
COMMENT ON COLUMN document_action.idper IS 'Rattachement à l''élève (destinataire unique)';
COMMENT ON COLUMN document_action.doctype IS '0 = other, 1 = music, 2 = audio, 3 = movie';
COMMENT ON COLUMN document_action.uri IS 'IE limit = 2083';

ALTER TABLE document_action OWNER TO nobody;

  --1 | 2017-02-02 |    78938 |          0 |     0 | Document rattaché au planning |       1 | http://www.algem.net
  --2 | 2017-02-01 |    78938 |          0 | 20889 | Document rattaché à l'élève   |       2 | http://www.algem.net
  --3 | 2017-01-30 |    78904 |          0 | 18584 | Test suivi document           |       2 | http://www.algem.net/
  --4 | 2017-01-30 |    78903 |          0 |     0 | Test suivi document collectif |       3 | http://www.algem.net/
  --5 | 2017-01-30 |    78903 |          0 |     0 | Deuxième document collectif   |       1 | http://www.musiques-tangentes.asso.fr/
  --6 | 2017-01-30 |    78899 |          0 | 13858 | Premier document individuel   |       1 | http://vps329298.ovh.net/nextcloud/index.php/s/TbRx24Mf10DCbKT
INSERT INTO document_action VALUES(DEFAULT,'2017-02-02',78938,0,0,1,'Document rattaché au planning','http://www.algem.net');
INSERT INTO document_action VALUES(DEFAULT,'2017-02-02',78938,0,20889,2,'Document rattaché à l''élève','http://www.algem.net');
INSERT INTO document_action VALUES(DEFAULT,'2017-02-02',78904,0,18584,2,'Test suivi document','http://www.algem.net');
INSERT INTO document_action VALUES(DEFAULT,'2017-02-02',78903,0,0,3,'Test suivi document collectif','http://www.algem.net');
INSERT INTO document_action VALUES(DEFAULT,'2017-02-02',78903,0,0,1,'Deuxième document collectif','http://www.musiques-tangentes.asso.fr/');
INSERT INTO document_action VALUES(DEFAULT,'2017-02-02',78899,0,13858,1,'Premier document individuel','http://vps329298.ovh.net/nextcloud/index.php/s/TbRx24Mf10DCbKT');
