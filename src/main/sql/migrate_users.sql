USE biomed;

CREATE TABLE IF NOT EXISTS app_user (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(200) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'patient',
    display_name  VARCHAR(100) NULL,
    created_at    DATETIME     NULL,
    last_login    DATETIME     NULL,
    active        TINYINT(1)   NOT NULL DEFAULT 1,
    INDEX idx_username (username)
);

SELECT 'User table ready.' AS status;
