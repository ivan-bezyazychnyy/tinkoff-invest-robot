CREATE TABLE "order" (
    id uuid NOT NULL PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    tinkoff_account_id varchar(64) NOT NULL,
    sandbox boolean NOT NULL,
    figi varchar(32) NOT NULL,
    lots bigint NOT NULL,
    direction varchar(32) NOT NULL,
    status varchar(32) NOT NULL,
    place_at timestamp with time zone,
    tinkoff_order_id varchar(64),
    strategy_execution_id uuid NOT NULL REFERENCES strategy_execution(id)
);

CREATE INDEX order_strategy_execution_id_index ON "order"(strategy_execution_id);