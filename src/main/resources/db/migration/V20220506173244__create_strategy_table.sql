CREATE TABLE strategy (
    id uuid NOT NULL PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    status varchar(32) NOT NULL,
    tinkoff_account_id varchar(64) NOT NULL,
    sandbox boolean NOT NULL,
    type varchar(32) NOT NULL,
    execution_period bigint NOT NULL,
    instruments text NOT NULL
);