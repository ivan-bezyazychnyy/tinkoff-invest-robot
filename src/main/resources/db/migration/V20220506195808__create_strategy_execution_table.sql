CREATE TABLE strategy_execution (
    id uuid NOT NULL PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    strategy_id uuid NOT NULL REFERENCES strategy(id),
    tinkoff_account_id varchar(64) NOT NULL,
    sandbox boolean NOT NULL,
    started_at timestamptz NOT NULL,
    ended_at timestamptz
);

CREATE INDEX strategy_execution_strategy_id_index ON strategy_execution(strategy_id);

CREATE UNIQUE INDEX strategy_execution_tinkoff_account_id_unique_index ON strategy_execution(tinkoff_account_id)
    WHERE ended_at IS NULL;