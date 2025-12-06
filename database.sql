CREATE TABLE IF NOT EXISTS public.users (
                                            id          SERIAL PRIMARY KEY,
                                            name        VARCHAR(64) NOT NULL,
    surname     VARCHAR(64) NOT NULL,
    email       VARCHAR(128) NOT NULL UNIQUE,
    password    VARCHAR(128) NOT NULL,
    protocol    VARCHAR(16)  NOT NULL,  -- IMAP / POP3
    host        VARCHAR(128),
    port        VARCHAR(16)
    );

CREATE TABLE IF NOT EXISTS public.folders (
                                              id         SERIAL PRIMARY KEY,
                                              title      VARCHAR(64) NOT NULL,
    color      VARCHAR(16) NOT NULL,
    icon_path  VARCHAR(255),
    user_id    INTEGER REFERENCES public.users(id)
    );

CREATE TABLE IF NOT EXISTS public.tags (
                                           id       SERIAL PRIMARY KEY,
                                           title    VARCHAR(64) NOT NULL,
    color    VARCHAR(16) NOT NULL,
    user_id  INTEGER REFERENCES public.users(id)
    );

CREATE TABLE IF NOT EXISTS public.mails (
                                            id          SERIAL PRIMARY KEY,
                                            subject     VARCHAR(255) NOT NULL,
    mail_id     INTEGER NOT NULL,
    text        TEXT NOT NULL,
    received_at TIMESTAMP,
    is_read     BOOLEAN DEFAULT FALSE,
    is_starred  BOOLEAN DEFAULT FALSE,
    is_draft    BOOLEAN DEFAULT FALSE,
    from_id     INTEGER REFERENCES public.users(id),
    to_id       INTEGER REFERENCES public.users(id)
    );

CREATE TABLE IF NOT EXISTS public.mail_folders (
                                                   folder_id INTEGER NOT NULL REFERENCES public.folders(id),
    mail_id   INTEGER NOT NULL REFERENCES public.mails(id),
    PRIMARY KEY (folder_id, mail_id)
    );

CREATE TABLE IF NOT EXISTS public.mail_tags (
                                                tag_id  INTEGER NOT NULL REFERENCES public.tags(id),
    mail_id INTEGER NOT NULL REFERENCES public.mails(id),
    PRIMARY KEY (tag_id, mail_id)
    );