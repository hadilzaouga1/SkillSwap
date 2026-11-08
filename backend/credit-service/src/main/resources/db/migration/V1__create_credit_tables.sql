-- =====================================================================
-- Crédits-temps (1 heure = 1 crédit). Montants ENTIERS (durées en heures entières).
-- Les CHECK sont un filet de sécurité de la base : même en cas de bug, il est
-- impossible d'avoir un solde négatif ou de réserver plus que le solde (ENF2).
-- =====================================================================

-- Un compte par utilisateur (user_id vient de identity-service : pas de clé étrangère entre bases)
-- balance  = crédits possédés
-- reserved = part de balance bloquée pour des échanges acceptés
-- disponible = balance - reserved
CREATE TABLE credit_accounts (
    id         BIGINT NOT NULL AUTO_INCREMENT,
    user_id    BIGINT NOT NULL,
    balance    INT NOT NULL,
    reserved   INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_credit_accounts_user UNIQUE (user_id),
    CONSTRAINT chk_credit_balance_positive CHECK (balance >= 0),
    CONSTRAINT chk_credit_reserved_range CHECK (reserved >= 0 AND reserved <= balance)
);

-- Une réservation par échange (exchange_id vient d'exchange-service)
-- status : HELD (bloqué), TRANSFERRED (versé au prestataire), RELEASED (rendu au demandeur)
CREATE TABLE credit_holds (
    id          BIGINT NOT NULL AUTO_INCREMENT,
    exchange_id BIGINT NOT NULL,
    payer_id    BIGINT NOT NULL,
    payee_id    BIGINT NOT NULL,
    amount      INT NOT NULL,
    status      VARCHAR(20) NOT NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_credit_holds_exchange UNIQUE (exchange_id),
    CONSTRAINT chk_credit_hold_amount CHECK (amount > 0),
    CONSTRAINT chk_credit_hold_distinct CHECK (payer_id <> payee_id),
    INDEX idx_credit_holds_status (status)
);

-- Historique (US14). type : WELCOME, RESERVATION, RELEASE, TRANSFER_OUT, TRANSFER_IN
-- amount : toujours positif ; balance_after : solde total après l'opération
CREATE TABLE credit_transactions (
    id            BIGINT NOT NULL AUTO_INCREMENT,
    user_id       BIGINT NOT NULL,
    exchange_id   BIGINT NULL,
    type          VARCHAR(20) NOT NULL,
    amount        INT NOT NULL,
    balance_after INT NOT NULL,
    description   VARCHAR(255) NOT NULL,
    created_at    DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_credit_transactions_user (user_id)
);
