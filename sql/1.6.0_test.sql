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
