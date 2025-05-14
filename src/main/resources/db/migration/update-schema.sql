CREATE TABLE ratings
(
    id              UUID    NOT NULL,
    user_id         UUID    NOT NULL,
    technician_id   UUID    NOT NULL,
    repair_order_id UUID    NOT NULL,
    comment         TEXT    NOT NULL,
    score           INTEGER NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    deleted         BOOLEAN NOT NULL,
    CONSTRAINT pk_ratings PRIMARY KEY (id)
);

CREATE TABLE repair_orders
(
    id                   UUID         NOT NULL,
    customer_id          UUID         NOT NULL,
    technician_id        UUID         NOT NULL,
    item_name            VARCHAR(100) NOT NULL,
    item_condition       VARCHAR(100) NOT NULL,
    issue_description    VARCHAR(500) NOT NULL,
    desired_service_date date         NOT NULL,
    status               VARCHAR(255) NOT NULL,
    created_at           TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at           TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_repair_orders PRIMARY KEY (id)
);

CREATE TABLE technician_reports
(
    report_id              UUID NOT NULL,
    request_id             UUID NOT NULL,
    technician_id          UUID NOT NULL,
    diagnosis              VARCHAR(500),
    action_plan            VARCHAR(500),
    estimated_cost         DECIMAL(10, 2),
    estimated_time_seconds BIGINT,
    status                 VARCHAR(255),
    customer_feedback      VARCHAR(500),
    last_updated_at        TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_technician_reports PRIMARY KEY (report_id)
);

CREATE TABLE user_requests
(
    request_id       UUID NOT NULL,
    user_id          UUID NOT NULL,
    user_description VARCHAR(500),
    CONSTRAINT pk_user_requests PRIMARY KEY (request_id)
);

ALTER TABLE technician_reports
    ADD CONSTRAINT FK_TECHNICIAN_REPORTS_ON_REQUEST FOREIGN KEY (request_id) REFERENCES user_requests (request_id);