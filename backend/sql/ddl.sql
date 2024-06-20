CREATE TABLE teachers (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          username VARCHAR(50) NOT NULL UNIQUE,
                          password VARCHAR(100) NOT NULL
);

CREATE TABLE classrooms (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            classroom_name VARCHAR(50) NOT NULL,
                            code VARCHAR(6) NOT NULL UNIQUE,
                            teacher_id INT NOT NULL,
                            FOREIGN KEY (teacher_id) REFERENCES teachers(id)
);


-- 질문 테이블 생성
CREATE TABLE questions (
                           id INT AUTO_INCREMENT PRIMARY KEY,
                           `type` tinyint NOT NULL comment '0:OX형, 1:선다형',
                           question VARCHAR(255) NOT null,
                           classroom_id INT NOT NULL,
                           FOREIGN KEY (classroom_id) REFERENCES classrooms(id)
);

-- 문항 테이블 생성
CREATE TABLE choices (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         choice_text VARCHAR(255) NOT NULL,
                         question_id INT,
                         FOREIGN KEY (question_id) REFERENCES questions(id)
);

INSERT INTO teachers (username, password) VALUES ('teacher1', '$2a$12$qhx4KBRsDSd8qDXrCput3uZGhczej5t4aOtycYRVQsNR4REXLFZAy'); -- 암호: "password"를 BCrypt로 인코딩한 값
INSERT INTO classrooms (classroom_name, code, teacher_id) VALUES ('즐거운 판서수업1','024588', 1);