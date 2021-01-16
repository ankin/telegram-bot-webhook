CREATE TABLE IF NOT EXISTS reminder
(
    id SERIAL PRIMARY KEY,
    remindAt date NOT NULL,
    description TEXT NOT NULL,
    chatId INTEGER NOT NULL,
    createdBy INTEGER NOT NULL,
    createdAt TIMESTAMP NOT NULL,
    executedAt TIMESTAMP NULL
);