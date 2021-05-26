create sequence hibernate_sequence start 1 increment 1;

create table captcha_codes (
       id int4 not null,
        code varchar(255) not null,
        secret_code varchar(255) not null,
        time TIMESTAMP WITH TIME ZONE not null,
        primary key (id)
    );

create table global_settings (
       id int4 not null,
        code varchar(255) not null,
        name varchar(255) not null,
        value varchar(255) not null,
        primary key (id)
    );

create table post_comments (
       id int4 not null,
        text TEXT not null,
        time TIMESTAMP WITH TIME ZONE not null,
        parent_id int4,
        post_id int4 not null,
        user_id int4 not null,
        primary key (id)
    );

create table post_votes (
       id int4 not null,
        time TIMESTAMP WITH TIME ZONE not null,
        value int2 not null,
        post_id int4 not null,
        user_id int4 not null,
        primary key (id)
    );

create table posts (
       id int4 not null,
        is_active int2 not null,
        moderation_status varchar(255) not null,
        text TEXT not null,
        time TIMESTAMP WITH TIME ZONE not null,
        title varchar(255) not null,
        view_count int4 not null,
        moderator_id int4,
        user_id int4 not null,
        primary key (id)
    );

create table tag2post (
       id int4 not null,
        post_id int4 not null,
        tag_id int4 not null,
        primary key (post_id, tag_id)
    );

create sequence tag2post_id_seq start with 1 increment by 1;

create table tags (
       id int4 not null,
        name varchar(255) not null,
        primary key (id)
    );

create table users (
       id int4 not null,
        code varchar(255),
        email varchar(255) not null,
        is_moderator int2 not null,
        name varchar(255) not null,
        password varchar(255) not null,
        photo TEXT,
        reg_time TIMESTAMP WITH TIME ZONE not null,
        primary key (id)
    );

create sequence users_id_seq start with 1 increment by 1;

alter table post_comments
       add constraint FKc3b7s6wypcsvua2ycn4o1lv2c
       foreign key (parent_id)
       references post_comments;

alter table post_comments
       add constraint FKaawaqxjs3br8dw5v90w7uu514
       foreign key (post_id)
       references posts;

alter table post_comments
       add constraint FKsnxoecngu89u3fh4wdrgf0f2g
       foreign key (user_id)
       references users;

alter table post_votes
       add constraint FK9jh5u17tmu1g7xnlxa77ilo3u
       foreign key (post_id)
       references posts;

alter table post_votes
       add constraint FK9q09ho9p8fmo6rcysnci8rocc
       foreign key (user_id)
       references users;

alter table posts
       add constraint FK6m7nr3iwh1auer2hk7rd05riw
       foreign key (moderator_id)
       references users;

alter table posts
       add constraint FK5lidm6cqbc7u4xhqpxm898qme
       foreign key (user_id)
       references users;

alter table tag2post
       add constraint FKpjoedhh4h917xf25el3odq20i
       foreign key (post_id)
       references posts;

alter table tag2post
       add constraint FKjou6suf2w810t2u3l96uasw3r
       foreign key (tag_id)
       references tags;