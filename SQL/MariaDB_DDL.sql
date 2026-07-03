CREATE SEQUENCE seq_users
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 999999
    CYCLE
    CACHE 20;

CREATE TABLE users
(
    user_id  VARCHAR(50)  NOT NULL,
    email    VARCHAR(255) NOT NULL,
    name     VARCHAR(50)  NOT NULL,
    password VARCHAR(255) NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (user_id)
) DEFAULT CHARSET=utf8mb4;

CREATE SEQUENCE seq_place
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 999999
    CYCLE
    CACHE 20;

CREATE TABLE place
(
    place_id    VARCHAR(50)     NOT NULL ,
    title       VARCHAR(255) NOT NULL,
    addr1       VARCHAR(255),
    addr2       VARCHAR(255),
    mapx        VARCHAR(100) NOT NULL,
    mapy        VARCHAR(100) NOT NULL,
    first_image TEXT,
    like_count  BIGINT       DEFAULT 0,
    place_type  VARCHAR(20)  NOT NULL,
    lclssystm1  VARCHAR(50),
    lclssystm2  VARCHAR(50),
    lclssystm3  VARCHAR(50),
    is_active   TINYINT(1)   NOT NULL DEFAULT 1,
    CONSTRAINT pk_place PRIMARY KEY (place_id)
) DEFAULT CHARSET=utf8mb4;

CREATE SEQUENCE seq_my_schedule
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 999999
    CYCLE
    CACHE 20;

# ALTER TABLE my_schedule DROP FOREIGN KEY fk_schedule_user;

CREATE TABLE my_schedule
(
    my_schedule_id VARCHAR(50)  NOT NULL,
    title          VARCHAR(255) NOT NULL,
    start_at       DATETIME     NOT NULL,
    budget_details TEXT,
    todo_details   TEXT,
    is_shared      TINYINT(1)   DEFAULT 0,
    user_id        VARCHAR(50)  NOT NULL,
    CONSTRAINT pk_my_schedule PRIMARY KEY (my_schedule_id)
#     CONSTRAINT fk_schedule_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
) DEFAULT CHARSET=utf8mb4;

CREATE SEQUENCE seq_schedule_post
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 999999
    CYCLE
    CACHE 20;
# ALTER TABLE schedule_post DROP FOREIGN KEY fk_post_user;
CREATE TABLE schedule_post
(
    post_id        VARCHAR(50)  NOT NULL,
    title          VARCHAR(255) NOT NULL,
    like_count     BIGINT       DEFAULT 0,
    view_count     BIGINT       DEFAULT 0,
    budget_details TEXT,
    todo_details   TEXT,
    posted_at      DATETIME     NOT NULL,
    is_anonymous   TINYINT(1)   NOT NULL,
    is_hidden      TINYINT(1)   NOT NULL DEFAULT 0,
    user_id        VARCHAR(50)  NOT NULL,
    CONSTRAINT pk_schedule_post PRIMARY KEY (post_id)
#     CONSTRAINT fk_post_user FOREIGN KEY (user_id) REFERENCES users (user_id)
) DEFAULT CHARSET=utf8mb4;


ALTER TABLE schedule_post
    ADD COLUMN is_hidden TINYINT(1) NOT NULL DEFAULT 0
AFTER is_anonymous;

CREATE SEQUENCE seq_visit_item
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 999999
    CYCLE
    CACHE 20;

CREATE TABLE visit_item
(
    visit_item_id    BIGINT AUTO_INCREMENT NOT NULL,
    visit_order      INT          NOT NULL,
    distance_to_next DOUBLE,
    place_id         VARCHAR(50)       NOT NULL,
    schedule_id      VARCHAR(50)  NOT NULL,
    schedule_type    VARCHAR(20)  NOT NULL,
    CONSTRAINT pk_visit_item PRIMARY KEY (visit_item_id),
    CONSTRAINT fk_visit_place FOREIGN KEY (place_id) REFERENCES place (place_id),
    CONSTRAINT chk_schedule_type CHECK (schedule_type IN ('SCHEDULE', 'POST'))
) DEFAULT CHARSET=utf8mb4;


ALTER TABLE visit_item
    MODIFY visit_item_id BIGINT NOT NULL AUTO_INCREMENT;

CREATE TABLE schedule_share_user
(
    share_id       BIGINT       NOT NULL AUTO_INCREMENT,
    permission     VARCHAR(10)  NOT NULL,
    my_schedule_id VARCHAR(50)  NOT NULL,
    shared_user_id VARCHAR(50)  NOT NULL,
    CONSTRAINT pk_schedule_share_user PRIMARY KEY (share_id),
    CONSTRAINT fk_share_schedule FOREIGN KEY (my_schedule_id) REFERENCES my_schedule (my_schedule_id) ON DELETE CASCADE
#     CONSTRAINT fk_share_user FOREIGN KEY (shared_user_id) REFERENCES users (user_id) ON DELETE CASCADE
) DEFAULT CHARSET=utf8mb4;

# ALTER TABLE schedule_share_user DROP FOREIGN KEY fk_share_user;


CREATE SEQUENCE seq_report_schedule_post
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 999999
    CYCLE
    CACHE 20;

CREATE TABLE report_schedule_post
(
    report_id  BIGINT  AUTO_INCREMENT NOT NULL,
    post_id     VARCHAR(50)  NOT NULL,
    reporter_id VARCHAR(50)  NOT NULL,
    reason TEXT,
    status VARCHAR(50) NOT NULL,
    reported_at DATETIME     NOT NULL,
    CONSTRAINT pk_report_schedule_post PRIMARY KEY (report_id),
    CONSTRAINT fk_report_schedule FOREIGN KEY (post_id) REFERENCES schedule_post (post_id) ON DELETE CASCADE
) DEFAULT CHARSET=utf8mb4;