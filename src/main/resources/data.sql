insert into USERS(created_at, updated_at, login_id, nickname, password, runner_level, user_role,
                  user_match_state)
VALUES (current_timestamp, current_timestamp, 'aaaa', 'TestUser1',
        '$2a$10$QiKe/JeG5mjed0Hwhu8Bv.MOsG4bs69ra7DHbH2kEngZyulguyuba', 'INTERMEDIATE',
        'ROLE_USER', 'IDLE');

insert into USERS(created_at, updated_at, login_id, nickname, password, runner_level, user_role,
                  user_match_state)
VALUES (current_timestamp, current_timestamp, 'bbbb', 'TestUser2',
        '$2a$10$QiKe/JeG5mjed0Hwhu8Bv.MOsG4bs69ra7DHbH2kEngZyulguyuba', 'BEGINNER',
        'ROLE_USER', 'IDLE');
-- pw: asdf

insert into USERS(created_at, updated_at, login_id, nickname, password, runner_level, user_role,
                  user_match_state)
VALUES (current_timestamp, current_timestamp, 'cccc', 'TestUser3',
        '$2a$10$QiKe/JeG5mjed0Hwhu8Bv.MOsG4bs69ra7DHbH2kEngZyulguyuba', 'EXPERT',
        'ROLE_USER', 'IDLE');
-- pw: asdf

insert into USERS(created_at, updated_at, login_id, nickname, password, runner_level, user_role,
                  user_match_state)
VALUES (current_timestamp, current_timestamp, 'dddd', 'TestUser4',
        '$2a$10$QiKe/JeG5mjed0Hwhu8Bv.MOsG4bs69ra7DHbH2kEngZyulguyuba', 'BEGINNER',
        'ROLE_USER', 'IDLE');
-- pw: asdf

insert into USERS(created_at, updated_at, login_id, nickname, password, runner_level, user_role,
                  user_match_state)
VALUES (current_timestamp, current_timestamp, 'eeee', 'TestUser5',
        '$2a$10$QiKe/JeG5mjed0Hwhu8Bv.MOsG4bs69ra7DHbH2kEngZyulguyuba', 'INTERMEDIATE',
        'ROLE_USER', 'IDLE');
-- pw: asdf


-- friend list sample
INSERT INTO friend (nickname, runner_level, address, likecount)
VALUES('러너1', 'Beginner', '서울특별시 강남구', 0),
      ('러너2', 'Intermediate', '대전 유성구', 0),
      ('러너3', 'Advanced', '부산 해운대구',  2),
      ('러너4', 'Elite', '광주 서구', 5);