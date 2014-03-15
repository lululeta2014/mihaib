PRAGMA foreign_keys = on;

BEGIN TRANSACTION;

CREATE TABLE IF NOT EXISTS retrievers (
	ID	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
	name	TEXT NOT NULL UNIQUE
		COLLATE NOCASE
		CHECK (length(trim(name)) > 0),
	private_data
		BLOB
);

CREATE INDEX IF NOT EXISTS retrievers_name_index ON retrievers(name);

CREATE TABLE IF NOT EXISTS articles (
	ID	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
	source	INTEGER NOT NULL,
	title	TEXT NOT NULL
		COLLATE NOCASE
		CHECK (length(trim(title)) > 0),
	content	TEXT NOT NULL
		COLLATE NOCASE
		CHECK (length(trim(content)) > 0),

	FOREIGN KEY (source) REFERENCES retrievers(ID)
		ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE INDEX IF NOT EXISTS articles_source ON articles(source);

CREATE TABLE IF NOT EXISTS times (
	name	TEXT NOT NULL UNIQUE
		COLLATE NOCASE
		CHECK (length(trim(name)) > 0),
	moment	TEXT NOT NULL
		CHECK (DATETIME(moment) NOT NULL)
);

COMMIT;
