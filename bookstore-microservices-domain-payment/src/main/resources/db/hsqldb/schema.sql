DROP TABLE wallet IF EXISTS;
DROP TABLE payment IF EXISTS;
DROP TABLE payment_error IF EXISTS;

CREATE TABLE wallet
(
    id         INTEGER IDENTITY PRIMARY KEY,
    money      DECIMAL,
    account_id INTEGER
);

CREATE TABLE payment
(
    id           INTEGER IDENTITY PRIMARY KEY,
    pay_id       VARCHAR(100),
    create_time  DATETIME,
    total_price  DECIMAL,
    expires      INTEGER NOT NULL,
    payment_link VARCHAR(300),
    pay_state    VARCHAR(20)
);

CREATE TABLE payment_error
(
    id           INTEGER IDENTITY PRIMARY KEY,
    pay_id       VARCHAR(100),
    product_id   INTEGER,
    amount       INTEGER
);
CREATE UNIQUE INDEX pay_stock ON payment_error (pay_id,product_id);