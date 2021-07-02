alter table post_comments
       drop constraint if exists FKc3b7s6wypcsvua2ycn4o1lv2c;

alter table post_comments
       drop constraint if exists FKaawaqxjs3br8dw5v90w7uu514;

alter table post_comments
       drop constraint if exists FKsnxoecngu89u3fh4wdrgf0f2g;

alter table post_votes
       drop constraint if exists FK9jh5u17tmu1g7xnlxa77ilo3u;

alter table post_votes
       drop constraint if exists FK9q09ho9p8fmo6rcysnci8rocc;

alter table posts
       drop constraint if exists FK6m7nr3iwh1auer2hk7rd05riw;

alter table posts
       drop constraint if exists FK5lidm6cqbc7u4xhqpxm898qme;

alter table tag2post
       drop constraint if exists FKpjoedhh4h917xf25el3odq20i;

alter table tag2post
       drop constraint if exists FKjou6suf2w810t2u3l96uasw3r;

drop table if exists captcha_codes cascade;

drop table if exists global_settings cascade;

drop table if exists hibernate_sequences cascade;

drop table if exists post_comments cascade;

drop table if exists post_votes cascade;

drop table if exists posts cascade;

drop table if exists tag2post cascade;

drop table if exists tags cascade;

drop table if exists users cascade;

drop sequence if exists hibernate_sequence;

drop sequence if exists comments_id_seq;

drop sequence if exists posts_id_seq;

drop sequence if exists tag2post_id_seq;

drop sequence if exists tag_id_seq;

drop sequence if exists users_id_seq;

drop sequence if exists votes_id_seq;