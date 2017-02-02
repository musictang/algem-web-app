-- 1.6.0
-- documents de cours
CREATE TABLE document_action(
    id serial PRIMARY KEY,
    idaction integer REFERENCES action(id), -- toutes séances
    idplanning integer NOT NULL DEFAULT 0, -- planning spécifique
    idplage integer NOT NULL DEFAULT 0, -- seance individuelle
    nom varchar(64),
    doctype smallint NOT NULL DEFAULT 0,
    uri varchar(2083) NOT NULL
);
COMMENT ON COLUMN document_action.doctype IS '0 = other, 1 = music, 2 = audio, 3 = movie';
