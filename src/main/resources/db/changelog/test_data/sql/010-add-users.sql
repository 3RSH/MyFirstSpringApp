insert into users (id, is_moderator, reg_time, name, email, password)
values ('1', '1', (select now()), 'Admin', 'iteng@gmail.com', '$2y$12$Aff3FBqci9xbrpYKGfElCuVV9ZouTYh8y/NDxo15aPM1ID3NP9Fv6'),
('2', '-1', (select now()), 'NeCola', 'nik87@mail.ru', '$2y$12$NyXRwg16KOTNhxGGlUBWJeZ4ujqHXeD.u1WzA2eQU7CiSO17Cm3Cy'),
('3', '-1', (select now()), 'SuperDev', 'corp.dev@hack.by', '$2y$12$dJVkHen/m6NPYhMmp.FtQemiagh6LXsOXMum2Pj2HGZCjTd8FUmHy'),
('4', '-1', (select now()), 'Sanch0', 'alex190@mail.ru', '$2y$12$/s5.ivw2xZumVPNbNCblNuQTuMfj3KCT3MLqlhs1JM0VP0pm4frg6'),
('5', '-1', (select now()), 'FlyDragon', 'crzfruit@yandex.ru', '$2y$12$els2907GLc07gYvwv3bmn.keH05sZYdMESXDBvMZ1k6mUiNaZL2TG'),
('6', '-1', (select now()), 'Ko0zimiR', 'jim-b@gmail.com', '$2y$12$wmVlH7l1etgn25lI5vo4WunWnAep/M.g29HHqxNgMx.z9dy0loGMy'),
('7', '-1', (select now()), 'RockinRoller', 'stepashka@yandex.ru', '$2y$12$Bhn5GO4DMLFGHTm42CPc1uig25T3bFLLo7HbXWtoc9DqyYQl885Om'),
('8', '-1', (select now()), 'SCIfield', 'mmrnt@gmail.ua', '$2y$12$ZCT/MBaJQuAm7jzLYyeAc.uKOrE1tbeNnPSHeMbwTtevw.rnUfiWy'),
('9', '-1', (select now()), 'Cot-Vasher', 'lomogamer@mail.ru', '$2y$12$9gewnNbCdqs1kIWQW.RM6OB2exzt9sJxvRU3nP3TNGCxg8PcO8ay.'),
('10', '-1', (select now()), 'reFuktor', 'kalosha@yahoo.com', '$2y$12$DISbsEzu4O0HhruJfn6I8ep9hlNtjloxYhTKj26dxUDOM9/fKHu/C');

alter sequence users_id_seq restart with 11;