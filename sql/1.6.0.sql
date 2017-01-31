-- 1.6.0
-- documents de cours
CREATE TABLE document_cours(
    id serial PRIMARY KEY,
    idplanning integer REFERENCES planning(id),
    nom varchar(64),
    uri varchar(2083) NOT NULL
);
