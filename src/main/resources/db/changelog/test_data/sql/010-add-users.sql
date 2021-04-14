insert into users (id, is_moderator, reg_time, name, email, password)
values ('1', '1', (select now()), 'Admin', 'iteng@gmail.com', 'password'),
('2', '-1', (select now()), 'NeCola', 'nik87@mail.ru', 'password'),
('3', '-1', (select now()), 'SuperDev', 'corp.dev@hack.by', 'password'),
('4', '-1', (select now()), 'Sanch0', 'alex190@mail.ru', 'password'),
('5', '-1', (select now()), 'FlyDragon', 'crzfruit@yandex.ru', 'password'),
('6', '-1', (select now()), 'Ko0zimiR', 'jim-b@gmail.com', 'password'),
('7', '-1', (select now()), 'RockinRoller', 'stepashka@yandex.ru', 'password'),
('8', '-1', (select now()), 'SCIfield', 'mmrnt@gmail.ua', 'password'),
('9', '-1', (select now()), 'Cot-Vasher', 'lomogamer@mail.ru', 'password'),
('10', '-1', (select now()), 'reFuktor', 'kalosha@yahoo.com', 'password');