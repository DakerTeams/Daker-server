-- ============================================================
-- DAKER 전체 더미데이터 스크립트 (개선판)
-- 기준일: 2026-04-05  (시간은 SHIFT 블록이 NOW() 기준으로 자동 정렬)
-- 비밀번호: password   (BCrypt 해시 통일, 모든 시드 유저 공통)
-- ============================================================
-- ⚠️ 적용 시 반드시 utf8mb4 클라이언트 charset 사용
--    클라이언트가 latin1로 동작하면 한글이 byte 길이로 계산되어
--    varchar 컬럼 제한을 초과하거나 데이터가 깨질 수 있습니다.
--
--    docker exec -i daker-mysql \
--        mysql --default-character-set=utf8mb4 -udaker -pdaker1234 daker \
--        < dummy_data.sql
--
-- 적용 흐름:
--   1) 백엔드를 한 번 부팅해 ddl-auto가 teams.deleted_at 컬럼을 생성
--   2) 위 명령으로 dummy_data.sql 적용
--   3) (선택) SEEDER_ENABLED=true ./gradlew bootRun 한 번 → 유저 80명까지 부풀리기
--   4) 평소 모드(./gradlew bootRun)로 재기동
--
-- 주요 분포 (시간 시프트 후):
--   - 유저 20명 (admin 1 / judge 2 / user 17) → 시더 활성 시 80명
--   - 해커톤 30개: ENDED·CLOSED(진행중)·OPEN·UPCOMING 골고루
--   - 팀 59개, 모든 해커톤이 description 200자 내외 + criteria 4개
--   - 엣지 케이스: 만석 팀, 소프트 딜리트 팀, PENDING/REJECTED 신청
-- ============================================================
SET NAMES utf8mb4;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;

SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS user_tags (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_tag (user_id, tag_id),
    CONSTRAINT fk_utag_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_utag_tag FOREIGN KEY (tag_id) REFERENCES tags(id)
);

TRUNCATE TABLE chat_messages;
TRUNCATE TABLE chat_participants;
TRUNCATE TABLE user_xp_history;
TRUNCATE TABLE user_tags;
TRUNCATE TABLE votes;
TRUNCATE TABLE judge_evaluations;
TRUNCATE TABLE submission_items;
TRUNCATE TABLE submissions;
TRUNCATE TABLE hackathon_registrations;
TRUNCATE TABLE team_applications;
TRUNCATE TABLE team_members;
TRUNCATE TABLE team_private_infos;
TRUNCATE TABLE team_positions;
TRUNCATE TABLE teams;
TRUNCATE TABLE hackathon_judges;
TRUNCATE TABLE hackathon_links;
TRUNCATE TABLE hackathon_notices;
TRUNCATE TABLE criteria;
TRUNCATE TABLE prizes;
TRUNCATE TABLE milestones;
TRUNCATE TABLE hackathon_submission_rules;
TRUNCATE TABLE hackathon_tags;
TRUNCATE TABLE tags;
TRUNCATE TABLE hackathons;
TRUNCATE TABLE users;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 1. USERS (12명)
-- id 1: admin / id 2-3: judge / id 4-12: user1-9
-- ============================================================
INSERT INTO users (id, email, nickname, password, role, account_status, created_at, updated_at) VALUES
(1,  'admin@daker.com',  'admin',        '$2b$10$jzMveWcSErfoFJvdKEQWQuIljP0Aauj/6HHh7YmR3lp10Bk0lMloy', 'ADMIN', 'ACTIVE', '2026-01-01 00:00:00', '2026-01-01 00:00:00'),
(2,  'judge1@daker.com', '박재원',       '$2b$10$jzMveWcSErfoFJvdKEQWQuIljP0Aauj/6HHh7YmR3lp10Bk0lMloy', 'JUDGE', 'ACTIVE', '2026-01-01 00:00:00', '2026-01-01 00:00:00'),
(3,  'judge2@daker.com', '이수진',       '$2b$10$jzMveWcSErfoFJvdKEQWQuIljP0Aauj/6HHh7YmR3lp10Bk0lMloy', 'JUDGE', 'ACTIVE', '2026-01-01 00:00:00', '2026-01-01 00:00:00'),
(4,  'user1@daker.com',  'devking',      '$2b$10$jzMveWcSErfoFJvdKEQWQuIljP0Aauj/6HHh7YmR3lp10Bk0lMloy', 'USER',  'ACTIVE', '2026-01-01 00:00:00', '2026-01-01 00:00:00'),
(5,  'user2@daker.com',  '김민준',       '$2b$10$jzMveWcSErfoFJvdKEQWQuIljP0Aauj/6HHh7YmR3lp10Bk0lMloy', 'USER',  'ACTIVE', '2026-01-01 00:00:00', '2026-01-01 00:00:00'),
(6,  'user3@daker.com',  'nullPointer',  '$2b$10$jzMveWcSErfoFJvdKEQWQuIljP0Aauj/6HHh7YmR3lp10Bk0lMloy', 'USER',  'ACTIVE', '2026-01-01 00:00:00', '2026-01-01 00:00:00'),
(7,  'user4@daker.com',  '이서연',       '$2b$10$jzMveWcSErfoFJvdKEQWQuIljP0Aauj/6HHh7YmR3lp10Bk0lMloy', 'USER',  'ACTIVE', '2026-01-02 00:00:00', '2026-01-02 00:00:00'),
(8,  'user5@daker.com',  'sudo_kim',     '$2b$10$jzMveWcSErfoFJvdKEQWQuIljP0Aauj/6HHh7YmR3lp10Bk0lMloy', 'USER',  'ACTIVE', '2026-01-02 00:00:00', '2026-01-02 00:00:00'),
(9,  'user6@daker.com',  '정하은',       '$2b$10$jzMveWcSErfoFJvdKEQWQuIljP0Aauj/6HHh7YmR3lp10Bk0lMloy', 'USER',  'ACTIVE', '2026-01-02 00:00:00', '2026-01-02 00:00:00'),
(10, 'user7@daker.com',  'byte_me',      '$2b$10$jzMveWcSErfoFJvdKEQWQuIljP0Aauj/6HHh7YmR3lp10Bk0lMloy', 'USER',  'ACTIVE', '2026-01-03 00:00:00', '2026-01-03 00:00:00'),
(11, 'user8@daker.com',  '최유진',       '$2b$10$jzMveWcSErfoFJvdKEQWQuIljP0Aauj/6HHh7YmR3lp10Bk0lMloy', 'USER',  'ACTIVE', '2026-01-03 00:00:00', '2026-01-03 00:00:00'),
(12, 'user9@daker.com',  'git_wizard',   '$2b$10$jzMveWcSErfoFJvdKEQWQuIljP0Aauj/6HHh7YmR3lp10Bk0lMloy', 'USER',  'ACTIVE', '2026-01-03 00:00:00', '2026-01-03 00:00:00');

-- ============================================================
-- 2. HACKATHONS (10개)
-- H1: ENDED/SCORE  H2: ENDED/VOTE  H3: CLOSED/SCORE  H4: CLOSED/VOTE
-- H5: OPEN/SCORE   H6: OPEN/SCORE  H7-H10: UPCOMING
-- ============================================================
INSERT INTO hackathons (id, title, summary, description, thumbnail_url, organizer, status, score_type,
    start_date, end_date, registration_start_date, registration_end_date, submission_deadline_at,
    closed_at, max_team_size, max_participants, camp_enabled, allow_solo, deleted, created_at, updated_at) VALUES
(1, 'AI 스타트업 해커톤 2025',
 'AI 기술로 스타트업 아이디어를 실현하는 72시간 해커톤',
 '인공지능 기술을 활용한 혁신적인 스타트업 아이디어를 개발하세요. 멘토링과 투자자 피칭 기회가 제공됩니다.',
 'https://picsum.photos/seed/hack1/800/400', 'Daker', 'ENDED', 'SCORE',
 '2026-01-10 09:00:00', '2026-01-12 18:00:00',
 '2025-12-01 00:00:00', '2026-01-05 23:59:59', '2026-01-12 15:00:00',
 NULL, 4, 120, 0, 0, 0, '2025-11-15 00:00:00', '2026-01-13 00:00:00'),
(2, 'XR 메타버스 챌린지',
 'AR/VR 기술로 현실과 가상의 경계를 허무는 48시간 해커톤',
 'Meta Quest, HoloLens 등 XR 기기를 활용하여 교육, 엔터테인먼트, 비즈니스 분야의 메타버스 서비스를 개발하세요.',
 'https://picsum.photos/seed/hack2/800/400', 'Daker', 'ENDED', 'VOTE',
 '2026-02-14 09:00:00', '2026-02-16 18:00:00',
 '2026-01-10 00:00:00', '2026-02-10 23:59:59', '2026-02-16 15:00:00',
 NULL, 5, 80, 1, 0, 0, '2026-01-05 00:00:00', '2026-02-17 00:00:00'),
(3, '데이터 사이언스 경진대회',
 '공공 빅데이터로 사회 문제를 해결하는 데이터 분석 경진대회',
 '공공데이터 포털의 다양한 데이터셋을 분석하여 사회 문제를 해결하는 모델을 개발합니다.',
 'https://picsum.photos/seed/hack3/800/400', 'Daker', 'ENDED', 'SCORE',
 '2026-03-07 09:00:00', '2026-03-09 18:00:00',
 '2026-02-01 00:00:00', '2026-03-03 23:59:59', '2026-03-09 15:00:00',
 '2026-03-10 09:00:00', 3, 200, 0, 1, 0, '2026-01-25 00:00:00', '2026-03-10 09:00:00'),
(4, '모바일 앱 해커톤 Spring 2026',
 'iOS/Android 크로스플랫폼 모바일 앱을 48시간 안에 완성하세요',
 'Flutter, React Native, Swift, Kotlin 등 자유로운 기술 스택으로 일상의 불편함을 해소하는 모바일 앱을 개발합니다.',
 'https://picsum.photos/seed/hack4/800/400', 'Daker', 'ENDED', 'VOTE',
 '2026-03-21 09:00:00', '2026-03-23 18:00:00',
 '2026-02-15 00:00:00', '2026-03-17 23:59:59', '2026-03-23 15:00:00',
 '2026-03-24 09:00:00', 4, 150, 0, 0, 0, '2026-02-10 00:00:00', '2026-03-24 09:00:00'),
(5, 'AI 혁신 해커톤 2026',
 'GPT, Claude 등 최신 LLM을 활용한 혁신 서비스를 개발하세요',
 '대형 언어 모델(LLM)을 활용하여 기업과 개인의 생산성을 혁신하는 AI 서비스를 개발합니다.',
 'https://picsum.photos/seed/hack5/800/400', 'Daker', 'CLOSED', 'SCORE',
 '2026-03-30 09:00:00', '2026-04-10 18:00:00',
 '2026-03-01 00:00:00', '2026-03-25 23:59:59', '2026-04-10 15:00:00',
 NULL, 4, 150, 0, 1, 0, '2026-02-20 00:00:00', '2026-03-30 09:00:00'),
(6, '핀테크 챌린지 2026',
 '금융과 기술의 융합으로 혁신적인 핀테크 서비스를 만들어보세요',
 '블록체인, 오픈뱅킹 API를 활용하여 금융 서비스의 미래를 만들어보세요.',
 'https://picsum.photos/seed/hack6/800/400', 'Daker', 'CLOSED', 'SCORE',
 '2026-03-28 09:00:00', '2026-04-08 18:00:00',
 '2026-03-01 00:00:00', '2026-03-23 23:59:59', '2026-04-08 15:00:00',
 NULL, 5, 100, 0, 0, 0, '2026-02-20 00:00:00', '2026-03-28 09:00:00'),
(7, '그린테크 해커톤',
 '기후 변화 대응을 위한 친환경 기술 솔루션을 개발하세요',
 '탄소 감축, 신재생 에너지, 스마트 그리드 등 환경 문제를 기술로 해결하세요.',
 'https://picsum.photos/seed/hack7/800/400', 'Daker', 'OPEN', 'VOTE',
 '2026-04-20 09:00:00', '2026-04-22 18:00:00',
 '2026-04-01 00:00:00', '2026-04-15 23:59:59', '2026-04-22 15:00:00',
 NULL, 4, 80, 1, 0, 0, '2026-03-15 00:00:00', '2026-03-15 00:00:00'),
(8, '헬스케어 이노베이션 해커톤',
 '디지털 헬스케어 기술로 의료 접근성을 높이는 혁신 서비스를 개발하세요',
 '원격 진료, 웨어러블 데이터 분석, AI 진단 보조 등 헬스케어 분야의 미래를 만드세요.',
 'https://picsum.photos/seed/hack8/800/400', 'Daker', 'UPCOMING', 'SCORE',
 '2026-05-10 09:00:00', '2026-05-12 18:00:00',
 '2026-04-15 00:00:00', '2026-05-05 23:59:59', '2026-05-12 15:00:00',
 NULL, 5, 100, 0, 1, 0, '2026-03-20 00:00:00', '2026-03-20 00:00:00'),
(9, '스마트시티 해커톤',
 'IoT와 빅데이터로 더 스마트한 도시 인프라를 구축하는 대회',
 '교통, 에너지, 안전, 환경 등 도시 문제를 IoT 센서와 데이터 분석으로 해결하세요.',
 'https://picsum.photos/seed/hack9/800/400', 'Daker', 'UPCOMING', 'SCORE',
 '2026-06-01 09:00:00', '2026-06-03 18:00:00',
 '2026-05-01 00:00:00', '2026-05-25 23:59:59', '2026-06-03 15:00:00',
 NULL, 5, 120, 1, 0, 0, '2026-04-01 00:00:00', '2026-04-01 00:00:00'),
(10, '사이버보안 챌린지',
 'CTF 형식 취약점 분석과 보안 솔루션 개발을 병행하는 해커톤',
 '취약점 분석, 암호화, 네트워크 보안 등 사이버보안 전 분야를 아우르는 종합 보안 챌린지.',
 'https://picsum.photos/seed/hack10/800/400', 'Daker', 'UPCOMING', 'SCORE',
 '2026-07-01 09:00:00', '2026-07-03 18:00:00',
 '2026-06-01 00:00:00', '2026-06-25 23:59:59', '2026-07-03 15:00:00',
 NULL, 3, 60, 0, 1, 0, '2026-04-01 00:00:00', '2026-04-01 00:00:00');

-- ============================================================
-- 3. TAGS
-- ============================================================
INSERT INTO tags (id, name) VALUES
(1,'AI'),(2,'스타트업'),(3,'머신러닝'),(4,'XR'),(5,'메타버스'),
(6,'VR'),(7,'AR'),(8,'데이터분석'),(9,'빅데이터'),(10,'Python'),
(11,'ML'),(12,'모바일'),(13,'iOS'),(14,'Android'),(15,'Flutter'),
(16,'LLM'),(17,'GPT'),(18,'생성형AI'),(19,'핀테크'),(20,'블록체인'),
(21,'금융'),(22,'오픈뱅킹'),(23,'그린테크'),(24,'환경'),(25,'탄소중립'),
(26,'신재생에너지'),(27,'헬스케어'),(28,'AI진단'),(29,'원격의료'),
(30,'스마트시티'),(31,'IoT'),(32,'보안'),(33,'CTF'),(34,'취약점분석');

-- ============================================================
-- 4. HACKATHON_TAGS
-- ============================================================
INSERT INTO hackathon_tags (hackathon_id, tag_id) VALUES
(1,1),(1,2),(1,3),(2,4),(2,5),(2,6),(2,7),(3,8),(3,9),(3,10),(3,11),
(4,12),(4,13),(4,14),(4,15),(5,1),(5,16),(5,17),(5,18),
(6,19),(6,20),(6,21),(6,22),(7,23),(7,24),(7,25),(7,26),
(8,27),(8,28),(8,29),(9,30),(9,31),(9,9),(10,32),(10,33),(10,34);

-- ============================================================
-- 5. MILESTONES
-- ============================================================
INSERT INTO milestones (hackathon_id, title, description, date) VALUES
(1,'참가 접수 시작',NULL,'2025-12-01 00:00:00'),
(1,'참가 접수 마감',NULL,'2026-01-05 23:59:59'),
(1,'해커톤 시작','팀 구성 및 아이디어 발표','2026-01-10 09:00:00'),
(1,'중간 발표','진행 상황 점검 및 멘토링','2026-01-11 14:00:00'),
(1,'최종 발표','팀별 10분 발표 + 5분 Q&A','2026-01-12 14:00:00'),
(1,'시상식',NULL,'2026-01-12 17:00:00'),
(2,'참가 접수 시작',NULL,'2026-01-10 00:00:00'),
(2,'참가 접수 마감',NULL,'2026-02-10 23:59:59'),
(2,'기기 대여 및 세팅','Meta Quest 3, HoloLens 2 무료 대여','2026-02-14 09:00:00'),
(2,'투표 시작','제출 마감 후 투표 개시','2026-02-16 15:00:00'),
(2,'투표 종료 및 시상',NULL,'2026-02-16 18:00:00'),
(3,'참가 접수 시작',NULL,'2026-02-01 00:00:00'),
(3,'참가 접수 마감',NULL,'2026-03-03 23:59:59'),
(3,'데이터셋 공개','공공데이터 API 키 배포','2026-03-07 09:00:00'),
(3,'분석 결과 제출 마감',NULL,'2026-03-09 15:00:00'),
(3,'시상식',NULL,'2026-03-09 18:00:00'),
(4,'참가 접수 시작',NULL,'2026-02-15 00:00:00'),
(4,'참가 접수 마감',NULL,'2026-03-17 23:59:59'),
(4,'해커톤 시작 및 킥오프',NULL,'2026-03-21 09:00:00'),
(4,'제출 마감',NULL,'2026-03-23 15:00:00'),
(4,'투표 종료 및 시상',NULL,'2026-03-23 18:00:00'),
(5,'참가 접수 시작',NULL,'2026-03-01 00:00:00'),
(5,'참가 접수 마감',NULL,'2026-03-25 23:59:59'),
(5,'해커톤 시작','API 크레딧 배포','2026-03-30 09:00:00'),
(5,'중간 멘토링',NULL,'2026-04-05 14:00:00'),
(5,'제출 마감',NULL,'2026-04-10 15:00:00'),
(5,'데모데이 및 시상',NULL,'2026-04-10 17:00:00'),
(6,'참가 접수 시작',NULL,'2026-03-01 00:00:00'),
(6,'참가 접수 마감',NULL,'2026-03-23 23:59:59'),
(6,'해커톤 시작',NULL,'2026-03-28 09:00:00'),
(6,'제출 마감',NULL,'2026-04-08 15:00:00'),
(6,'최종 발표 및 시상',NULL,'2026-04-08 18:00:00'),
(7,'참가 접수 시작',NULL,'2026-04-01 00:00:00'),
(7,'참가 접수 마감',NULL,'2026-04-15 23:59:59'),
(7,'해커톤 시작',NULL,'2026-04-20 09:00:00'),
(7,'최종 발표',NULL,'2026-04-22 14:00:00'),
(8,'참가 접수 시작',NULL,'2026-04-15 00:00:00'),
(8,'참가 접수 마감',NULL,'2026-05-05 23:59:59'),
(8,'해커톤 시작',NULL,'2026-05-10 09:00:00'),
(8,'최종 발표',NULL,'2026-05-12 14:00:00'),
(9,'참가 접수 시작',NULL,'2026-05-01 00:00:00'),
(9,'참가 접수 마감',NULL,'2026-05-25 23:59:59'),
(9,'해커톤 개막식',NULL,'2026-06-01 09:00:00'),
(9,'최종 발표 및 시상',NULL,'2026-06-03 15:00:00'),
(10,'참가 접수 시작',NULL,'2026-06-01 00:00:00'),
(10,'본선 시작',NULL,'2026-07-01 09:00:00'),
(10,'시상식',NULL,'2026-07-03 18:00:00');

-- ============================================================
-- 6. PRIZES
-- ============================================================
INSERT INTO prizes (hackathon_id, ranking, amount, description) VALUES
(1,1,5000000,'대상 - 투자 연계 기회 제공'),(1,2,3000000,'최우수상'),(1,3,1000000,'우수상'),
(2,1,10000000,'대상'),(2,2,5000000,'최우수상'),(2,3,2000000,'우수상'),
(3,1,3000000,'대상'),(3,2,1500000,'최우수상'),(3,3,500000,'우수상'),
(4,1,8000000,'최우수상'),(4,2,4000000,'우수상'),(4,3,1500000,'장려상'),
(5,1,20000000,'대상'),(5,2,10000000,'최우수상'),(5,3,5000000,'우수상'),
(6,1,15000000,'대상'),(6,2,7000000,'최우수상'),(6,3,3000000,'우수상'),
(7,1,8000000,'대상'),(7,2,4000000,'최우수상'),(7,3,2000000,'우수상'),
(8,1,12000000,'대상'),(8,2,6000000,'최우수상'),(8,3,2500000,'우수상'),
(9,1,25000000,'대상'),(9,2,10000000,'최우수상'),(9,3,5000000,'우수상'),
(10,1,10000000,'대상'),(10,2,5000000,'최우수상'),(10,3,2000000,'우수상');

-- ============================================================
-- 7. CRITERIA (SCORE 타입 해커톤)
-- ============================================================
INSERT INTO criteria (hackathon_id, name, description, max_score) VALUES
(1,'아이디어 혁신성','AI 기술을 활용한 창의적 문제 해결',30),
(1,'기술 완성도','코드 품질 및 구현 수준',30),
(1,'비즈니스 가능성','시장성 및 수익 모델',30),
(1,'발표력','명확한 발표 및 Q&A 대응',10),
(3,'데이터 활용도','공공 데이터 다양하고 적절히 활용',30),
(3,'분석 정확도','모델 성능 및 통계적 유의성',40),
(3,'시각화 품질','인사이트를 명확히 전달하는 시각화',20),
(3,'발표력','데이터 기반 스토리텔링',10),
(5,'AI 활용도','LLM의 창의적이고 효과적인 활용',30),
(5,'기술 완성도','서비스 구현 및 안정성',30),
(5,'비즈니스 임팩트','실제 문제 해결 가능성 및 시장성',30),
(5,'발표력','데모 시연 및 Q&A',10),
(6,'금융 혁신성','기존 금융 서비스 개선 정도',35),
(6,'보안 안전성','금융 보안 규정 준수',35),
(6,'사용자 편의성','UX 및 접근성',20),
(6,'발표력',NULL,10),
(8,'임상적 유용성','의료 현장 적용 가능성',35),
(8,'기술 완성도','AI/디지털 기술 활용',30),
(8,'사용자 경험','환자 및 의료진 UX',25),
(8,'발표력',NULL,10),
(9,'도시 문제 해결력','실제 도시 문제 해결 효과',35),
(9,'공공데이터 활용도','공공 API 활용 적절성',25),
(9,'기술 완성도','IoT/빅데이터 기술 구현',30),
(9,'발표력',NULL,10),
(10,'취약점 발견 수','유효한 보안 취약점 식별',40),
(10,'보고서 품질','취약점 분석 보고서 완성도',30),
(10,'방어 솔루션','보안 패치 및 방어 코드 완성도',20),
(10,'발표력',NULL,10);

-- ============================================================
-- 8. HACKATHON_NOTICES
-- ============================================================
INSERT INTO hackathon_notices (hackathon_id, content) VALUES
(1,'팀당 최대 4명까지 참여 가능합니다. 1인 참가는 허용되지 않습니다.'),
(1,'사전에 개발된 코드 사용은 불가합니다. 해커톤 기간 중 새로 개발해야 합니다.'),
(1,'최종 발표는 5분 이내 데모 시연을 포함해야 합니다.'),
(2,'현장 참여 필수입니다. XR 기기(Meta Quest 3, HoloLens 2)는 무료 대여됩니다.'),
(2,'Unity 또는 Unreal Engine 사용을 권장합니다.'),
(3,'공공데이터 포털(data.go.kr) 데이터를 반드시 활용해야 합니다.'),
(3,'1인 참가 가능합니다. Python, R, Jupyter Notebook 환경이 제공됩니다.'),
(4,'실제 기기(iOS 또는 Android)에서 동작하는 앱을 제출해야 합니다.'),
(5,'OpenAI API, Anthropic Claude API 크레딧이 지원됩니다.'),
(6,'금융위원회 규정을 준수하는 솔루션에 추가 가점이 있습니다.'),
(7,'현장 참여 필수 (숙박 제공).'),
(8,'의료법 관련 규정을 반드시 준수해야 합니다.'),
(9,'서울시 열린데이터광장 및 부산 빅데이터 플랫폼 API가 제공됩니다.'),
(10,'모든 취약점 분석은 지정된 샌드박스 환경에서만 수행해야 합니다.');

-- ============================================================
-- 9. HACKATHON_LINKS
-- ============================================================
INSERT INTO hackathon_links (hackathon_id, link_type, label, url) VALUES
(1,'WEBSITE','공식 홈페이지','https://daker.com'),
(1,'DISCORD','Discord 채널','https://discord.gg/daker'),
(2,'WEBSITE','공식 홈페이지','https://daker.com'),
(2,'NOTION','참가자 가이드','https://notion.so/xr-guide'),
(3,'WEBSITE','공공데이터 포털','https://data.go.kr'),
(3,'GITHUB','데이터셋 저장소','https://github.com/daker/datascience-2026'),
(4,'WEBSITE','공식 홈페이지','https://daker.com'),
(4,'DISCORD','Discord 채널','https://discord.gg/daker-mobile'),
(5,'WEBSITE','공식 홈페이지','https://daker.com'),
(5,'DISCORD','Discord 채널','https://discord.gg/daker-ai'),
(5,'NOTION','참가자 핸드북','https://notion.so/ai-hackathon-2026'),
(6,'WEBSITE','공식 홈페이지','https://daker.com'),
(7,'WEBSITE','공식 홈페이지','https://daker.com'),
(8,'WEBSITE','공식 홈페이지','https://daker.com'),
(9,'WEBSITE','서울 열린데이터광장','https://data.seoul.go.kr'),
(10,'WEBSITE','공식 홈페이지','https://daker.com');

-- ============================================================
-- 10. HACKATHON_SUBMISSION_RULES
-- ============================================================
INSERT INTO hackathon_submission_rules (hackathon_id, artifact_type, required, label, description, sort_order) VALUES
(1,'url', 1,'GitHub 저장소','소스코드가 포함된 GitHub 저장소 URL',1),
(1,'url', 1,'발표 자료','Google Slides 또는 PDF 링크',2),
(1,'url', 0,'데모 영상','YouTube 또는 Vimeo 데모 영상 링크',3),
(1,'text',1,'프로젝트 설명','프로젝트 개요 및 주요 기능 설명 (500자 이내)',4),
(3,'url', 1,'GitHub 저장소','분석 코드 및 Notebook이 포함된 저장소',1),
(3,'url', 1,'발표 자료','분석 결과 발표 자료 링크',2),
(3,'file',1,'분석 보고서','PDF 형식 분석 보고서 (필수 제출)',3),
(3,'text',1,'분석 요약','핵심 인사이트 및 결론 요약 (300자 이내)',4),
(5,'url', 1,'GitHub 저장소','소스코드 저장소',1),
(5,'url', 1,'서비스 URL','배포된 서비스 또는 데모 URL',2),
(5,'url', 0,'데모 영상','3분 이내 데모 영상',3),
(5,'text',1,'프로젝트 설명','서비스 개요, 사용 LLM, 주요 기능 (500자 이내)',4),
(6,'url', 1,'GitHub 저장소','소스코드 저장소',1),
(6,'url', 1,'서비스 데모','서비스 시연 영상 또는 라이브 URL',2),
(6,'text',1,'프로젝트 설명','서비스 개요 및 금융 규정 준수 방식 (500자 이내)',3);

-- ============================================================
-- 11. HACKATHON_JUDGES
-- judge1(2): H1-H6 전담
-- judge2(3): H1, H3, H5 (복수 심사)
-- ============================================================
INSERT INTO hackathon_judges (hackathon_id, user_id, assigned_at) VALUES
(1,2,'2026-01-05 00:00:00'),(1,3,'2026-01-05 00:00:00'),
(2,2,'2026-02-08 00:00:00'),
(3,2,'2026-03-01 00:00:00'),(3,3,'2026-03-01 00:00:00'),
(4,2,'2026-03-15 00:00:00'),
(5,2,'2026-03-25 00:00:00'),(5,3,'2026-03-25 00:00:00'),
(6,2,'2026-03-23 00:00:00');

-- ============================================================
-- 12. TEAMS (18개, H1-H6 각 3팀)
-- H1: team1(user1+user2), team2(user3+user4), team3(user5+user6)
-- H2: team4(user1+user7), team5(user2+user8), team6(user3+user9)
-- H3: team7(user4+user5), team8(user6+user7), team9(user8+user9)
-- H4: team10(user1+user4), team11(user2+user8), team12(user3+user9)
-- H5: team13(user5+user6), team14(user7+user1), team15(user2, solo)
-- H6: team16(user3+user4), team17(user8+user9), team18(user5, solo)
-- ============================================================
INSERT INTO teams (id, hackathon_id, owner_user_id, name, description, status, is_open, is_public,
    current_member_count, max_member_count, created_at, updated_at) VALUES
(1, 1,4,'Alpha Team','AI로 부동산 허위 매물을 탐지하는 서비스','CLOSED',0,1,2,4,'2025-12-15 10:00:00','2026-01-10 09:00:00'),
(2, 1,6,'Beta Team','AI 기반 중소기업 맞춤 채용 매칭 플랫폼','CLOSED',0,1,2,4,'2025-12-20 14:00:00','2026-01-10 09:00:00'),
(3, 1,8,'Gamma Squad','음성 인식 기반 실시간 회의록 자동화 서비스','CLOSED',0,1,2,4,'2025-12-22 11:00:00','2026-01-10 09:00:00'),
(4, 2,4,'Nova','XR 기반 실시간 원격 교육 플랫폼','CLOSED',0,1,2,5,'2026-01-20 10:00:00','2026-02-14 09:00:00'),
(5, 2,5,'Pixel','AR 쇼핑 경험 - 집에서 가구를 배치해보는 서비스','CLOSED',0,1,2,5,'2026-01-25 11:00:00','2026-02-14 09:00:00'),
(6, 2,6,'Prism','VR 가상 오피스 협업 플랫폼','CLOSED',0,1,2,5,'2026-01-28 13:00:00','2026-02-14 09:00:00'),
(7, 3,7,'Data+','서울시 미세먼지 데이터 기반 이동 경로 추천','CLOSED',0,1,2,3,'2026-02-10 09:00:00','2026-03-07 09:00:00'),
(8, 3,9,'Insight','공공데이터로 분석한 지역별 복지 사각지대','CLOSED',0,1,2,3,'2026-02-12 11:00:00','2026-03-07 09:00:00'),
(9, 3,11,'Sigma','전국 폐업 상권 데이터 기반 창업 입지 분석','CLOSED',0,1,2,3,'2026-02-15 09:00:00','2026-03-07 09:00:00'),
(10,4,4,'AppMakers','운동 루틴 공유 및 챌린지 앱','CLOSED',0,1,2,4,'2026-02-20 10:00:00','2026-03-21 09:00:00'),
(11,4,5,'MobileX','지역 소상공인 연결 장터 앱','CLOSED',0,1,2,4,'2026-02-22 14:00:00','2026-03-21 09:00:00'),
(12,4,6,'SwiftUI','시간표 공유 기반 약속 잡기 앱','CLOSED',0,1,2,4,'2026-02-24 10:00:00','2026-03-21 09:00:00'),
(13,5,8,'Spark','LLM 기반 코드 리뷰 자동화 서비스','OPEN',1,1,2,4,'2026-03-05 10:00:00','2026-03-30 09:00:00'),
(14,5,10,'Deep','AI 논문 요약 및 트렌드 분석 서비스','OPEN',1,1,2,4,'2026-03-06 11:00:00','2026-03-30 09:00:00'),
(15,5,5,'Flux','LLM 기반 개인 학습 플래너','OPEN',1,1,1,4,'2026-03-07 09:00:00','2026-03-30 09:00:00'),
(16,6,6,'FinX','소액 투자 자동화 및 포트폴리오 관리 앱','OPEN',1,1,2,5,'2026-03-05 15:00:00','2026-03-28 09:00:00'),
(17,6,11,'PayLink','오픈뱅킹 기반 간편 송금 및 정산 서비스','OPEN',1,1,2,5,'2026-03-06 16:00:00','2026-03-28 09:00:00'),
(18,6,8,'CryptoVault','블록체인 기반 개인 자산 관리 서비스','OPEN',1,1,1,5,'2026-03-07 10:00:00','2026-03-28 09:00:00');

-- ============================================================
-- 12-1. TEAM_POSITIONS
-- ============================================================
INSERT INTO team_positions (team_id, position_name, required_count) VALUES
(1,'백엔드',1),(1,'AI/ML',1),(2,'AI/ML',1),(2,'백엔드',1),
(3,'백엔드',1),(3,'프론트엔드',1),(4,'XR 개발',1),(4,'백엔드',1),
(5,'AR 개발',1),(5,'UI/UX',1),(6,'VR 개발',1),(6,'백엔드',1),
(7,'데이터 엔지니어',1),(7,'백엔드',1),(8,'데이터 분석',1),(8,'시각화',1),
(9,'ML 엔지니어',1),(9,'백엔드',1),(10,'Flutter',1),(10,'백엔드',1),
(11,'React Native',1),(11,'백엔드',1),(12,'Swift',1),(12,'백엔드',1),
(13,'LLM 엔지니어',1),(13,'백엔드',1),(14,'AI/ML',1),(14,'백엔드',1),
(15,'풀스택',1),(16,'핀테크 백엔드',1),(16,'프론트엔드',1),
(17,'백엔드',1),(17,'프론트엔드',1),(18,'블록체인',1);

-- ============================================================
-- 12-2. TEAM_PRIVATE_INFOS
-- ============================================================
INSERT INTO team_private_infos (team_id, contact_type, contact_value, internal_memo, edit_token, created_at, updated_at) VALUES
(1,'DISCORD','alpha-team#1234','AI 부동산 프로젝트','tok_alpha_001','2025-12-15 10:00:00','2025-12-15 10:00:00'),
(2,'SLACK','beta-team.slack.com','채용 매칭 플랫폼 팀','tok_beta_002','2025-12-20 14:00:00','2025-12-20 14:00:00'),
(3,'KAKAO','gamma-squad-group','음성 인식 회의록 팀','tok_gamma_003','2025-12-22 11:00:00','2025-12-22 11:00:00'),
(4,'DISCORD','nova-xr#5678','XR 교육 플랫폼 개발','tok_nova_004','2026-01-20 10:00:00','2026-01-20 10:00:00'),
(5,'KAKAO','pixel-ar-group','AR 쇼핑 서비스','tok_pixel_005','2026-01-25 11:00:00','2026-01-25 11:00:00'),
(6,'SLACK','prism.slack.com','VR 오피스 협업 팀','tok_prism_006','2026-01-28 13:00:00','2026-01-28 13:00:00'),
(7,'DISCORD','dataplus#9012','미세먼지 경로 추천 프로젝트','tok_data_007','2026-02-10 09:00:00','2026-02-10 09:00:00'),
(8,'SLACK','insight.slack.com','공공데이터 복지 분석 팀','tok_insight_008','2026-02-12 11:00:00','2026-02-12 11:00:00'),
(9,'KAKAO','sigma-data-group','상권 분석 팀','tok_sigma_009','2026-02-15 09:00:00','2026-02-15 09:00:00'),
(10,'DISCORD','appmakers#3456','운동 챌린지 앱 개발','tok_app_010','2026-02-20 10:00:00','2026-02-20 10:00:00'),
(11,'KAKAO','mobilex-group','지역 장터 앱','tok_mobile_011','2026-02-22 14:00:00','2026-02-22 14:00:00'),
(12,'DISCORD','swiftui-dev#7890','약속 앱 개발 채널','tok_swift_012','2026-02-24 10:00:00','2026-02-24 10:00:00'),
(13,'DISCORD','spark-llm#7890','LLM 코드 리뷰 서비스','tok_spark_013','2026-03-05 10:00:00','2026-03-05 10:00:00'),
(14,'SLACK','deep-ai.slack.com','AI 논문 요약 서비스','tok_deep_014','2026-03-06 11:00:00','2026-03-06 11:00:00'),
(15,'KAKAO','flux-llm-group','LLM 학습 플래너 팀','tok_flux_015','2026-03-07 09:00:00','2026-03-07 09:00:00'),
(16,'DISCORD','finx-dev#2345','소액 투자 앱 개발','tok_finx_016','2026-03-05 15:00:00','2026-03-05 15:00:00'),
(17,'SLACK','paylink.slack.com','간편 송금 서비스 팀','tok_pay_017','2026-03-06 16:00:00','2026-03-06 16:00:00'),
(18,'KAKAO','cryptovault-group','블록체인 자산 관리 팀','tok_crypto_018','2026-03-07 10:00:00','2026-03-07 10:00:00');

-- ============================================================
-- 13. TEAM_MEMBERS
-- ============================================================
INSERT INTO team_members (team_id, user_id, role_type, position, joined_at) VALUES
-- H1
(1,4,'OWNER','백엔드','2025-12-15 10:00:00'),(1,5,'MEMBER','AI/ML','2025-12-16 11:00:00'),
(2,6,'OWNER','AI/ML','2025-12-20 14:00:00'),(2,7,'MEMBER','백엔드','2025-12-21 10:00:00'),
(3,8,'OWNER','백엔드','2025-12-22 11:00:00'),(3,9,'MEMBER','프론트엔드','2025-12-23 10:00:00'),
-- H2
(4,4,'OWNER','백엔드','2026-01-20 10:00:00'),(4,10,'MEMBER','XR 개발','2026-01-21 09:00:00'),
(5,5,'OWNER','AR 개발','2026-01-25 11:00:00'),(5,11,'MEMBER','UI/UX','2026-01-26 10:00:00'),
(6,6,'OWNER','VR 개발','2026-01-28 13:00:00'),(6,12,'MEMBER','백엔드','2026-01-29 11:00:00'),
-- H3
(7,7,'OWNER','데이터 엔지니어','2026-02-10 09:00:00'),(7,8,'MEMBER','백엔드','2026-02-11 10:00:00'),
(8,9,'OWNER','데이터 분석','2026-02-12 11:00:00'),(8,10,'MEMBER','시각화','2026-02-13 10:00:00'),
(9,11,'OWNER','ML 엔지니어','2026-02-15 09:00:00'),(9,12,'MEMBER','백엔드','2026-02-16 10:00:00'),
-- H4
(10,4,'OWNER','Flutter','2026-02-20 10:00:00'),(10,7,'MEMBER','백엔드','2026-02-21 11:00:00'),
(11,5,'OWNER','React Native','2026-02-22 14:00:00'),(11,11,'MEMBER','백엔드','2026-02-23 09:00:00'),
(12,6,'OWNER','Swift','2026-02-24 10:00:00'),(12,12,'MEMBER','백엔드','2026-02-25 11:00:00'),
-- H5
(13,8,'OWNER','LLM 엔지니어','2026-03-05 10:00:00'),(13,9,'MEMBER','백엔드','2026-03-06 11:00:00'),
(14,10,'OWNER','AI/ML','2026-03-06 11:00:00'),(14,4,'MEMBER','백엔드','2026-03-07 09:00:00'),
(15,5,'OWNER','풀스택','2026-03-07 09:00:00'),
-- H6
(16,6,'OWNER','핀테크 백엔드','2026-03-05 15:00:00'),(16,7,'MEMBER','프론트엔드','2026-03-06 16:00:00'),
(17,11,'OWNER','백엔드','2026-03-06 16:00:00'),(17,12,'MEMBER','프론트엔드','2026-03-07 10:00:00'),
(18,8,'OWNER','블록체인','2026-03-07 10:00:00');

-- ============================================================
-- 14. TEAM_APPLICATIONS
-- ============================================================
INSERT INTO team_applications (team_id, user_id, message, position, status, processed_by_user_id, created_at, processed_at) VALUES
(1,5,'AI 모델 개발 경험이 있습니다.','AI/ML','ACCEPTED',4,'2025-12-16 10:00:00','2025-12-16 11:00:00'),
(2,7,'데이터 분석 및 백엔드 개발 가능합니다.','백엔드','ACCEPTED',6,'2025-12-21 09:00:00','2025-12-21 10:00:00'),
(3,9,'프론트엔드 React 2년 경험 있습니다.','프론트엔드','ACCEPTED',8,'2025-12-23 09:00:00','2025-12-23 10:00:00'),
(4,10,'Unity XR 개발 1년 경험 있습니다.','XR 개발','ACCEPTED',4,'2026-01-21 08:00:00','2026-01-21 09:00:00'),
(5,11,'ARKit 사용 경험 있습니다.','UI/UX','ACCEPTED',5,'2026-01-26 09:00:00','2026-01-26 10:00:00'),
(6,12,'WebXR 및 Three.js 개발 가능합니다.','백엔드','ACCEPTED',6,'2026-01-29 10:00:00','2026-01-29 11:00:00'),
(7,8,'Python 데이터 분석 잘합니다.','백엔드','ACCEPTED',7,'2026-02-11 09:00:00','2026-02-11 10:00:00'),
(8,10,'Tableau, D3.js 시각화 경험 있습니다.','시각화','ACCEPTED',9,'2026-02-13 09:00:00','2026-02-13 10:00:00'),
(9,12,'PyTorch ML 모델링 가능합니다.','백엔드','ACCEPTED',11,'2026-02-16 09:00:00','2026-02-16 10:00:00'),
(10,7,'Flutter 앱 개발 경험 있습니다.','백엔드','ACCEPTED',4,'2026-02-21 10:00:00','2026-02-21 11:00:00'),
(11,11,'React Native 및 iOS 개발 가능합니다.','백엔드','ACCEPTED',5,'2026-02-23 08:00:00','2026-02-23 09:00:00'),
(12,12,'Swift UI 경험 있습니다.','백엔드','ACCEPTED',6,'2026-02-25 10:00:00','2026-02-25 11:00:00'),
(13,9,'LLM 프롬프트 엔지니어링 경험 있습니다.','백엔드','ACCEPTED',8,'2026-03-06 10:00:00','2026-03-06 11:00:00'),
(14,4,'LangChain RAG 파이프라인 구축 경험 있습니다.','백엔드','ACCEPTED',10,'2026-03-07 08:00:00','2026-03-07 09:00:00'),
(16,7,'핀테크 도메인 지식 보유.','프론트엔드','ACCEPTED',6,'2026-03-06 15:00:00','2026-03-06 16:00:00'),
(17,12,'Vue.js 프론트엔드 개발 가능합니다.','프론트엔드','ACCEPTED',11,'2026-03-07 09:00:00','2026-03-07 10:00:00');

-- ============================================================
-- 15. HACKATHON_REGISTRATIONS
-- ============================================================
INSERT INTO hackathon_registrations (hackathon_id, team_id, registered_at) VALUES
(1,1,'2025-12-15 10:00:00'),(1,2,'2025-12-20 14:00:00'),(1,3,'2025-12-22 11:00:00'),
(2,4,'2026-01-20 10:00:00'),(2,5,'2026-01-25 11:00:00'),(2,6,'2026-01-28 13:00:00'),
(3,7,'2026-02-10 09:00:00'),(3,8,'2026-02-12 11:00:00'),(3,9,'2026-02-15 09:00:00'),
(4,10,'2026-02-20 10:00:00'),(4,11,'2026-02-22 14:00:00'),(4,12,'2026-02-24 10:00:00'),
(5,13,'2026-03-05 10:00:00'),(5,14,'2026-03-06 11:00:00'),(5,15,'2026-03-07 09:00:00'),
(6,16,'2026-03-05 15:00:00'),(6,17,'2026-03-06 16:00:00'),(6,18,'2026-03-07 10:00:00');

-- ============================================================
-- 16. SUBMISSIONS
-- ============================================================
INSERT INTO submissions (id, hackathon_id, team_id, submitter_user_id, status, revision_no, is_latest, submitted_at, created_at, updated_at) VALUES
-- H1 (ENDED)
(1, 1,1,4,'SUBMITTED',1,1,'2026-01-12 13:00:00','2026-01-12 13:00:00','2026-01-12 13:00:00'),
(2, 1,2,6,'SUBMITTED',1,1,'2026-01-12 13:30:00','2026-01-12 13:30:00','2026-01-12 13:30:00'),
(3, 1,3,8,'SUBMITTED',1,1,'2026-01-12 14:00:00','2026-01-12 14:00:00','2026-01-12 14:00:00'),
-- H2 (ENDED)
(4, 2,4,4,'SUBMITTED',1,1,'2026-02-16 13:00:00','2026-02-16 13:00:00','2026-02-16 13:00:00'),
(5, 2,5,5,'SUBMITTED',1,1,'2026-02-16 13:30:00','2026-02-16 13:30:00','2026-02-16 13:30:00'),
(6, 2,6,6,'SUBMITTED',1,1,'2026-02-16 14:00:00','2026-02-16 14:00:00','2026-02-16 14:00:00'),
-- H3 (CLOSED) - revision 포함
(7, 3,7,7,'SUBMITTED',2,1,'2026-03-09 13:30:00','2026-03-09 13:30:00','2026-03-09 13:30:00'),
(8, 3,7,7,'SUBMITTED',1,0,'2026-03-09 10:00:00','2026-03-09 10:00:00','2026-03-09 13:30:00'),
(9, 3,8,9,'SUBMITTED',1,1,'2026-03-09 13:00:00','2026-03-09 13:00:00','2026-03-09 13:00:00'),
(10,3,9,11,'SUBMITTED',1,1,'2026-03-09 14:00:00','2026-03-09 14:00:00','2026-03-09 14:00:00'),
-- H4 (CLOSED)
(11,4,10,4,'SUBMITTED',1,1,'2026-03-23 13:00:00','2026-03-23 13:00:00','2026-03-23 13:00:00'),
(12,4,11,5,'SUBMITTED',2,1,'2026-03-23 14:30:00','2026-03-23 14:30:00','2026-03-23 14:30:00'),
(13,4,11,5,'SUBMITTED',1,0,'2026-03-23 11:00:00','2026-03-23 11:00:00','2026-03-23 14:30:00'),
(14,4,12,6,'SUBMITTED',1,1,'2026-03-23 13:30:00','2026-03-23 13:30:00','2026-03-23 13:30:00'),
-- H5 (OPEN, 진행중)
(15,5,13,8,'SUBMITTED',1,1,'2026-04-01 08:00:00','2026-04-01 08:00:00','2026-04-01 08:00:00'),
(16,5,14,10,'SUBMITTED',1,1,'2026-04-02 09:00:00','2026-04-02 09:00:00','2026-04-02 09:00:00'),
-- H6 (OPEN, 진행중)
(17,6,16,6,'SUBMITTED',1,1,'2026-04-01 07:00:00','2026-04-01 07:00:00','2026-04-01 07:00:00'),
(18,6,17,11,'SUBMITTED',1,1,'2026-04-02 08:00:00','2026-04-02 08:00:00','2026-04-02 08:00:00');

-- ============================================================
-- 17. SUBMISSION_ITEMS
-- H3 제출물에 파일(fileName) 포함 → 심사위원 다운로드 테스트용
-- ============================================================
INSERT INTO submission_items (submission_id, rule_id, value_text, value_url, file_name, original_file_name, file_extension, file_size, is_final) VALUES
-- Submission 1 (H1, Alpha Team)
(1,NULL,NULL,'https://github.com/alpha-team/ai-real-estate',NULL,NULL,NULL,NULL,1),
(1,NULL,NULL,'https://slides.google.com/alpha-pitch',NULL,NULL,NULL,NULL,1),
(1,NULL,NULL,'https://youtu.be/alpha-demo-2026',NULL,NULL,NULL,NULL,1),
(1,NULL,'AI와 NLP 기술로 부동산 허위 매물을 탐지하는 서비스. BERT 파인튜닝으로 정확도 92% 달성.',NULL,NULL,NULL,NULL,NULL,1),
-- Submission 2 (H1, Beta Team)
(2,NULL,NULL,'https://github.com/beta-team/ai-job-match',NULL,NULL,NULL,NULL,1),
(2,NULL,NULL,'https://slides.google.com/beta-pitch',NULL,NULL,NULL,NULL,1),
(2,NULL,'중소기업과 구직자를 AI로 매칭하는 플랫폼. 기업 문화와 지원자 패턴을 분석하여 최적 매칭 제안.',NULL,NULL,NULL,NULL,NULL,1),
-- Submission 3 (H1, Gamma Squad)
(3,NULL,NULL,'https://github.com/gamma/meeting-transcriber',NULL,NULL,NULL,NULL,1),
(3,NULL,NULL,'https://slides.google.com/gamma-pitch',NULL,NULL,NULL,NULL,1),
(3,NULL,NULL,'https://youtu.be/gamma-demo-2026',NULL,NULL,NULL,NULL,1),
(3,NULL,'Whisper API와 GPT-4를 결합하여 실시간 회의 내용을 요약하고 액션 아이템을 추출하는 서비스.',NULL,NULL,NULL,NULL,NULL,1),
-- Submission 4 (H2, Nova)
(4,NULL,NULL,'https://github.com/nova-team/xr-edu',NULL,NULL,NULL,NULL,1),
(4,NULL,NULL,'https://youtu.be/nova-xr-demo',NULL,NULL,NULL,NULL,1),
(4,NULL,'Meta Quest 3를 활용한 실시간 원격 교육 플랫폼. 교사와 학생이 같은 가상 공간에서 실험 실습 가능.',NULL,NULL,NULL,NULL,NULL,1),
-- Submission 5 (H2, Pixel)
(5,NULL,NULL,'https://github.com/pixel-team/ar-furniture',NULL,NULL,NULL,NULL,1),
(5,NULL,NULL,'https://youtu.be/pixel-ar-demo',NULL,NULL,NULL,NULL,1),
(5,NULL,'AR 기술을 활용한 가구 배치 시뮬레이션 앱. 실내 공간 스캔 후 실제 크기 3D 가구 배치 가능.',NULL,NULL,NULL,NULL,NULL,1),
-- Submission 6 (H2, Prism)
(6,NULL,NULL,'https://github.com/prism-team/vr-office',NULL,NULL,NULL,NULL,1),
(6,NULL,NULL,'https://youtu.be/prism-vr-demo',NULL,NULL,NULL,NULL,1),
(6,NULL,'VR 가상 오피스에서 팀원과 협업하는 플랫폼. 3D 화이트보드와 화상회의 통합.',NULL,NULL,NULL,NULL,NULL,1),
-- Submission 7 (H3, Data+ - 최신, revision 2) - 파일 포함!
(7,NULL,NULL,'https://github.com/dataplus/finedust-route',NULL,NULL,NULL,NULL,1),
(7,NULL,NULL,'https://slides.google.com/dataplus-v2',NULL,NULL,NULL,NULL,1),
(7,NULL,NULL,NULL,'daker/submissions/hackathon-3/team-7/a1b2c3d4_analysis_report_v2.pdf','분석보고서_v2.pdf','pdf',2457600,1),
(7,NULL,'서울시 미세먼지 공공데이터 3년치 분석. 시간대별 오염도 패턴 기반 최적 이동 경로 추천. RMSE 0.82 달성.',NULL,NULL,NULL,NULL,NULL,1),
-- Submission 9 (H3, Insight) - 파일 포함!
(9,NULL,NULL,'https://github.com/insight-team/welfare-gap',NULL,NULL,NULL,NULL,1),
(9,NULL,NULL,'https://slides.google.com/insight-welfare',NULL,NULL,NULL,NULL,1),
(9,NULL,NULL,NULL,'daker/submissions/hackathon-3/team-8/e5f6g7h8_welfare_analysis_report.pdf','복지사각지대_분석보고서.pdf','pdf',1843200,1),
(9,NULL,'통계청 + 서울시 공공데이터 결합 분석. 지역별 1인 가구 밀집도와 복지 시설 분포 교차 분석으로 복지 사각지대 17개 구역 식별.',NULL,NULL,NULL,NULL,NULL,1),
-- Submission 10 (H3, Sigma) - 파일 포함!
(10,NULL,NULL,'https://github.com/sigma-team/startup-location',NULL,NULL,NULL,NULL,1),
(10,NULL,NULL,'https://slides.google.com/sigma-startup',NULL,NULL,NULL,NULL,1),
(10,NULL,NULL,NULL,'daker/submissions/hackathon-3/team-9/i9j0k1l2_startup_location_report.pdf','창업입지분석_보고서.pdf','pdf',3145728,1),
(10,NULL,'전국 폐업 상권 데이터 5년치 분석. 생존율 높은 업종과 입지 조건 상관관계 분석으로 창업 성공 입지 추천 모델 개발.',NULL,NULL,NULL,NULL,NULL,1),
-- Submission 11 (H4, AppMakers)
(11,NULL,NULL,'https://github.com/appmakers/workout-challenge',NULL,NULL,NULL,NULL,1),
(11,NULL,NULL,'https://apps.apple.com/appmakers-workout',NULL,NULL,NULL,NULL,1),
(11,NULL,'운동 루틴 공유와 친구 챌린지 소셜 피트니스 앱. Flutter로 iOS/Android 동시 출시.',NULL,NULL,NULL,NULL,NULL,1),
-- Submission 12 (H4, MobileX - 최신)
(12,NULL,NULL,'https://github.com/mobilex/local-market',NULL,NULL,NULL,NULL,1),
(12,NULL,NULL,'https://play.google.com/mobilex-market',NULL,NULL,NULL,NULL,1),
(12,NULL,'위치 기반 반경 1km 소상공인 오늘의 특가 및 재고 실시간 확인 앱. 하이퍼로컬 마켓.',NULL,NULL,NULL,NULL,NULL,1),
-- Submission 14 (H4, SwiftUI)
(14,NULL,NULL,'https://github.com/swiftui-team/schedule-app',NULL,NULL,NULL,NULL,1),
(14,NULL,NULL,'https://apps.apple.com/swiftui-schedule',NULL,NULL,NULL,NULL,1),
(14,NULL,'시간표 기반 공통 자유 시간을 자동으로 찾아 약속을 잡아주는 앱. SwiftUI + Kotlin 개발.',NULL,NULL,NULL,NULL,NULL,1),
-- Submission 15 (H5, Spark)
(15,NULL,NULL,'https://github.com/spark-team/llm-code-review',NULL,NULL,NULL,NULL,1),
(15,NULL,NULL,'https://spark-review.vercel.app',NULL,NULL,NULL,NULL,1),
(15,NULL,'Claude API로 GitHub PR에 자동 코드 리뷰 코멘트. 버그, 보안 취약점, 코드 스타일 자동 제안.',NULL,NULL,NULL,NULL,NULL,1),
-- Submission 16 (H5, Deep)
(16,NULL,NULL,'https://github.com/deep-team/paper-summary',NULL,NULL,NULL,NULL,1),
(16,NULL,NULL,'https://deep-research.vercel.app',NULL,NULL,NULL,NULL,1),
(16,NULL,'arXiv 최신 논문을 GPT-4o로 3줄 요약하고 트렌드 키워드를 추출하는 서비스.',NULL,NULL,NULL,NULL,NULL,1),
-- Submission 17 (H6, FinX)
(17,NULL,NULL,'https://github.com/finx-team/micro-invest',NULL,NULL,NULL,NULL,1),
(17,NULL,NULL,'https://youtu.be/finx-demo',NULL,NULL,NULL,NULL,1),
(17,NULL,'카드 결제 잔돈을 자동으로 ETF에 투자하고 AI 포트폴리오 리밸런싱 앱.',NULL,NULL,NULL,NULL,NULL,1),
-- Submission 18 (H6, PayLink)
(18,NULL,NULL,'https://github.com/paylink-team/easy-transfer',NULL,NULL,NULL,NULL,1),
(18,NULL,NULL,'https://paylink-demo.vercel.app',NULL,NULL,NULL,NULL,1),
(18,NULL,'오픈뱅킹 API 기반 여러 은행 계좌를 한 앱에서 관리하고 정산하는 간편 송금 서비스.',NULL,NULL,NULL,NULL,NULL,1);

-- ============================================================
-- 18. JUDGE_EVALUATIONS (올바른 scores_json 포맷: [점수1, 점수2, ...])
-- H1 기준: [아이디어혁신성(30), 기술완성도(30), 비즈니스가능성(30), 발표력(10)]
-- H3 기준: [데이터활용도(30), 분석정확도(40), 시각화품질(20), 발표력(10)]
-- H5 기준: [AI활용도(30), 기술완성도(30), 비즈니스임팩트(30), 발표력(10)]
-- H6 기준: [금융혁신성(35), 보안안전성(35), 사용자편의성(20), 발표력(10)]
-- 결과: H1(team1 1등88, team2 2등75, team3 3등62)
--       H3(team7 1등91, team8 2등78, team9 3등65)
-- ============================================================
INSERT INTO judge_evaluations (hackathon_id, team_id, judge_user_id, total_score, scores_json, created_at, updated_at) VALUES
-- H1 judge1(2) 심사
(1,1,2,88.0,'[28.0,27.0,26.0,7.0]','2026-01-12 18:00:00','2026-01-12 18:00:00'),
(1,2,2,75.0,'[22.0,25.0,22.0,6.0]','2026-01-12 18:15:00','2026-01-12 18:15:00'),
(1,3,2,62.0,'[18.0,20.0,17.0,7.0]','2026-01-12 18:30:00','2026-01-12 18:30:00'),
-- H1 judge2(3) 심사
(1,1,3,90.0,'[28.0,29.0,25.0,8.0]','2026-01-12 18:05:00','2026-01-12 18:05:00'),
(1,2,3,73.0,'[22.0,24.0,20.0,7.0]','2026-01-12 18:20:00','2026-01-12 18:20:00'),
(1,3,3,60.0,'[17.0,19.0,17.0,7.0]','2026-01-12 18:35:00','2026-01-12 18:35:00'),
-- H3 judge1(2) 심사
(3,7,2,91.0,'[28.0,38.0,18.0,7.0]','2026-03-09 18:00:00','2026-03-09 18:00:00'),
(3,8,2,78.0,'[24.0,32.0,16.0,6.0]','2026-03-09 18:15:00','2026-03-09 18:15:00'),
(3,9,2,65.0,'[20.0,26.0,13.0,6.0]','2026-03-09 18:30:00','2026-03-09 18:30:00'),
-- H3 judge2(3) 심사
(3,7,3,92.0,'[27.0,38.0,19.0,8.0]','2026-03-09 18:05:00','2026-03-09 18:05:00'),
(3,8,3,79.0,'[25.0,32.0,15.0,7.0]','2026-03-09 18:20:00','2026-03-09 18:20:00'),
(3,9,3,64.0,'[21.0,25.0,11.0,7.0]','2026-03-09 18:35:00','2026-03-09 18:35:00'),
-- H5 judge1(2) 심사 (OPEN, 일부 진행)
(5,13,2,83.0,'[25.0,26.0,24.0,8.0]','2026-04-03 14:00:00','2026-04-03 14:00:00'),
(5,14,2,70.0,'[22.0,20.0,21.0,7.0]','2026-04-03 14:30:00','2026-04-03 14:30:00'),
-- H5 judge2(3) 심사 (OPEN, 일부 진행)
(5,13,3,85.0,'[26.0,27.0,24.0,8.0]','2026-04-03 15:00:00','2026-04-03 15:00:00'),
-- H6 judge1(2) 심사 (OPEN, 일부 진행)
(6,16,2,82.0,'[28.0,30.0,16.0,8.0]','2026-04-04 10:00:00','2026-04-04 10:00:00'),
(6,17,2,75.0,'[25.0,27.0,17.0,6.0]','2026-04-04 10:30:00','2026-04-04 10:30:00');

-- ============================================================
-- 19. VOTES (VOTE 타입 ENDED/CLOSED)
-- H2: team4(Nova) 6표, team5(Pixel) 4표, team6(Prism) 2표
-- H4: team10(AppMakers) 6표, team11(MobileX) 5표, team12(SwiftUI) 1표
-- ============================================================
INSERT INTO votes (hackathon_id, voter_id, team_id, voted_at) VALUES
-- H2 투표
-- team4(user1=4, user7=10 소속) → team5 또는 team6 투표
(2,4,5,'2026-02-16 15:10:00'),   -- user1 → Pixel
(2,10,6,'2026-02-16 15:15:00'),  -- user7 → Prism
-- team5(user2=5, user8=11 소속) → team4 또는 team6 투표
(2,5,4,'2026-02-16 15:20:00'),   -- user2 → Nova
(2,11,4,'2026-02-16 15:25:00'),  -- user8 → Nova
-- team6(user3=6, user9=12 소속) → team4 또는 team5 투표
(2,6,5,'2026-02-16 15:30:00'),   -- user3 → Pixel
(2,12,4,'2026-02-16 15:35:00'),  -- user9 → Nova
-- 비소속 유저 자유 투표
(2,1,4,'2026-02-16 16:00:00'),   -- admin → Nova
(2,2,5,'2026-02-16 16:05:00'),   -- judge1 → Pixel
(2,3,4,'2026-02-16 16:10:00'),   -- judge2 → Nova
(2,7,4,'2026-02-16 16:15:00'),   -- user4 → Nova
(2,8,6,'2026-02-16 16:20:00'),   -- user5 → Prism
(2,9,5,'2026-02-16 16:25:00'),   -- user6 → Pixel

-- H4 투표
-- team10(user1=4, user4=7 소속) → team11 또는 team12 투표
(4,4,11,'2026-03-23 15:10:00'),  -- user1 → MobileX
(4,7,12,'2026-03-23 15:15:00'),  -- user4 → SwiftUI
-- team11(user2=5, user8=11 소속) → team10 또는 team12 투표
(4,5,10,'2026-03-23 15:20:00'),  -- user2 → AppMakers
(4,11,10,'2026-03-23 15:25:00'), -- user8 → AppMakers
-- team12(user3=6, user9=12 소속) → team10 또는 team11 투표
(4,6,11,'2026-03-23 15:30:00'),  -- user3 → MobileX
(4,12,10,'2026-03-23 15:35:00'), -- user9 → AppMakers
-- 비소속 유저 자유 투표
(4,1,10,'2026-03-23 16:00:00'),  -- admin → AppMakers
(4,2,10,'2026-03-23 16:05:00'),  -- judge1 → AppMakers
(4,3,11,'2026-03-23 16:10:00'),  -- judge2 → MobileX
(4,8,11,'2026-03-23 16:15:00'),  -- user5 → MobileX
(4,9,10,'2026-03-23 16:20:00'),  -- user6 → AppMakers
(4,10,11,'2026-03-23 16:25:00'); -- user7 → MobileX

-- ============================================================
-- 20. USER_XP_HISTORY
-- H1(SCORE): team1(1등) > team2(2등) > team3(3등)
-- H2(VOTE):  Nova(team4,6표,1등) > Pixel(team5,4표,2등) > Prism(team6,2표,3등)
-- H3(SCORE): team7(1등) > team8(2등) > team9(3등)
-- H4(VOTE):  AppMakers(team10,6표,1등) > MobileX(team11,5표,2등) > SwiftUI(team12,1표,3등)
--
-- XP 합계:
--   user1(4):  H1-1st(500) + H2-1st(500) + H4-1st(500) = 1500
--   user2(5):  H1-1st(500) + H2-2nd(300) + H4-2nd(300) = 1100
--   user3(6):  H1-2nd(300) + H2-3rd(200) + H4-3rd(200) = 700
--   user4(7):  H1-2nd(300) + H3-1st(500) + H4-1st(500) = 1300
--   user5(8):  H1-3rd(200) + H3-1st(500)               = 700
--   user6(9):  H1-3rd(200) + H3-2nd(300)               = 500
--   user7(10): H2-1st(500) + H3-2nd(300)               = 800
--   user8(11): H2-2nd(300) + H3-3rd(200) + H4-2nd(300) = 800
--   user9(12): H2-3rd(200) + H3-3rd(200) + H4-3rd(200) = 600
-- ============================================================
INSERT INTO user_xp_history (user_id, hackathon_id, type, amount, earned_at) VALUES
-- H1 수상
(4,1,'AWARD_1ST',500,'2026-01-12 18:00:00'), -- user1 (team1 1등)
(5,1,'AWARD_1ST',500,'2026-01-12 18:00:00'), -- user2 (team1 1등)
(6,1,'AWARD_2ND',300,'2026-01-12 18:00:00'), -- user3 (team2 2등)
(7,1,'AWARD_2ND',300,'2026-01-12 18:00:00'), -- user4 (team2 2등)
(8,1,'AWARD_3RD',200,'2026-01-12 18:00:00'), -- user5 (team3 3등)
(9,1,'AWARD_3RD',200,'2026-01-12 18:00:00'), -- user6 (team3 3등)
-- H2 수상
(4,2,'AWARD_1ST',500,'2026-02-16 18:00:00'), -- user1 (Nova 1등)
(10,2,'AWARD_1ST',500,'2026-02-16 18:00:00'),-- user7 (Nova 1등)
(5,2,'AWARD_2ND',300,'2026-02-16 18:00:00'), -- user2 (Pixel 2등)
(11,2,'AWARD_2ND',300,'2026-02-16 18:00:00'),-- user8 (Pixel 2등)
(6,2,'AWARD_3RD',200,'2026-02-16 18:00:00'), -- user3 (Prism 3등)
(12,2,'AWARD_3RD',200,'2026-02-16 18:00:00'),-- user9 (Prism 3등)
-- H3 수상
(7,3,'AWARD_1ST',500,'2026-03-09 18:00:00'), -- user4 (Data+ 1등)
(8,3,'AWARD_1ST',500,'2026-03-09 18:00:00'), -- user5 (Data+ 1등)
(9,3,'AWARD_2ND',300,'2026-03-09 18:00:00'), -- user6 (Insight 2등)
(10,3,'AWARD_2ND',300,'2026-03-09 18:00:00'),-- user7 (Insight 2등)
(11,3,'AWARD_3RD',200,'2026-03-09 18:00:00'),-- user8 (Sigma 3등)
(12,3,'AWARD_3RD',200,'2026-03-09 18:00:00'),-- user9 (Sigma 3등)
-- H4 수상
(4,4,'AWARD_1ST',500,'2026-03-23 18:00:00'), -- user1 (AppMakers 1등)
(7,4,'AWARD_1ST',500,'2026-03-23 18:00:00'), -- user4 (AppMakers 1등)
(5,4,'AWARD_2ND',300,'2026-03-23 18:00:00'), -- user2 (MobileX 2등)
(11,4,'AWARD_2ND',300,'2026-03-23 18:00:00'),-- user8 (MobileX 2등)
(6,4,'AWARD_3RD',200,'2026-03-23 18:00:00'), -- user3 (SwiftUI 3등)
(12,4,'AWARD_3RD',200,'2026-03-23 18:00:00');-- user9 (SwiftUI 3등)

-- ============================================================
-- 21. USER_TAGS
-- ============================================================
INSERT INTO user_tags (user_id, tag_id) VALUES
(4,1),(4,2),(4,3),       -- user1: AI, 스타트업, 머신러닝
(5,12),(5,15),(5,14),    -- user2: 모바일, Flutter, Android
(6,8),(6,9),(6,10),      -- user3: 데이터분석, 빅데이터, Python
(7,1),(7,16),(7,18),     -- user4: AI, LLM, 생성형AI
(8,8),(8,11),(8,10),     -- user5: 데이터분석, ML, Python
(9,19),(9,20),(9,21),    -- user6: 핀테크, 블록체인, 금융
(10,4),(10,5),(10,7),    -- user7: XR, 메타버스, AR
(11,12),(11,13),(11,15), -- user8: 모바일, iOS, Flutter
(12,32),(12,33),(12,34); -- user9: 보안, CTF, 취약점분석

-- ============================================================
-- 추가 해커톤 (진행중 2개, 모집중 2개)
-- H11: 웹3 블록체인 해커톤 (CLOSED/SCORE) - 진행중
-- H12: 에듀테크 챌린지 (CLOSED/VOTE) - 진행중
-- H13: 소셜임팩트 해커톤 (OPEN/SCORE) - 모집중
-- H14: 클라우드 네이티브 챌린지 (OPEN/VOTE) - 모집중
-- ============================================================

INSERT INTO hackathons (id, title, summary, description, thumbnail_url, organizer, status, score_type,
    start_date, end_date, registration_start_date, registration_end_date, submission_deadline_at,
    closed_at, max_team_size, max_participants, camp_enabled, allow_solo, deleted, created_at, updated_at) VALUES
(11, '웹3 블록체인 해커톤 2026',
 '블록체인 기술로 탈중앙화 서비스를 48시간 안에 구현하세요',
 'EVM 기반 스마트 컨트랙트, DeFi, NFT 등 다양한 웹3 기술을 활용하여 실제 문제를 해결하는 dApp을 개발합니다.',
 'https://picsum.photos/seed/hack11/800/400', 'Daker', 'CLOSED', 'SCORE',
 '2026-03-31 09:00:00', '2026-04-14 18:00:00',
 '2026-02-20 00:00:00', '2026-03-26 23:59:59', '2026-04-14 15:00:00',
 '2026-03-27 09:00:00', 4, 100, 0, 0, 0, '2026-02-10 00:00:00', '2026-03-31 09:00:00'),
(12, '에듀테크 이노베이션 챌린지',
 'AI와 데이터로 교육의 미래를 바꾸는 서비스를 만들어보세요',
 '개인 맞춤 학습, 학습 분석, 교육 접근성 등 교육 문제를 기술로 해결하는 서비스를 개발합니다.',
 'https://picsum.photos/seed/hack12/800/400', 'Daker', 'CLOSED', 'VOTE',
 '2026-04-01 09:00:00', '2026-04-13 18:00:00',
 '2026-02-25 00:00:00', '2026-03-28 23:59:59', '2026-04-03 15:00:00',
 '2026-03-29 09:00:00', 5, 80, 1, 1, 0, '2026-02-15 00:00:00', '2026-04-01 09:00:00'),
(13, '소셜임팩트 해커톤 2026',
 '기술로 사회 문제를 해결하는 임팩트 있는 서비스를 개발하세요',
 '환경, 복지, 교육 불평등 등 사회 문제를 해결하는 기술 솔루션을 개발합니다. 우수팀에게는 사회적기업 인큐베이팅 기회가 제공됩니다.',
 'https://picsum.photos/seed/hack13/800/400', 'Daker', 'OPEN', 'SCORE',
 '2026-04-25 09:00:00', '2026-04-27 18:00:00',
 '2026-03-20 00:00:00', '2026-04-20 23:59:59', '2026-04-27 15:00:00',
 NULL, 4, 120, 0, 1, 0, '2026-03-10 00:00:00', '2026-03-20 00:00:00'),
(14, '클라우드 네이티브 챌린지',
 'Kubernetes, MSA 기반의 클라우드 네이티브 서비스를 설계하고 배포하세요',
 '도커, 쿠버네티스, 서비스 메시 등 클라우드 네이티브 기술 스택을 활용한 확장 가능한 서비스를 구축합니다.',
 'https://picsum.photos/seed/hack14/800/400', 'Daker', 'OPEN', 'VOTE',
 '2026-04-22 09:00:00', '2026-04-24 18:00:00',
 '2026-03-25 00:00:00', '2026-04-18 23:59:59', '2026-04-24 15:00:00',
 NULL, 3, 60, 0, 0, 0, '2026-03-15 00:00:00', '2026-03-25 00:00:00');

INSERT INTO tags (id, name) VALUES
(35,'웹3'),(36,'DeFi'),(37,'NFT'),(38,'스마트컨트랙트'),
(39,'에듀테크'),(40,'교육'),(41,'온라인학습'),
(42,'사회혁신'),(43,'임팩트'),(44,'공익'),
(45,'클라우드'),(46,'DevOps'),(47,'쿠버네티스'),(48,'MSA');

INSERT INTO hackathon_tags (hackathon_id, tag_id) VALUES
(11,35),(11,36),(11,37),(11,38),(11,20),
(12,39),(12,40),(12,41),(12,1),
(13,42),(13,43),(13,44),(13,8),
(14,45),(14,46),(14,47),(14,48);

INSERT INTO milestones (hackathon_id, title, description, date) VALUES
(11,'참가 접수 시작',NULL,'2026-02-20 00:00:00'),
(11,'참가 접수 마감',NULL,'2026-03-26 23:59:59'),
(11,'해커톤 시작','스마트 컨트랙트 코드 공개','2026-03-31 09:00:00'),
(11,'중간 점검','팀별 진행 상황 발표','2026-04-07 14:00:00'),
(11,'제출 마감',NULL,'2026-04-14 15:00:00'),
(11,'시상식',NULL,'2026-04-14 18:00:00'),
(12,'참가 접수 시작',NULL,'2026-02-25 00:00:00'),
(12,'참가 접수 마감',NULL,'2026-03-28 23:59:59'),
(12,'해커톤 시작','데이터셋 및 API 배포','2026-04-01 09:00:00'),
(12,'제출 마감',NULL,'2026-04-13 15:00:00'),
(12,'투표 종료 및 시상',NULL,'2026-04-13 18:00:00'),
(13,'참가 접수 시작',NULL,'2026-03-20 00:00:00'),
(13,'참가 접수 마감',NULL,'2026-04-20 23:59:59'),
(13,'해커톤 시작',NULL,'2026-04-25 09:00:00'),
(13,'최종 발표 및 시상',NULL,'2026-04-27 17:00:00'),
(14,'참가 접수 시작',NULL,'2026-03-25 00:00:00'),
(14,'참가 접수 마감',NULL,'2026-04-18 23:59:59'),
(14,'해커톤 시작',NULL,'2026-04-22 09:00:00'),
(14,'최종 발표 및 시상',NULL,'2026-04-24 17:00:00');

INSERT INTO prizes (hackathon_id, ranking, amount, description) VALUES
(11,1,10000000,'대상'),(11,2,5000000,'최우수상'),(11,3,2000000,'우수상'),
(12,1,7000000,'대상'),(12,2,3000000,'최우수상'),(12,3,1000000,'우수상'),
(13,1,15000000,'대상 - 사회적기업 인큐베이팅'),(13,2,7000000,'최우수상'),(13,3,3000000,'우수상'),
(14,1,12000000,'대상'),(14,2,6000000,'최우수상'),(14,3,2500000,'우수상');

INSERT INTO criteria (hackathon_id, name, description, max_score) VALUES
(11,'블록체인 기술 활용도','스마트 컨트랙트 및 웹3 기술 활용 수준',35),
(11,'서비스 완성도','dApp 구현 및 배포 완성도',30),
(11,'탈중앙화 철학','실질적인 탈중앙화 구현 여부',25),
(11,'발표력',NULL,10),
(13,'사회적 임팩트','해결하는 사회 문제의 심각성 및 임팩트 크기',40),
(13,'기술 완성도','서비스 구현 수준',30),
(13,'지속 가능성','사업 지속 가능성 및 확장 가능성',20),
(13,'발표력',NULL,10);

INSERT INTO hackathon_submission_rules (hackathon_id, artifact_type, required, label, description, sort_order) VALUES
(11,'url', 1,'GitHub 저장소','스마트 컨트랙트 및 프론트엔드 코드',1),
(11,'url', 1,'배포된 컨트랙트 주소','Sepolia 또는 Mumbai 테스트넷 배포 주소',2),
(11,'url', 0,'데모 영상','서비스 시연 영상 링크',3),
(11,'text',1,'프로젝트 설명','서비스 개요 및 블록체인 활용 방식 (500자 이내)',4),
(13,'url', 1,'GitHub 저장소','소스코드 저장소',1),
(13,'url', 0,'서비스 URL','배포된 서비스 URL',2),
(13,'text',1,'문제 정의','해결하려는 사회 문제 및 접근 방식 (500자 이내)',3),
(13,'url', 0,'데모 영상','서비스 시연 영상',4);

INSERT INTO hackathon_notices (hackathon_id, content) VALUES
(11,'Solidity, Rust(Anchor), Move 등 자유로운 스마트 컨트랙트 언어 사용 가능합니다.'),
(11,'테스트넷 배포가 필수입니다. 메인넷 배포는 선택 사항입니다.'),
(12,'현장 참여 필수입니다. 숙박 및 식사가 제공됩니다.'),
(12,'1인 참가 가능합니다.'),
(13,'사회적 가치를 중심으로 평가합니다. 기술적 완성도보다 임팩트를 우선시합니다.'),
(14,'AWS, GCP, Azure 크레딧이 팀당 제공됩니다.');

INSERT INTO hackathon_links (hackathon_id, link_type, label, url) VALUES
(11,'WEBSITE','공식 홈페이지','https://daker.com'),
(11,'DISCORD','Web3 Discord','https://discord.gg/daker-web3'),
(12,'WEBSITE','공식 홈페이지','https://daker.com'),
(13,'WEBSITE','공식 홈페이지','https://daker.com'),
(13,'NOTION','참가자 가이드','https://notion.so/social-impact-2026'),
(14,'WEBSITE','공식 홈페이지','https://daker.com'),
(14,'DISCORD','Cloud Native Discord','https://discord.gg/daker-cloud');

-- 심사위원 배정 (judge1: H11, H12, H13 / judge2: H11)
INSERT INTO hackathon_judges (hackathon_id, user_id, assigned_at) VALUES
(11,2,'2026-03-26 00:00:00'),(11,3,'2026-03-26 00:00:00'),
(12,2,'2026-03-28 00:00:00'),
(13,2,'2026-04-20 00:00:00');

-- ============================================================
-- TEAMS (H11-H14)
-- H11(CLOSED): team19(user1+user9), team20(user2+user7), team21(user3+user8)
-- H12(CLOSED): team22(user4+user6), team23(user5+user1), team24(user7+user2)
-- H13(OPEN):   team25(user8), team26(user9+user3)
-- H14(OPEN):   team27(user6), team28(user4+user5)
-- ============================================================
INSERT INTO teams (id, hackathon_id, owner_user_id, name, description, status, is_open, is_public,
    current_member_count, max_member_count, created_at, updated_at) VALUES
(19,11,4, 'ChainX',  'EVM 기반 탈중앙화 P2P 렌탈 마켓플레이스', 'CLOSED',0,1,2,4,'2026-02-25 10:00:00','2026-03-31 09:00:00'),
(20,11,5, 'BlockDev','NFT 기반 디지털 저작권 관리 플랫폼',        'CLOSED',0,1,2,4,'2026-02-26 11:00:00','2026-03-31 09:00:00'),
(21,11,6, 'NFTLab',  'DeFi 기반 소액 투자 DAO 플랫폼',           'CLOSED',0,1,2,4,'2026-02-27 13:00:00','2026-03-31 09:00:00'),
(22,12,7, 'LearnAI', 'AI 기반 개인 맞춤 학습 경로 추천 서비스',   'CLOSED',0,1,2,5,'2026-03-01 10:00:00','2026-04-01 09:00:00'),
(23,12,8, 'EduBot',  '챗봇 기반 실시간 과외 서비스',              'CLOSED',0,1,2,5,'2026-03-02 14:00:00','2026-04-01 09:00:00'),
(24,12,10,'SkillUp', '직장인 마이크로러닝 플랫폼',                'CLOSED',0,1,2,5,'2026-03-03 11:00:00','2026-04-01 09:00:00'),
(25,13,11,'GoodTech','장애인 이동 편의를 위한 실시간 경로 안내',  'OPEN',  1,1,1,4,'2026-03-25 10:00:00','2026-03-25 10:00:00'),
(26,13,12,'Impact+', '독거노인 고독사 예방 IoT 케어 서비스',      'OPEN',  1,1,2,4,'2026-03-26 11:00:00','2026-03-27 10:00:00'),
(27,14,9, 'CloudOps','멀티 클라우드 비용 최적화 자동화 툴',       'OPEN',  1,1,1,3,'2026-03-28 10:00:00','2026-03-28 10:00:00'),
(28,14,7, 'K8sTeam', '쿠버네티스 기반 자동 스케일링 SaaS 플랫폼', 'OPEN',  1,1,2,3,'2026-03-29 14:00:00','2026-03-30 09:00:00');

INSERT INTO team_positions (team_id, position_name, required_count) VALUES
(19,'블록체인',1),(19,'프론트엔드',1),
(20,'Solidity',1),(20,'백엔드',1),
(21,'스마트컨트랙트',1),(21,'풀스택',1),
(22,'AI/ML',1),(22,'백엔드',1),
(23,'백엔드',1),(23,'프론트엔드',1),
(24,'풀스택',1),(24,'UI/UX',1),
(25,'백엔드',1),(25,'AI/ML',1),
(26,'백엔드',1),(26,'IoT',1),
(27,'DevOps',1),(27,'백엔드',1),
(28,'쿠버네티스',1),(28,'백엔드',1);

INSERT INTO team_private_infos (team_id, contact_type, contact_value, internal_memo, edit_token, created_at, updated_at) VALUES
(19,'DISCORD','chainx-dev#1111','P2P 렌탈 dApp','tok_chain_019','2026-02-25 10:00:00','2026-02-25 10:00:00'),
(20,'SLACK','blockdev.slack.com','NFT 저작권 플랫폼','tok_block_020','2026-02-26 11:00:00','2026-02-26 11:00:00'),
(21,'KAKAO','nftlab-group','DeFi DAO 팀','tok_nft_021','2026-02-27 13:00:00','2026-02-27 13:00:00'),
(22,'DISCORD','learnai#2222','AI 학습 추천 팀','tok_learn_022','2026-03-01 10:00:00','2026-03-01 10:00:00'),
(23,'SLACK','edubot.slack.com','챗봇 과외 서비스','tok_edu_023','2026-03-02 14:00:00','2026-03-02 14:00:00'),
(24,'KAKAO','skillup-group','마이크로러닝 팀','tok_skill_024','2026-03-03 11:00:00','2026-03-03 11:00:00'),
(25,'DISCORD','goodtech#3333','장애인 이동 안내','tok_good_025','2026-03-25 10:00:00','2026-03-25 10:00:00'),
(26,'KAKAO','impact-plus-group','독거노인 케어 팀','tok_imp_026','2026-03-26 11:00:00','2026-03-26 11:00:00'),
(27,'DISCORD','cloudops#4444','멀티클라우드 자동화','tok_cloud_027','2026-03-28 10:00:00','2026-03-28 10:00:00'),
(28,'SLACK','k8steam.slack.com','쿠버네티스 SaaS','tok_k8s_028','2026-03-29 14:00:00','2026-03-29 14:00:00');

INSERT INTO team_members (team_id, user_id, role_type, position, joined_at) VALUES
-- H11
(19,4, 'OWNER','블록체인','2026-02-25 10:00:00'),(19,12,'MEMBER','프론트엔드','2026-02-26 09:00:00'),
(20,5, 'OWNER','Solidity','2026-02-26 11:00:00'),(20,10,'MEMBER','백엔드','2026-02-27 10:00:00'),
(21,6, 'OWNER','스마트컨트랙트','2026-02-27 13:00:00'),(21,11,'MEMBER','풀스택','2026-02-28 10:00:00'),
-- H12
(22,7, 'OWNER','AI/ML','2026-03-01 10:00:00'),(22,9, 'MEMBER','백엔드','2026-03-02 09:00:00'),
(23,8, 'OWNER','백엔드','2026-03-02 14:00:00'),(23,4, 'MEMBER','프론트엔드','2026-03-03 10:00:00'),
(24,10,'OWNER','풀스택','2026-03-03 11:00:00'),(24,5, 'MEMBER','UI/UX','2026-03-04 09:00:00'),
-- H13
(25,11,'OWNER','백엔드','2026-03-25 10:00:00'),
(26,12,'OWNER','백엔드','2026-03-26 11:00:00'),(26,6,'MEMBER','IoT','2026-03-27 10:00:00'),
-- H14
(27,9, 'OWNER','DevOps','2026-03-28 10:00:00'),
(28,7, 'OWNER','쿠버네티스','2026-03-29 14:00:00'),(28,8,'MEMBER','백엔드','2026-03-30 09:00:00');

INSERT INTO team_applications (team_id, user_id, message, position, status, processed_by_user_id, created_at, processed_at) VALUES
(19,12,'Web3 프론트엔드 개발 경험 있습니다.','프론트엔드','ACCEPTED',4,'2026-02-26 08:00:00','2026-02-26 09:00:00'),
(20,10,'Solidity 스마트 컨트랙트 개발 가능합니다.','백엔드','ACCEPTED',5,'2026-02-27 09:00:00','2026-02-27 10:00:00'),
(21,11,'DeFi 프로토콜 구현 경험 있습니다.','풀스택','ACCEPTED',6,'2026-02-28 09:00:00','2026-02-28 10:00:00'),
(22,9,'머신러닝 기반 추천 시스템 개발 가능합니다.','백엔드','ACCEPTED',7,'2026-03-02 08:00:00','2026-03-02 09:00:00'),
(23,4,'React 프론트엔드 개발 가능합니다.','프론트엔드','ACCEPTED',8,'2026-03-03 09:00:00','2026-03-03 10:00:00'),
(24,5,'Next.js UI/UX 개발 경험 있습니다.','UI/UX','ACCEPTED',10,'2026-03-04 08:00:00','2026-03-04 09:00:00'),
(26,6,'라즈베리파이 IoT 개발 경험 있습니다.','IoT','ACCEPTED',12,'2026-03-27 09:00:00','2026-03-27 10:00:00'),
(28,8,'Spring Boot + Kubernetes 운영 경험 있습니다.','백엔드','ACCEPTED',7,'2026-03-30 08:00:00','2026-03-30 09:00:00');

INSERT INTO hackathon_registrations (hackathon_id, team_id, registered_at) VALUES
(11,19,'2026-02-25 10:00:00'),(11,20,'2026-02-26 11:00:00'),(11,21,'2026-02-27 13:00:00'),
(12,22,'2026-03-01 10:00:00'),(12,23,'2026-03-02 14:00:00'),(12,24,'2026-03-03 11:00:00'),
(13,25,'2026-03-25 10:00:00'),(13,26,'2026-03-26 11:00:00'),
(14,27,'2026-03-28 10:00:00'),(14,28,'2026-03-29 14:00:00');

-- 진행중 해커톤 제출물 (일부만 제출)
INSERT INTO submissions (id, hackathon_id, team_id, submitter_user_id, status, revision_no, is_latest, submitted_at, created_at, updated_at) VALUES
(19,11,19,4,'SUBMITTED',1,1,'2026-04-03 11:00:00','2026-04-03 11:00:00','2026-04-03 11:00:00'),
(20,11,20,5,'SUBMITTED',1,1,'2026-04-04 09:00:00','2026-04-04 09:00:00','2026-04-04 09:00:00'),
(21,12,22,7,'SUBMITTED',1,1,'2026-04-03 14:00:00','2026-04-03 14:00:00','2026-04-03 14:00:00'),
(22,12,23,8,'SUBMITTED',1,1,'2026-04-04 10:00:00','2026-04-04 10:00:00','2026-04-04 10:00:00');

INSERT INTO submission_items (submission_id, rule_id, value_text, value_url, file_name, original_file_name, file_extension, file_size, is_final) VALUES
-- Submission 19 (H11, ChainX)
(19,NULL,NULL,'https://github.com/chainx-team/p2p-rental',NULL,NULL,NULL,NULL,1),
(19,NULL,NULL,'https://sepolia.etherscan.io/address/0xChainX',NULL,NULL,NULL,NULL,1),
(19,NULL,NULL,'https://youtu.be/chainx-demo',NULL,NULL,NULL,NULL,1),
(19,NULL,'ERC-721 기반 물품 등록, 스마트 컨트랙트 자동 정산, 보증금 에스크로 구현. Hardhat + ethers.js 사용.',NULL,NULL,NULL,NULL,NULL,1),
-- Submission 20 (H11, BlockDev)
(20,NULL,NULL,'https://github.com/blockdev/nft-copyright',NULL,NULL,NULL,NULL,1),
(20,NULL,NULL,'https://mumbai.polygonscan.com/address/0xBlockDev',NULL,NULL,NULL,NULL,1),
(20,NULL,'EIP-2981 로열티 표준 구현. 창작물 최초 등록부터 2차 판매 수익 자동 분배까지 온체인 처리.',NULL,NULL,NULL,NULL,NULL,1),
-- Submission 21 (H12, LearnAI)
(21,NULL,NULL,'https://github.com/learnai/adaptive-path',NULL,NULL,NULL,NULL,1),
(21,NULL,NULL,'https://learnai-demo.vercel.app',NULL,NULL,NULL,NULL,1),
(21,NULL,'학습자 이력 데이터 기반 GPT-4o 개인 맞춤 커리큘럼 생성. 학습 완료율 42% 향상 실험 결과.',NULL,NULL,NULL,NULL,NULL,1),
-- Submission 22 (H12, EduBot)
(22,NULL,NULL,'https://github.com/edubot-team/realtime-tutor',NULL,NULL,NULL,NULL,1),
(22,NULL,NULL,'https://edubot.vercel.app',NULL,NULL,NULL,NULL,1),
(22,NULL,'Claude API 기반 수학/과학 실시간 문제 풀이 챗봇. 오답 패턴 분석으로 약점 집중 학습 유도.',NULL,NULL,NULL,NULL,NULL,1);

-- 진행중 심사 (H11 SCORE, 일부만 완료)
-- H11 기준: [블록체인기술활용도(35), 서비스완성도(30), 탈중앙화철학(25), 발표력(10)]
INSERT INTO judge_evaluations (hackathon_id, team_id, judge_user_id, total_score, scores_json, created_at, updated_at) VALUES
(11,19,2,88.0,'[32.0,27.0,22.0,7.0]','2026-04-04 15:00:00','2026-04-04 15:00:00'),
(11,19,3,86.0,'[30.0,28.0,21.0,7.0]','2026-04-04 16:00:00','2026-04-04 16:00:00');

-- ============================================================
-- 페이지네이션 테스트용 추가 유저
-- ============================================================
INSERT INTO users (id, email, nickname, password, role, account_status, created_at, updated_at) VALUES
(13, 'halmasy1@naver.com','halmasy1',    '$2b$10$jzMveWcSErfoFJvdKEQWQuIljP0Aauj/6HHh7YmR3lp10Bk0lMloy','USER','ACTIVE','2026-03-01 00:00:00','2026-03-01 00:00:00'),
(14, 'dev01@daker.com',   'codeMaster',  '$2b$10$jzMveWcSErfoFJvdKEQWQuIljP0Aauj/6HHh7YmR3lp10Bk0lMloy','USER','ACTIVE','2026-01-05 00:00:00','2026-01-05 00:00:00'),
(15, 'dev02@daker.com',   '박서준',      '$2b$10$jzMveWcSErfoFJvdKEQWQuIljP0Aauj/6HHh7YmR3lp10Bk0lMloy','USER','ACTIVE','2026-01-10 00:00:00','2026-01-10 00:00:00'),
(16, 'dev03@daker.com',   'lambda_dev',  '$2b$10$jzMveWcSErfoFJvdKEQWQuIljP0Aauj/6HHh7YmR3lp10Bk0lMloy','USER','ACTIVE','2026-01-15 00:00:00','2026-01-15 00:00:00'),
(17, 'dev04@daker.com',   '한지윤',      '$2b$10$jzMveWcSErfoFJvdKEQWQuIljP0Aauj/6HHh7YmR3lp10Bk0lMloy','USER','ACTIVE','2026-01-20 00:00:00','2026-01-20 00:00:00'),
(18, 'dev05@daker.com',   'rust_fan',    '$2b$10$jzMveWcSErfoFJvdKEQWQuIljP0Aauj/6HHh7YmR3lp10Bk0lMloy','USER','ACTIVE','2026-01-25 00:00:00','2026-01-25 00:00:00'),
(19, 'dev06@daker.com',   '송예린',      '$2b$10$jzMveWcSErfoFJvdKEQWQuIljP0Aauj/6HHh7YmR3lp10Bk0lMloy','USER','ACTIVE','2026-02-01 00:00:00','2026-02-01 00:00:00'),
(20, 'dev07@daker.com',   'docker_pro',  '$2b$10$jzMveWcSErfoFJvdKEQWQuIljP0Aauj/6HHh7YmR3lp10Bk0lMloy','USER','ACTIVE','2026-02-05 00:00:00','2026-02-05 00:00:00');

-- ============================================================
-- 페이지네이션 테스트용 추가 해커톤 16개 (ID 15~30)
-- ============================================================
INSERT INTO hackathons (id, title, summary, description, thumbnail_url, organizer, status, score_type,
    start_date, end_date, registration_start_date, registration_end_date, submission_deadline_at,
    closed_at, max_team_size, max_participants, camp_enabled, allow_solo, deleted, created_at, updated_at) VALUES

-- ENDED (3개)
(15, 'NLP 텍스트 마이닝 대회',
 '자연어 처리로 텍스트 데이터에서 인사이트를 추출하세요',
 'HuggingFace 모델 허브를 활용한 감성 분석, 개체명 인식, 문서 분류 등 NLP 핵심 과제를 해결합니다.',
 'https://picsum.photos/seed/hack15/800/400', 'Daker', 'ENDED', 'SCORE',
 '2025-10-15 09:00:00', '2025-10-17 18:00:00',
 '2025-09-01 00:00:00', '2025-10-10 23:59:59', '2025-10-17 15:00:00',
 NULL, 3, 90, 0, 1, 0, '2025-08-20 00:00:00', '2025-10-18 00:00:00'),
(16, 'DevOps 자동화 챌린지',
 'CI/CD 파이프라인 최적화와 인프라 자동화 대회',
 'GitHub Actions, Jenkins, Terraform 등을 활용해 배포 자동화와 모니터링 시스템을 구축하세요.',
 'https://picsum.photos/seed/hack16/800/400', 'Daker', 'ENDED', 'VOTE',
 '2025-11-10 09:00:00', '2025-11-12 18:00:00',
 '2025-10-01 00:00:00', '2025-11-05 23:59:59', '2025-11-12 15:00:00',
 NULL, 3, 80, 0, 1, 0, '2025-09-15 00:00:00', '2025-11-13 00:00:00'),
(17, '디자인 시스템 해커톤',
 'Figma + React로 재사용 가능한 디자인 시스템을 구축하세요',
 '컴포넌트 라이브러리, 토큰 시스템, 접근성 가이드라인을 포함한 완성도 높은 디자인 시스템을 만드세요.',
 'https://picsum.photos/seed/hack17/800/400', 'Daker', 'ENDED', 'SCORE',
 '2025-12-05 09:00:00', '2025-12-07 18:00:00',
 '2025-11-01 00:00:00', '2025-12-01 23:59:59', '2025-12-07 15:00:00',
 NULL, 4, 60, 0, 0, 0, '2025-10-10 00:00:00', '2025-12-08 00:00:00'),

-- CLOSED (진행중, 등록마감, 2개)
(18, '추천 시스템 경진대회',
 '협업 필터링과 딥러닝으로 개인화 추천 엔진을 만드세요',
 'MovieLens, Amazon 리뷰 데이터를 활용한 추천 모델을 개발합니다. NDCG, Hit Rate 기준 평가.',
 'https://picsum.photos/seed/hack18/800/400', 'Daker', 'CLOSED', 'SCORE',
 '2026-04-01 09:00:00', '2026-04-12 18:00:00',
 '2026-02-20 00:00:00', '2026-03-25 23:59:59', '2026-04-12 15:00:00',
 NULL, 3, 100, 0, 1, 0, '2026-02-10 00:00:00', '2026-04-01 09:00:00'),
(19, 'MLOps 파이프라인 챌린지',
 'ML 모델 학습부터 배포까지 End-to-End 파이프라인을 구축하세요',
 'MLflow, Kubeflow, Airflow를 활용해 재현 가능한 ML 파이프라인을 만듭니다.',
 'https://picsum.photos/seed/hack19/800/400', 'Daker', 'CLOSED', 'SCORE',
 '2026-04-02 09:00:00', '2026-04-11 18:00:00',
 '2026-03-01 00:00:00', '2026-03-28 23:59:59', '2026-04-11 15:00:00',
 NULL, 4, 80, 0, 1, 0, '2026-02-15 00:00:00', '2026-04-02 09:00:00'),

-- OPEN (모집중, 4개)
(20, '음성 AI 해커톤',
 'STT, TTS, 음성 합성 기술로 음성 기반 서비스를 개발하세요',
 'Whisper, ElevenLabs, Coqui TTS 등을 활용한 음성 인터페이스 서비스를 만드세요.',
 'https://picsum.photos/seed/hack20/800/400', 'Daker', 'OPEN', 'VOTE',
 '2026-04-20 09:00:00', '2026-04-25 18:00:00',
 '2026-03-20 00:00:00', '2026-04-15 23:59:59', '2026-04-25 15:00:00',
 NULL, 4, 100, 0, 0, 0, '2026-03-10 00:00:00', '2026-03-20 00:00:00'),
(21, '컴퓨터 비전 챌린지',
 '이미지 인식, 객체 탐지, 세그멘테이션 등 비전 AI 경진대회',
 'YOLO, SAM, CLIP 등 최신 비전 모델을 활용해 실제 산업 문제를 해결하세요.',
 'https://picsum.photos/seed/hack21/800/400', 'Daker', 'OPEN', 'SCORE',
 '2026-04-22 09:00:00', '2026-04-27 18:00:00',
 '2026-03-25 00:00:00', '2026-04-18 23:59:59', '2026-04-27 15:00:00',
 NULL, 3, 120, 0, 1, 0, '2026-03-15 00:00:00', '2026-03-25 00:00:00'),
(22, '웹 접근성 해커톤',
 'WCAG 기준을 충족하는 접근성 높은 웹 서비스를 만드세요',
 '시각·청각·운동 장애인도 불편 없이 사용할 수 있는 웹 서비스를 개발합니다.',
 'https://picsum.photos/seed/hack22/800/400', 'Daker', 'OPEN', 'VOTE',
 '2026-04-25 09:00:00', '2026-04-28 18:00:00',
 '2026-03-28 00:00:00', '2026-04-20 23:59:59', '2026-04-28 15:00:00',
 NULL, 4, 80, 0, 1, 0, '2026-03-18 00:00:00', '2026-03-28 00:00:00'),
(23, '크리에이티브 코딩 해커톤',
 'p5.js, Processing으로 예술과 코딩의 경계를 넘는 작품을 만드세요',
 '제너러티브 아트, 인터랙티브 설치 미술, 데이터 시각화 아트워크 등 코드로 표현하는 창작 대회.',
 'https://picsum.photos/seed/hack23/800/400', 'Daker', 'OPEN', 'SCORE',
 '2026-04-28 09:00:00', '2026-04-30 18:00:00',
 '2026-04-01 00:00:00', '2026-04-25 23:59:59', '2026-04-30 15:00:00',
 NULL, 3, 60, 0, 1, 0, '2026-03-25 00:00:00', '2026-04-01 00:00:00'),

-- UPCOMING (7개)
(24, '자율주행 시뮬레이션 대회',
 'CARLA 시뮬레이터에서 자율주행 알고리즘을 구현하세요',
 '경로 계획, 장애물 회피, 신호 인식 등 자율주행 핵심 모듈을 개발합니다.',
 'https://picsum.photos/seed/hack24/800/400', 'Daker', 'UPCOMING', 'SCORE',
 '2026-05-15 09:00:00', '2026-05-18 18:00:00',
 '2026-04-15 00:00:00', '2026-05-10 23:59:59', '2026-05-18 15:00:00',
 NULL, 3, 60, 1, 0, 0, '2026-04-01 00:00:00', '2026-04-01 00:00:00'),
(25, '오픈소스 컨트리뷰톤',
 '오픈소스 프로젝트에 기여하며 기술력을 증명하세요',
 '인기 오픈소스 프로젝트의 이슈를 해결하고 PR을 제출합니다. 멘토가 코드 리뷰를 제공합니다.',
 'https://picsum.photos/seed/hack25/800/400', 'Daker', 'UPCOMING', 'VOTE',
 '2026-06-01 09:00:00', '2026-06-03 18:00:00',
 '2026-05-01 00:00:00', '2026-05-25 23:59:59', '2026-06-03 15:00:00',
 NULL, 5, 150, 0, 1, 0, '2026-04-05 00:00:00', '2026-04-05 00:00:00'),
(26, 'Edge AI 해커톤',
 'Raspberry Pi, Jetson 등 엣지 디바이스에서 AI 추론을 최적화하세요',
 '모델 경량화, 양자화, 프루닝 기법을 활용해 엣지 디바이스에서 실시간 추론이 가능한 시스템을 구축합니다.',
 'https://picsum.photos/seed/hack26/800/400', 'Daker', 'UPCOMING', 'SCORE',
 '2026-06-20 09:00:00', '2026-06-22 18:00:00',
 '2026-05-20 00:00:00', '2026-06-15 23:59:59', '2026-06-22 15:00:00',
 NULL, 3, 50, 0, 0, 0, '2026-04-05 00:00:00', '2026-04-05 00:00:00'),
(27, '데이터 엔지니어링 챌린지',
 'Spark, Kafka, Flink로 대규모 데이터 파이프라인을 구축하세요',
 '실시간 스트리밍, 배치 처리, 데이터 레이크 구축 등 데이터 엔지니어링 역량을 겨루는 대회입니다.',
 'https://picsum.photos/seed/hack27/800/400', 'Daker', 'UPCOMING', 'SCORE',
 '2026-07-10 09:00:00', '2026-07-12 18:00:00',
 '2026-06-10 00:00:00', '2026-07-05 23:59:59', '2026-07-12 15:00:00',
 NULL, 4, 100, 1, 0, 0, '2026-04-05 00:00:00', '2026-04-05 00:00:00'),
(28, 'API 이코노미 해커톤',
 '공공·민간 API를 매시업해 새로운 가치를 창출하세요',
 '카카오맵, 공공데이터, OpenWeather 등 다양한 API를 조합하여 혁신 서비스를 만드세요.',
 'https://picsum.photos/seed/hack28/800/400', 'Daker', 'UPCOMING', 'VOTE',
 '2026-07-25 09:00:00', '2026-07-27 18:00:00',
 '2026-06-25 00:00:00', '2026-07-20 23:59:59', '2026-07-27 15:00:00',
 NULL, 4, 120, 0, 0, 0, '2026-04-05 00:00:00', '2026-04-05 00:00:00'),
(29, '게임 개발 잼 2026',
 'Unity/Unreal로 48시간 안에 게임 하나를 완성하는 게임잼',
 '테마 공개 후 48시간 안에 플레이 가능한 게임을 완성하세요. 아트 에셋 라이브러리가 제공됩니다.',
 'https://picsum.photos/seed/hack29/800/400', 'Daker', 'UPCOMING', 'SCORE',
 '2026-08-15 09:00:00', '2026-08-17 18:00:00',
 '2026-07-15 00:00:00', '2026-08-10 23:59:59', '2026-08-17 15:00:00',
 NULL, 4, 80, 0, 1, 0, '2026-04-05 00:00:00', '2026-04-05 00:00:00'),
(30, '로보틱스 챌린지 2026',
 'ROS2와 하드웨어를 활용한 로봇 제어 및 자율주행 대회',
 '시뮬레이션 환경에서 로봇을 프로그래밍하고, 최종일에는 실제 로봇으로 미션을 수행합니다.',
 'https://picsum.photos/seed/hack30/800/400', 'Daker', 'UPCOMING', 'SCORE',
 '2026-09-01 09:00:00', '2026-09-03 18:00:00',
 '2026-08-01 00:00:00', '2026-08-25 23:59:59', '2026-09-03 15:00:00',
 NULL, 3, 60, 1, 0, 0, '2026-04-05 00:00:00', '2026-04-05 00:00:00');

-- 추가 태그 (ID 49~68)
INSERT INTO tags (id, name) VALUES
(49,'NLP'),(50,'감성분석'),(51,'HuggingFace'),
(52,'CI/CD'),(53,'Terraform'),(54,'Jenkins'),
(55,'디자인시스템'),(56,'Figma'),(57,'React'),
(58,'추천시스템'),(59,'협업필터링'),
(60,'MLOps'),(61,'MLflow'),(62,'Kubeflow'),
(63,'음성AI'),(64,'STT'),(65,'TTS'),
(66,'컴퓨터비전'),(67,'YOLO'),(68,'접근성');

-- 추가 해커톤 태그
INSERT INTO hackathon_tags (hackathon_id, tag_id) VALUES
(15,49),(15,50),(15,51),(15,10),
(16,52),(16,53),(16,54),(16,46),
(17,55),(17,56),(17,57),
(18,58),(18,59),(18,1),(18,11),
(19,60),(19,61),(19,62),(19,1),
(20,63),(20,64),(20,65),
(21,66),(21,67),(21,1),(21,11),
(22,68),(22,57),(22,42),
(23,57),(23,1),(23,10),
(24,40),(24,38),(24,10),
(25,46),(25,52),(25,10),
(26,1),(26,31),(26,11),
(27,9),(27,52),(27,10),
(28,31),(28,9),(28,1),
(29,44),(29,57),(29,1),
(30,38),(30,39),(30,40);

-- 추가 마일스톤
INSERT INTO milestones (hackathon_id, title, description, date) VALUES
(15,'참가 접수 시작',NULL,'2025-09-01 00:00:00'),
(15,'참가 접수 마감',NULL,'2025-10-10 23:59:59'),
(15,'대회 시작',NULL,'2025-10-15 09:00:00'),
(15,'시상식',NULL,'2025-10-17 18:00:00'),
(16,'참가 접수 시작',NULL,'2025-10-01 00:00:00'),
(16,'참가 접수 마감',NULL,'2025-11-05 23:59:59'),
(16,'대회 시작',NULL,'2025-11-10 09:00:00'),
(16,'시상식',NULL,'2025-11-12 18:00:00'),
(17,'참가 접수 시작',NULL,'2025-11-01 00:00:00'),
(17,'참가 접수 마감',NULL,'2025-12-01 23:59:59'),
(17,'대회 시작',NULL,'2025-12-05 09:00:00'),
(17,'시상식',NULL,'2025-12-07 18:00:00'),
(18,'참가 접수 시작',NULL,'2026-02-20 00:00:00'),
(18,'참가 접수 마감',NULL,'2026-03-25 23:59:59'),
(18,'대회 시작',NULL,'2026-04-01 09:00:00'),
(18,'제출 마감',NULL,'2026-04-12 15:00:00'),
(19,'참가 접수 시작',NULL,'2026-03-01 00:00:00'),
(19,'참가 접수 마감',NULL,'2026-03-28 23:59:59'),
(19,'대회 시작',NULL,'2026-04-02 09:00:00'),
(19,'제출 마감',NULL,'2026-04-11 15:00:00'),
(20,'참가 접수 시작',NULL,'2026-03-20 00:00:00'),
(20,'참가 접수 마감',NULL,'2026-04-15 23:59:59'),
(20,'대회 시작',NULL,'2026-04-20 09:00:00'),
(20,'시상식',NULL,'2026-04-25 18:00:00'),
(21,'참가 접수 시작',NULL,'2026-03-25 00:00:00'),
(21,'참가 접수 마감',NULL,'2026-04-18 23:59:59'),
(21,'대회 시작',NULL,'2026-04-22 09:00:00'),
(21,'시상식',NULL,'2026-04-27 18:00:00'),
(22,'참가 접수 시작',NULL,'2026-03-28 00:00:00'),
(22,'참가 접수 마감',NULL,'2026-04-20 23:59:59'),
(22,'대회 시작',NULL,'2026-04-25 09:00:00'),
(22,'시상식',NULL,'2026-04-28 18:00:00'),
(23,'참가 접수 시작',NULL,'2026-04-01 00:00:00'),
(23,'참가 접수 마감',NULL,'2026-04-25 23:59:59'),
(23,'대회 시작',NULL,'2026-04-28 09:00:00'),
(23,'시상식',NULL,'2026-04-30 18:00:00'),
(24,'참가 접수 시작',NULL,'2026-04-15 00:00:00'),
(24,'대회 시작',NULL,'2026-05-15 09:00:00'),
(25,'참가 접수 시작',NULL,'2026-05-01 00:00:00'),
(25,'대회 시작',NULL,'2026-06-01 09:00:00'),
(26,'참가 접수 시작',NULL,'2026-05-20 00:00:00'),
(26,'대회 시작',NULL,'2026-06-20 09:00:00'),
(27,'참가 접수 시작',NULL,'2026-06-10 00:00:00'),
(27,'대회 시작',NULL,'2026-07-10 09:00:00'),
(28,'참가 접수 시작',NULL,'2026-06-25 00:00:00'),
(28,'대회 시작',NULL,'2026-07-25 09:00:00'),
(29,'참가 접수 시작',NULL,'2026-07-15 00:00:00'),
(29,'대회 시작',NULL,'2026-08-15 09:00:00'),
(30,'참가 접수 시작',NULL,'2026-08-01 00:00:00'),
(30,'대회 시작',NULL,'2026-09-01 09:00:00');

-- 추가 상금
INSERT INTO prizes (hackathon_id, ranking, amount, description) VALUES
(15,1,5000000,'대상'),(15,2,2500000,'최우수상'),(15,3,1000000,'우수상'),
(16,1,6000000,'대상'),(16,2,3000000,'최우수상'),(16,3,1500000,'우수상'),
(17,1,4000000,'대상'),(17,2,2000000,'최우수상'),(17,3,800000,'우수상'),
(18,1,8000000,'대상'),(18,2,4000000,'최우수상'),(18,3,2000000,'우수상'),
(19,1,10000000,'대상'),(19,2,5000000,'최우수상'),(19,3,2500000,'우수상'),
(20,1,7000000,'대상'),(20,2,3500000,'최우수상'),(20,3,1500000,'우수상'),
(21,1,12000000,'대상'),(21,2,6000000,'최우수상'),(21,3,3000000,'우수상'),
(22,1,6000000,'대상'),(22,2,3000000,'최우수상'),(22,3,1000000,'우수상'),
(23,1,5000000,'대상'),(23,2,2500000,'최우수상'),(23,3,1000000,'우수상'),
(24,1,15000000,'대상'),(24,2,7000000,'최우수상'),(24,3,3000000,'우수상'),
(25,1,8000000,'대상'),(25,2,4000000,'최우수상'),(25,3,2000000,'우수상'),
(26,1,10000000,'대상'),(26,2,5000000,'최우수상'),(26,3,2000000,'우수상'),
(27,1,12000000,'대상'),(27,2,6000000,'최우수상'),(27,3,3000000,'우수상'),
(28,1,8000000,'대상'),(28,2,4000000,'최우수상'),(28,3,1500000,'우수상'),
(29,1,10000000,'대상'),(29,2,5000000,'최우수상'),(29,3,2000000,'우수상'),
(30,1,15000000,'대상'),(30,2,7000000,'최우수상'),(30,3,3000000,'우수상');

-- ============================================================
-- 대량 팀 추가 (ID 29~58)
-- 기존 해커톤(H1~H14)에 추가 팀 + 신규 해커톤(H15~H23)에 팀 배치
-- ============================================================
INSERT INTO teams (id, hackathon_id, owner_user_id, name, description, status, is_open, is_public,
    current_member_count, max_member_count, created_at, updated_at) VALUES
-- H5 (CLOSED/진행중): halmasy1 팀 + 추가 2팀 (기존 13,14,15 + 29,30,31)
(29, 5, 13,'Halmasy AI','LLM 기반 자동 문서 생성 및 요약 서비스',           'OPEN',1,1,1,4,'2026-03-10 10:00:00','2026-03-30 09:00:00'),
(30, 5, 14,'CodePilot', 'AI 코드 자동 완성 및 최적화 어시스턴트',           'OPEN',1,1,2,4,'2026-03-08 09:00:00','2026-03-30 09:00:00'),
(31, 5, 16,'TextMind',  'LLM 기반 비정형 텍스트 데이터 구조화 서비스',      'OPEN',1,1,2,4,'2026-03-09 10:00:00','2026-03-30 09:00:00'),
-- H6 (CLOSED/진행중): 추가 2팀 (기존 16,17,18 + 32,33)
(32, 6, 15,'WealthBot', 'AI 기반 개인 자산 관리 로보어드바이저',            'OPEN',1,1,2,5,'2026-03-04 10:00:00','2026-03-28 09:00:00'),
(33, 6, 17,'InsurTech', '보험 비교 분석 및 맞춤 추천 서비스',              'OPEN',1,1,1,5,'2026-03-05 11:00:00','2026-03-28 09:00:00'),
-- H15 (ENDED): 3팀
(34,15,14,'SentiView',  '뉴스 기사 실시간 감성 분석 대시보드',             'CLOSED',0,1,2,3,'2025-09-05 10:00:00','2025-10-15 09:00:00'),
(35,15,16,'DocParser',  '계약서 자동 분석 및 리스크 탐지 시스템',           'CLOSED',0,1,2,3,'2025-09-07 14:00:00','2025-10-15 09:00:00'),
(36,15,18,'NERBot',     '커스텀 개체명 인식 모델 학습 플랫폼',             'CLOSED',0,1,1,3,'2025-09-10 11:00:00','2025-10-15 09:00:00'),
-- H16 (ENDED): 3팀
(37,16,15,'DeployBot',  'Slack 연동 원클릭 배포 자동화 봇',                'CLOSED',0,1,2,3,'2025-10-05 09:00:00','2025-11-10 09:00:00'),
(38,16,17,'InfraEye',   '클라우드 인프라 비용 모니터링 대시보드',           'CLOSED',0,1,2,3,'2025-10-07 11:00:00','2025-11-10 09:00:00'),
(39,16,19,'PipelinePro','GitHub Actions 기반 멀티 환경 배포 파이프라인',    'CLOSED',0,1,1,3,'2025-10-08 13:00:00','2025-11-10 09:00:00'),
-- H17 (ENDED): 2팀
(40,17,20,'PixelKit',   'React 기반 오픈소스 디자인 시스템 라이브러리',     'CLOSED',0,1,2,4,'2025-11-05 09:00:00','2025-12-05 09:00:00'),
(41,17,14,'ThemeForge', '다크/라이트 모드 자동 전환 토큰 시스템',           'CLOSED',0,1,2,4,'2025-11-07 10:00:00','2025-12-05 09:00:00'),
-- H18 (CLOSED/진행중): 3팀
(42,18,18,'RecoEngine', '하이브리드 추천 알고리즘 기반 영화 추천 서비스',   'OPEN',1,1,2,3,'2026-02-25 09:00:00','2026-04-01 09:00:00'),
(43,18,19,'ShopReco',   'e커머스 실시간 상품 추천 엔진',                   'OPEN',1,1,2,3,'2026-02-26 11:00:00','2026-04-01 09:00:00'),
(44,18,20,'NewsForU',   'AI 뉴스 큐레이션 및 개인화 피드 서비스',          'OPEN',1,1,1,3,'2026-02-28 10:00:00','2026-04-01 09:00:00'),
-- H19 (CLOSED/진행중): 3팀
(45,19,16,'PipeML',     'ML 파이프라인 시각화 및 모니터링 대시보드',        'OPEN',1,1,2,4,'2026-03-05 09:00:00','2026-04-02 09:00:00'),
(46,19,15,'AutoTrain',  'AutoML 기반 원클릭 모델 학습 서비스',             'OPEN',1,1,1,4,'2026-03-06 10:00:00','2026-04-02 09:00:00'),
(47,19,17,'ModelHub',   'ML 모델 버전 관리 및 A/B 테스트 플랫폼',          'OPEN',1,1,2,4,'2026-03-07 11:00:00','2026-04-02 09:00:00'),
-- H20 (OPEN/모집중): 3팀
(48,20,13,'VoiceNote',  '음성 메모 자동 정리 및 검색 서비스',              'OPEN',1,1,1,4,'2026-03-25 09:00:00','2026-03-25 09:00:00'),
(49,20,18,'TalkBot',    '실시간 다국어 음성 통역 서비스',                  'OPEN',1,1,2,4,'2026-03-26 14:00:00','2026-03-27 10:00:00'),
(50,20,20,'PodCastAI',  'AI 팟캐스트 자동 편집 및 하이라이트 추출',        'OPEN',1,1,1,4,'2026-03-28 10:00:00','2026-03-28 10:00:00'),
-- H21 (OPEN/모집중): 3팀
(51,21,14,'EyeScan',    '제조 라인 불량품 자동 탐지 시스템',               'OPEN',1,1,2,3,'2026-03-28 09:00:00','2026-03-29 10:00:00'),
(52,21,19,'FaceGuard',  '얼굴 인식 기반 출입 관리 시스템',                 'OPEN',1,1,1,3,'2026-03-29 10:00:00','2026-03-29 10:00:00'),
(53,21,16,'PlantDoc',   'AI 식물 질병 진단 및 처방 서비스',                'OPEN',1,1,2,3,'2026-03-30 11:00:00','2026-03-31 09:00:00'),
-- H22 (OPEN/모집중): 2팀
(54,22,17,'A11yKit',    '웹 접근성 자동 검사 및 수정 제안 도구',           'OPEN',1,1,2,4,'2026-04-01 09:00:00','2026-04-02 10:00:00'),
(55,22,15,'ScreenRead', '시각 장애인용 웹 콘텐츠 음성 변환 서비스',        'OPEN',1,1,1,4,'2026-04-02 10:00:00','2026-04-02 10:00:00'),
-- H23 (OPEN/모집중): 2팀
(56,23,20,'GenArt',     '제너러티브 아트 실시간 인터랙티브 갤러리',        'OPEN',1,1,2,3,'2026-04-03 09:00:00','2026-04-04 10:00:00'),
(57,23,13,'DataViz',    '데이터 시각화 아트워크 자동 생성 도구',           'OPEN',1,1,1,3,'2026-04-04 10:00:00','2026-04-04 10:00:00'),
-- H13 (OPEN/모집중): 추가 2팀 (기존 25,26 + 58,59)
(58,13,14,'SafeWalk',   '여성 안심 귀갓길 AI 경로 추천 서비스',            'OPEN',1,1,2,4,'2026-03-28 10:00:00','2026-03-29 09:00:00'),
(59,13,17,'MealShare',  '잉여 식자재 매칭으로 음식 낭비를 줄이는 서비스',   'OPEN',1,1,1,4,'2026-03-30 11:00:00','2026-03-30 11:00:00');

-- 대량 팀 포지션
INSERT INTO team_positions (team_id, position_name, required_count) VALUES
(29,'AI/ML',1),(29,'백엔드',1),
(30,'AI/ML',1),(30,'프론트엔드',1),
(31,'NLP',1),(31,'백엔드',1),
(32,'AI/ML',1),(32,'프론트엔드',1),
(33,'백엔드',1),(33,'프론트엔드',1),
(34,'NLP',1),(34,'프론트엔드',1),
(35,'NLP',1),(35,'백엔드',1),
(36,'AI/ML',1),
(37,'DevOps',1),(37,'백엔드',1),
(38,'DevOps',1),(38,'프론트엔드',1),
(39,'DevOps',1),
(40,'프론트엔드',1),(40,'UI/UX',1),
(41,'프론트엔드',1),(41,'디자인',1),
(42,'AI/ML',1),(42,'백엔드',1),
(43,'AI/ML',1),(43,'백엔드',1),
(44,'AI/ML',1),
(45,'MLOps',1),(45,'백엔드',1),
(46,'AI/ML',1),
(47,'MLOps',1),(47,'프론트엔드',1),
(48,'음성AI',1),(48,'백엔드',1),
(49,'AI/ML',1),(49,'프론트엔드',1),
(50,'음성AI',1),
(51,'비전AI',1),(51,'백엔드',1),
(52,'비전AI',1),
(53,'비전AI',1),(53,'백엔드',1),
(54,'프론트엔드',1),(54,'백엔드',1),
(55,'프론트엔드',1),
(56,'크리에이티브',1),(56,'프론트엔드',1),
(57,'데이터시각화',1),
(58,'AI/ML',1),(58,'백엔드',1),
(59,'백엔드',1),(59,'프론트엔드',1);

-- 대량 팀 내부 연락처
INSERT INTO team_private_infos (team_id, contact_type, contact_value, internal_memo, edit_token, created_at, updated_at) VALUES
(29,'KAKAO','halmasy-ai-group','AI 문서 생성 프로젝트','tok_029','2026-03-10 10:00:00','2026-03-10 10:00:00'),
(30,'DISCORD','codepilot#1001','AI 코드 완성 프로젝트','tok_030','2026-03-08 09:00:00','2026-03-08 09:00:00'),
(31,'SLACK','textmind.slack.com','텍스트 구조화 프로젝트','tok_031','2026-03-09 10:00:00','2026-03-09 10:00:00'),
(32,'DISCORD','wealthbot#2001','로보어드바이저 팀','tok_032','2026-03-04 10:00:00','2026-03-04 10:00:00'),
(33,'KAKAO','insurtech-group','보험 추천 서비스 팀','tok_033','2026-03-05 11:00:00','2026-03-05 11:00:00'),
(34,'DISCORD','sentiview#3001','감성 분석 프로젝트','tok_034','2025-09-05 10:00:00','2025-09-05 10:00:00'),
(35,'SLACK','docparser.slack.com','계약서 분석 팀','tok_035','2025-09-07 14:00:00','2025-09-07 14:00:00'),
(36,'KAKAO','nerbot-group','개체명 인식 팀','tok_036','2025-09-10 11:00:00','2025-09-10 11:00:00'),
(37,'DISCORD','deploybot#4001','배포 자동화 팀','tok_037','2025-10-05 09:00:00','2025-10-05 09:00:00'),
(38,'SLACK','infraeye.slack.com','인프라 모니터링 팀','tok_038','2025-10-07 11:00:00','2025-10-07 11:00:00'),
(39,'KAKAO','pipelinepro-group','배포 파이프라인 팀','tok_039','2025-10-08 13:00:00','2025-10-08 13:00:00'),
(40,'DISCORD','pixelkit#5001','디자인 시스템 프로젝트','tok_040','2025-11-05 09:00:00','2025-11-05 09:00:00'),
(41,'SLACK','themeforge.slack.com','토큰 시스템 팀','tok_041','2025-11-07 10:00:00','2025-11-07 10:00:00'),
(42,'DISCORD','recoengine#6001','추천 엔진 프로젝트','tok_042','2026-02-25 09:00:00','2026-02-25 09:00:00'),
(43,'KAKAO','shopreco-group','상품 추천 팀','tok_043','2026-02-26 11:00:00','2026-02-26 11:00:00'),
(44,'SLACK','newsforu.slack.com','뉴스 큐레이션 팀','tok_044','2026-02-28 10:00:00','2026-02-28 10:00:00'),
(45,'DISCORD','pipeml#7001','ML 파이프라인 프로젝트','tok_045','2026-03-05 09:00:00','2026-03-05 09:00:00'),
(46,'SLACK','autotrain.slack.com','AutoML 서비스 팀','tok_046','2026-03-06 10:00:00','2026-03-06 10:00:00'),
(47,'KAKAO','modelhub-group','모델 허브 팀','tok_047','2026-03-07 11:00:00','2026-03-07 11:00:00'),
(48,'DISCORD','voicenote#8001','음성 메모 프로젝트','tok_048','2026-03-25 09:00:00','2026-03-25 09:00:00'),
(49,'SLACK','talkbot.slack.com','다국어 통역 팀','tok_049','2026-03-26 14:00:00','2026-03-26 14:00:00'),
(50,'KAKAO','podcastai-group','팟캐스트 AI 팀','tok_050','2026-03-28 10:00:00','2026-03-28 10:00:00'),
(51,'DISCORD','eyescan#9001','불량품 탐지 프로젝트','tok_051','2026-03-28 09:00:00','2026-03-28 09:00:00'),
(52,'KAKAO','faceguard-group','출입 관리 팀','tok_052','2026-03-29 10:00:00','2026-03-29 10:00:00'),
(53,'SLACK','plantdoc.slack.com','식물 질병 진단 팀','tok_053','2026-03-30 11:00:00','2026-03-30 11:00:00'),
(54,'DISCORD','a11ykit#1010','접근성 검사 프로젝트','tok_054','2026-04-01 09:00:00','2026-04-01 09:00:00'),
(55,'KAKAO','screenread-group','음성 변환 서비스 팀','tok_055','2026-04-02 10:00:00','2026-04-02 10:00:00'),
(56,'DISCORD','genart#1020','제너러티브 아트 팀','tok_056','2026-04-03 09:00:00','2026-04-03 09:00:00'),
(57,'SLACK','dataviz.slack.com','데이터 시각화 팀','tok_057','2026-04-04 10:00:00','2026-04-04 10:00:00'),
(58,'DISCORD','safewalk#1030','안심 귀갓길 프로젝트','tok_058','2026-03-28 10:00:00','2026-03-28 10:00:00'),
(59,'KAKAO','mealshare-group','식자재 매칭 팀','tok_059','2026-03-30 11:00:00','2026-03-30 11:00:00');

-- 대량 팀 멤버
INSERT INTO team_members (team_id, user_id, role_type, position, joined_at) VALUES
-- H5 추가
(29,13,'OWNER','AI/ML','2026-03-10 10:00:00'),
(30,14,'OWNER','AI/ML','2026-03-08 09:00:00'),(30,15,'MEMBER','프론트엔드','2026-03-09 10:00:00'),
(31,16,'OWNER','NLP','2026-03-09 10:00:00'),(31,17,'MEMBER','백엔드','2026-03-10 09:00:00'),
-- H6 추가
(32,15,'OWNER','AI/ML','2026-03-04 10:00:00'),(32,19,'MEMBER','프론트엔드','2026-03-05 09:00:00'),
(33,17,'OWNER','백엔드','2026-03-05 11:00:00'),
-- H15
(34,14,'OWNER','NLP','2025-09-05 10:00:00'),(34,16,'MEMBER','프론트엔드','2025-09-06 10:00:00'),
(35,16,'OWNER','NLP','2025-09-07 14:00:00'),(35,18,'MEMBER','백엔드','2025-09-08 10:00:00'),
(36,18,'OWNER','AI/ML','2025-09-10 11:00:00'),
-- H16
(37,15,'OWNER','DevOps','2025-10-05 09:00:00'),(37,17,'MEMBER','백엔드','2025-10-06 10:00:00'),
(38,17,'OWNER','DevOps','2025-10-07 11:00:00'),(38,19,'MEMBER','프론트엔드','2025-10-08 09:00:00'),
(39,19,'OWNER','DevOps','2025-10-08 13:00:00'),
-- H17
(40,20,'OWNER','프론트엔드','2025-11-05 09:00:00'),(40,18,'MEMBER','UI/UX','2025-11-06 10:00:00'),
(41,14,'OWNER','프론트엔드','2025-11-07 10:00:00'),(41,20,'MEMBER','디자인','2025-11-08 09:00:00'),
-- H18
(42,18,'OWNER','AI/ML','2026-02-25 09:00:00'),(42,14,'MEMBER','백엔드','2026-02-26 10:00:00'),
(43,19,'OWNER','AI/ML','2026-02-26 11:00:00'),(43,16,'MEMBER','백엔드','2026-02-27 10:00:00'),
(44,20,'OWNER','AI/ML','2026-02-28 10:00:00'),
-- H19
(45,16,'OWNER','MLOps','2026-03-05 09:00:00'),(45,20,'MEMBER','백엔드','2026-03-06 09:00:00'),
(46,15,'OWNER','AI/ML','2026-03-06 10:00:00'),
(47,17,'OWNER','MLOps','2026-03-07 11:00:00'),(47,18,'MEMBER','프론트엔드','2026-03-08 10:00:00'),
-- H20
(48,13,'OWNER','음성AI','2026-03-25 09:00:00'),
(49,18,'OWNER','AI/ML','2026-03-26 14:00:00'),(49,15,'MEMBER','프론트엔드','2026-03-27 10:00:00'),
(50,20,'OWNER','음성AI','2026-03-28 10:00:00'),
-- H21
(51,14,'OWNER','비전AI','2026-03-28 09:00:00'),(51,17,'MEMBER','백엔드','2026-03-29 10:00:00'),
(52,19,'OWNER','비전AI','2026-03-29 10:00:00'),
(53,16,'OWNER','비전AI','2026-03-30 11:00:00'),(53,13,'MEMBER','백엔드','2026-03-31 09:00:00'),
-- H22
(54,17,'OWNER','프론트엔드','2026-04-01 09:00:00'),(54,14,'MEMBER','백엔드','2026-04-02 10:00:00'),
(55,15,'OWNER','프론트엔드','2026-04-02 10:00:00'),
-- H23
(56,20,'OWNER','크리에이티브','2026-04-03 09:00:00'),(56,19,'MEMBER','프론트엔드','2026-04-04 10:00:00'),
(57,13,'OWNER','데이터시각화','2026-04-04 10:00:00'),
-- H13 추가
(58,14,'OWNER','AI/ML','2026-03-28 10:00:00'),(58,18,'MEMBER','백엔드','2026-03-29 09:00:00'),
(59,17,'OWNER','백엔드','2026-03-30 11:00:00');

-- 대량 해커톤 등록
INSERT INTO hackathon_registrations (hackathon_id, team_id, registered_at) VALUES
(5, 29,'2026-03-10 10:00:00'),(5, 30,'2026-03-08 09:00:00'),(5, 31,'2026-03-09 10:00:00'),
(6, 32,'2026-03-04 10:00:00'),(6, 33,'2026-03-05 11:00:00'),
(15,34,'2025-09-05 10:00:00'),(15,35,'2025-09-07 14:00:00'),(15,36,'2025-09-10 11:00:00'),
(16,37,'2025-10-05 09:00:00'),(16,38,'2025-10-07 11:00:00'),(16,39,'2025-10-08 13:00:00'),
(17,40,'2025-11-05 09:00:00'),(17,41,'2025-11-07 10:00:00'),
(18,42,'2026-02-25 09:00:00'),(18,43,'2026-02-26 11:00:00'),(18,44,'2026-02-28 10:00:00'),
(19,45,'2026-03-05 09:00:00'),(19,46,'2026-03-06 10:00:00'),(19,47,'2026-03-07 11:00:00'),
(20,48,'2026-03-25 09:00:00'),(20,49,'2026-03-26 14:00:00'),(20,50,'2026-03-28 10:00:00'),
(21,51,'2026-03-28 09:00:00'),(21,52,'2026-03-29 10:00:00'),(21,53,'2026-03-30 11:00:00'),
(22,54,'2026-04-01 09:00:00'),(22,55,'2026-04-02 10:00:00'),
(23,56,'2026-04-03 09:00:00'),(23,57,'2026-04-04 10:00:00'),
(13,58,'2026-03-28 10:00:00'),(13,59,'2026-03-30 11:00:00');

-- ============================================================
-- AUTO_INCREMENT 재설정
-- ============================================================
ALTER TABLE users                    AUTO_INCREMENT = 21;
ALTER TABLE hackathons               AUTO_INCREMENT = 31;
ALTER TABLE tags                     AUTO_INCREMENT = 69;
ALTER TABLE teams                    AUTO_INCREMENT = 60;
ALTER TABLE submissions              AUTO_INCREMENT = 23;

-- ============================================================
-- 22. teams.deleted_at 컬럼 안내
-- 이 컬럼은 Spring Boot 부팅 시 ddl-auto: update가 자동으로 추가합니다.
-- (MySQL 8.0은 ALTER TABLE ... ADD COLUMN IF NOT EXISTS 미지원)
-- 만약 백엔드를 한 번도 띄우지 않은 상태라면 dummy_data.sql 적용 전에
-- ./gradlew bootRun 으로 한 번 부팅해서 컬럼을 생성해두세요.
-- ============================================================

-- ============================================================
-- 23. 엣지 케이스: 만석 팀 (Alpha Team을 정원=현재로 만들기)
-- ============================================================
UPDATE teams SET max_member_count = 2 WHERE id = 1;

-- ============================================================
-- 24. 엣지 케이스: 소프트 딜리트된 종료 해커톤 팀
-- ThemeForge(team 41, H17/ENDED) — 종료 후 본인이 삭제한 케이스
-- 정책상 종료 해커톤 팀은 소프트 딜리트만 가능하므로 status=DELETED + deleted_at
-- 본인 참가이력에는 노출되지만 상세/리더보드/카운트에서는 제외됨을 검증하는 시드
-- ============================================================
UPDATE teams SET status = 'DELETED', deleted_at = '2025-12-08 10:00:00' WHERE id = 41;

-- ============================================================
-- 25. 엣지 케이스: 다양한 신청 상태 (PENDING / REJECTED)
-- 기존 시드는 거의 ACCEPTED만 있어 모집 중 화면이 단조로움
-- ============================================================
INSERT INTO team_applications (team_id, user_id, message, position, status, processed_by_user_id, created_at, processed_at) VALUES
(13, 4,  '코드 리뷰 도구에 관심 많습니다. 합류 가능할까요?',  'LLM 엔지니어', 'PENDING',  NULL, '2026-04-08 10:00:00', NULL),
(14, 5,  'LangChain 기반 RAG 파이프라인 구축 경험 있습니다.', '백엔드',       'PENDING',  NULL, '2026-04-08 11:00:00', NULL),
(16, 9,  '핀테크 도메인은 처음이지만 빠르게 익히겠습니다.',    '프론트엔드',   'REJECTED', 6,    '2026-04-07 09:00:00', '2026-04-07 12:00:00'),
(25, 10, '장애인 이동 편의 프로젝트에 기여하고 싶습니다.',     'AI/ML',        'PENDING',  NULL, '2026-04-06 14:00:00', NULL),
(26, 4,  '독거노인 케어 서비스에 백엔드로 참여하고 싶습니다.', '백엔드',       'PENDING',  NULL, '2026-04-07 10:00:00', NULL);

-- ============================================================
-- 26. 채팅 데이터 (chat_participants / chat_messages)
-- supplement.sql에서 가치 있는 데이터만 흡수 (ID는 AUTO_INCREMENT 위임)
-- ============================================================
INSERT INTO chat_participants (hackathon_id, user_id, joined_at) VALUES
(7,  4,  '2026-03-25 10:30:00'),
(7,  5,  '2026-03-25 11:00:00'),
(7,  13, '2026-03-25 12:00:00'),
(7,  14, '2026-03-26 09:00:00'),
(7,  15, '2026-03-27 10:00:00'),
(13, 11, '2026-03-31 10:00:00'),
(13, 12, '2026-03-31 11:00:00'),
(13, 14, '2026-03-31 12:00:00'),
(13, 20, '2026-04-01 09:00:00'),
(14, 9,  '2026-04-01 10:00:00'),
(14, 7,  '2026-04-01 11:00:00'),
(14, 16, '2026-04-01 12:00:00'),
(14, 17, '2026-04-01 13:00:00');

INSERT INTO chat_messages (hackathon_id, user_id, content, created_at) VALUES
(7,  4,  '안녕하세요! 그린테크 해커톤 참가자 여러분~',                '2026-03-25 10:35:00'),
(7,  5,  '반갑습니다. 팀원 모집 중이신 분 계신가요?',                  '2026-03-25 11:05:00'),
(7,  13, '저희 팀은 프론트엔드 개발자 모집 중입니다',                  '2026-03-25 12:10:00'),
(7,  14, '탄소 절감 관련 아이디어 있으신 분 연락주세요',               '2026-03-26 09:15:00'),
(7,  15, '태양광 모니터링 앱 같이 하실 분 계신가요',                   '2026-03-27 10:20:00'),
(7,  4,  '다들 파이팅, 좋은 결과 있기를 바랍니다',                     '2026-03-28 14:00:00'),
(13, 11, '소셜임팩트 해커톤 시작됐네요. 반갑습니다',                   '2026-03-31 10:05:00'),
(13, 12, '저희 팀은 디지털 소외계층 지원 서비스 개발 예정입니다',      '2026-03-31 11:10:00'),
(13, 14, 'AI/ML 개발자 찾고 있어요',                                   '2026-03-31 12:15:00'),
(13, 20, '기획자인데 팀 합류 가능할까요?',                             '2026-04-01 09:30:00'),
(13, 11, '물론이죠. 쪽지 주세요',                                      '2026-04-01 10:00:00'),
(14, 9,  '클라우드 해커톤 참가자 모여라',                              '2026-04-01 10:10:00'),
(14, 7,  'K8s 관련 프로젝트 하시는 분 계신가요',                       '2026-04-01 11:15:00'),
(14, 16, 'DevOps 경력자 우대 모집 중입니다',                           '2026-04-01 12:20:00'),
(14, 17, '서버리스 아키텍처에 관심 있으신 분들 환영해요',               '2026-04-01 13:05:00'),
(14, 9,  '다들 좋은 아이디어로 좋은 결과 내봐요',                      '2026-04-02 09:00:00'),
(7,  15, '그린테크 파이팅',                                            '2026-04-02 10:00:00');

-- ============================================================
-- 27. ⏰ 시간 시프트 — 모든 datetime을 실행 시점 기준으로 자동 정렬
-- 원본 기준일: 2026-04-05
-- 매번 SQL을 다시 실행할 때 NOW() 기준으로 분포가 항상 의미 있게 유지됨
-- (HackathonStatusScheduler가 매분 status를 시간 기반으로 자동 갱신하므로
--  status 컬럼은 직접 손대지 않아도 곧 정합 상태가 됨)
-- ============================================================
SET @shift_days := DATEDIFF(CURDATE(), '2026-04-05');

UPDATE users
   SET created_at = created_at + INTERVAL @shift_days DAY,
       updated_at = updated_at + INTERVAL @shift_days DAY;

UPDATE hackathons
   SET start_date              = start_date              + INTERVAL @shift_days DAY,
       end_date                = end_date                + INTERVAL @shift_days DAY,
       registration_start_date = registration_start_date + INTERVAL @shift_days DAY,
       registration_end_date   = registration_end_date   + INTERVAL @shift_days DAY,
       submission_deadline_at  = CASE WHEN submission_deadline_at IS NOT NULL
                                      THEN submission_deadline_at + INTERVAL @shift_days DAY END,
       closed_at               = CASE WHEN closed_at IS NOT NULL
                                      THEN closed_at + INTERVAL @shift_days DAY END,
       created_at              = created_at + INTERVAL @shift_days DAY,
       updated_at              = updated_at + INTERVAL @shift_days DAY;

UPDATE milestones              SET date = date + INTERVAL @shift_days DAY;
UPDATE hackathon_judges        SET assigned_at = assigned_at + INTERVAL @shift_days DAY;

UPDATE teams
   SET created_at = created_at + INTERVAL @shift_days DAY,
       updated_at = updated_at + INTERVAL @shift_days DAY,
       deleted_at = CASE WHEN deleted_at IS NOT NULL
                         THEN deleted_at + INTERVAL @shift_days DAY END;

UPDATE team_private_infos
   SET created_at = created_at + INTERVAL @shift_days DAY,
       updated_at = updated_at + INTERVAL @shift_days DAY;

UPDATE team_members            SET joined_at = joined_at + INTERVAL @shift_days DAY;

UPDATE team_applications
   SET created_at   = created_at + INTERVAL @shift_days DAY,
       processed_at = CASE WHEN processed_at IS NOT NULL
                           THEN processed_at + INTERVAL @shift_days DAY END;

UPDATE hackathon_registrations SET registered_at = registered_at + INTERVAL @shift_days DAY;

UPDATE submissions
   SET submitted_at = submitted_at + INTERVAL @shift_days DAY,
       created_at   = created_at   + INTERVAL @shift_days DAY,
       updated_at   = updated_at   + INTERVAL @shift_days DAY;

UPDATE judge_evaluations
   SET created_at = created_at + INTERVAL @shift_days DAY,
       updated_at = updated_at + INTERVAL @shift_days DAY;

UPDATE votes                   SET voted_at = voted_at + INTERVAL @shift_days DAY;
UPDATE user_xp_history         SET earned_at = earned_at + INTERVAL @shift_days DAY;
UPDATE chat_participants       SET joined_at = joined_at + INTERVAL @shift_days DAY;
UPDATE chat_messages           SET created_at = created_at + INTERVAL @shift_days DAY;

-- ============================================================
-- 28. 대회 개요(description) 보강 — 200자 내외로 재작성
-- summary는 목록용 한 줄 설명으로 그대로 두고, 상세 페이지에 노출되는
-- description을 6~8문장(약 200자)으로 풍부하게 확장
-- ============================================================
UPDATE hackathons SET description = 'AI 기술로 차세대 스타트업 아이디어를 72시간 안에 프로토타입으로 구현하는 해커톤입니다. 머신러닝, 자연어 처리, 컴퓨터 비전 등 다양한 AI 분야를 활용해 사회 문제나 비즈니스 기회를 풀어내는 혁신 서비스를 만들어보세요. 분야별 멘토링과 투자자 피칭 세션이 함께 진행되며, 우수팀에게는 후속 투자 연계 기회와 인큐베이팅 프로그램 입주 기회가 제공됩니다.' WHERE id = 1;
UPDATE hackathons SET description = 'Meta Quest와 HoloLens 같은 최신 XR 기기를 직접 다루며 현실과 가상의 경계를 허무는 48시간 메타버스 챌린지입니다. 교육, 엔터테인먼트, 협업, 커머스 등 다양한 영역에서 몰입감 있는 XR 경험을 설계하고 구현해보세요. 기기 무료 대여, Unity·Unreal 엔진 라이선스, 분야별 멘토가 지원되며, 참가자 투표로 수상팀이 결정됩니다.' WHERE id = 2;
UPDATE hackathons SET description = '공공데이터 포털의 다양한 데이터셋을 활용해 사회 문제를 해결하는 데이터 사이언스 경진대회입니다. 교통, 복지, 환경, 경제 등 분야별 공공데이터를 분석해 의미 있는 인사이트를 도출하고, 통계 모델 또는 머신러닝 모델로 검증합니다. 1인 참가도 가능하며 Python·R·Jupyter 환경이 사전 제공됩니다. 발표는 데이터 기반 스토리텔링 중심으로 평가됩니다.' WHERE id = 3;
UPDATE hackathons SET description = 'iOS와 Android를 모두 아우르는 크로스플랫폼 모바일 앱을 48시간 안에 완성하는 해커톤입니다. Flutter, React Native, Swift, Kotlin 등 자유로운 기술 스택으로 일상의 불편함을 해소하는 앱을 개발하세요. 실제 기기에서 동작하는 빌드를 제출해야 하며, 사용자 경험과 디자인 완성도를 중심으로 참가자 투표를 통해 수상팀이 가려집니다.' WHERE id = 4;
UPDATE hackathons SET description = 'GPT, Claude 등 최신 대형 언어 모델(LLM)을 자유롭게 활용해 실제 문제를 해결하는 혁신 서비스를 만드는 해커톤입니다. 프롬프트 엔지니어링, RAG, 에이전트 워크플로우 등 최신 LLM 기법을 적용해 기업과 개인의 생산성을 극대화하는 AI 서비스를 개발합니다. OpenAI·Anthropic API 크레딧이 팀당 지원되며, 데모데이에서 실제 서비스 시연을 진행합니다.' WHERE id = 5;
UPDATE hackathons SET description = '블록체인, 오픈뱅킹 API, 결제 기술을 활용해 차세대 핀테크 서비스를 만드는 챌린지입니다. 송금, 자산 관리, 투자, 보험 등 금융 전 분야에서 사용자 경험을 혁신할 아이디어를 구현해보세요. 금융위원회 규정 준수 솔루션에는 추가 가점이 주어지며, 보안과 사용자 편의성을 균형 있게 갖춘 팀이 우대됩니다. 실제 금융사 PoC 연계 기회도 제공됩니다.' WHERE id = 6;
UPDATE hackathons SET description = '기후 변화 대응을 위한 친환경 기술 솔루션을 개발하는 그린테크 해커톤입니다. 탄소 감축, 신재생 에너지, 스마트 그리드, 자원 순환 등 환경 문제를 기술로 해결할 수 있는 아이디어를 구체적인 서비스로 구현합니다. 현장 참여 필수이며 숙박과 식사가 무료 제공됩니다. 환경 임팩트와 실현 가능성을 중심으로 참가자 투표를 통해 우수팀을 선정합니다.' WHERE id = 7;
UPDATE hackathons SET description = '디지털 헬스케어 기술로 의료 접근성을 높이는 헬스케어 이노베이션 해커톤입니다. 원격 진료, 웨어러블 데이터 분석, AI 진단 보조, 환자 케어 챗봇 등 의료 현장의 실질적 문제를 해결하는 서비스를 개발하세요. 의료법 관련 규정 준수가 평가 항목에 포함되며, 임상 전문가 멘토링과 의료 데이터셋이 제공됩니다. 환자와 의료진 모두를 고려한 UX가 핵심입니다.' WHERE id = 8;
UPDATE hackathons SET description = 'IoT 센서와 빅데이터 분석으로 더 스마트한 도시 인프라를 구축하는 해커톤입니다. 교통, 에너지, 안전, 환경, 시민 참여 등 도시 운영의 다양한 영역에서 데이터 기반 솔루션을 설계하고 구현합니다. 서울시 열린데이터광장과 부산 빅데이터 플랫폼 API가 무료 제공되며, 실제 도시 문제 해결력과 공공데이터 활용도가 핵심 평가 지표가 됩니다.' WHERE id = 9;
UPDATE hackathons SET description = '실전형 CTF와 보안 솔루션 개발을 병행하는 사이버보안 종합 챌린지입니다. 웹·시스템·네트워크·암호 등 다양한 분야의 취약점을 분석하고, 발견한 취약점에 대한 방어 솔루션까지 함께 구현합니다. 모든 분석은 지정된 샌드박스 환경에서만 수행해야 하며, 취약점의 심각도와 보고서 품질, 방어 코드의 완성도가 종합적으로 평가됩니다.' WHERE id = 10;
UPDATE hackathons SET description = 'EVM 기반 스마트 컨트랙트와 DeFi, NFT 기술을 활용해 탈중앙화 서비스를 48시간 안에 구현하는 웹3 해커톤입니다. Solidity, Rust(Anchor), Move 등 자유로운 언어로 dApp을 개발하고 Sepolia 또는 Mumbai 테스트넷에 배포해야 합니다. 메인넷 배포는 선택 사항이며, 실질적 탈중앙화 구현 여부와 사용자 경험을 함께 평가합니다.' WHERE id = 11;
UPDATE hackathons SET description = 'AI와 데이터 기술로 교육의 미래를 바꾸는 에듀테크 챌린지입니다. 개인 맞춤 학습, 학습 분석, 교육 접근성, 교사 지원 도구 등 교육 현장의 다양한 문제를 기술로 해결하는 서비스를 개발합니다. 현장 참여 필수이며 숙박·식사가 제공됩니다. 학습 효과를 입증할 수 있는 데이터 또는 사용자 테스트 결과가 우대되며, 참가자 투표로 수상팀이 결정됩니다.' WHERE id = 12;
UPDATE hackathons SET description = '기술로 사회 문제를 해결하는 임팩트 있는 서비스를 개발하는 해커톤입니다. 환경, 복지, 교육 불평등, 사회적 약자 지원 등 사회적 가치를 중심으로 평가하며, 기술적 완성도보다 사회적 임팩트의 크기와 지속 가능성을 우선합니다. 우수팀에게는 사회적기업 인큐베이팅 프로그램 입주와 후속 임팩트 투자 연계 기회가 제공됩니다.' WHERE id = 13;
UPDATE hackathons SET description = 'Docker, Kubernetes, 서비스 메시 등 클라우드 네이티브 기술 스택을 활용해 확장 가능한 서비스를 설계·배포하는 챌린지입니다. 마이크로서비스 아키텍처, CI/CD 파이프라인, 관측성, 무중단 배포 등 현대적 운영 역량을 종합적으로 검증합니다. AWS·GCP·Azure 크레딧이 팀당 제공되며, 참가자 투표로 우수팀을 선정합니다.' WHERE id = 14;
UPDATE hackathons SET description = 'HuggingFace 모델 허브를 활용해 텍스트 데이터에서 인사이트를 추출하는 NLP 경진대회입니다. 감성 분석, 개체명 인식, 문서 분류, 질의응답 등 NLP 핵심 과제를 다루며 자체 데이터셋도 자유롭게 활용할 수 있습니다. 1인 참가도 가능하며 사전 학습 모델 기반의 파인튜닝 전략과 모델 성능, 분석 깊이가 종합적으로 평가됩니다.' WHERE id = 15;
UPDATE hackathons SET description = 'GitHub Actions, Jenkins, Terraform, Ansible 등 다양한 도구를 활용해 CI/CD 파이프라인과 인프라 자동화를 구축하는 DevOps 챌린지입니다. 자동화된 배포, 인프라 코드, 모니터링·알림 체계, 무중단 롤백 시나리오까지 종합적으로 평가됩니다. 1인 참가가 가능하며, 참가자 투표로 가장 효율적이고 안정적인 솔루션이 선정됩니다.' WHERE id = 16;
UPDATE hackathons SET description = 'Figma와 React를 활용해 재사용 가능한 디자인 시스템을 구축하는 해커톤입니다. 컴포넌트 라이브러리, 디자인 토큰, 다크/라이트 테마, 접근성 가이드라인을 포함한 완성도 높은 시스템을 설계하고 코드와 문서까지 함께 산출해야 합니다. 디자인 일관성과 컴포넌트 완성도, 접근성 준수 수준이 종합적으로 평가됩니다.' WHERE id = 17;
UPDATE hackathons SET description = '협업 필터링과 딥러닝 기반 추천 시스템을 구현하는 경진대회입니다. MovieLens, Amazon 리뷰 등 공개 데이터셋을 활용해 개인화 추천 모델을 개발하며, NDCG·Hit Rate 등 표준 지표로 객관적으로 평가됩니다. 콜드 스타트 문제, 다양성, 설명 가능성 등 추천 시스템의 핵심 난제를 어떻게 풀어내는지가 핵심 평가 기준입니다.' WHERE id = 18;
UPDATE hackathons SET description = 'MLflow, Kubeflow, Airflow 등을 활용해 ML 모델의 학습부터 배포·모니터링까지 End-to-End 파이프라인을 구축하는 MLOps 챌린지입니다. 재현 가능한 실험 환경, 데이터·모델 버전 관리, 자동화된 학습·배포 워크플로우를 모두 갖춰야 합니다. 운영 환경에서의 안정성과 재현성, 자동화 수준이 평가의 핵심입니다.' WHERE id = 19;
UPDATE hackathons SET description = 'Whisper, ElevenLabs, Coqui TTS 등 최신 음성 AI 모델을 활용해 음성 기반 서비스를 개발하는 해커톤입니다. 음성 인식, 음성 합성, 화자 분리, 실시간 번역 등 다양한 음성 기술을 조합해 기존에 없던 새로운 사용자 경험을 만들어보세요. 참가자 투표를 통해 가장 자연스럽고 유용한 서비스가 수상팀으로 선정됩니다.' WHERE id = 20;
UPDATE hackathons SET description = 'YOLO, SAM, CLIP 등 최신 비전 모델을 활용해 실제 산업 문제를 해결하는 컴퓨터 비전 챌린지입니다. 이미지 인식, 객체 탐지, 세그멘테이션, 자세 추정 등 다양한 비전 과제를 다루며, 1인 참가가 가능합니다. 모델 정확도뿐 아니라 실제 산업 현장에 적용 가능한 응용성과 추론 효율성도 함께 평가됩니다.' WHERE id = 21;
UPDATE hackathons SET description = 'WCAG 2.1 기준을 충족하는 접근성 높은 웹 서비스를 만드는 해커톤입니다. 시각·청각·운동 장애를 가진 사용자도 불편 없이 사용할 수 있는 UI/UX와 보조 기술 호환성을 함께 구현해야 합니다. 자동화 검사 도구와 실제 장애인 사용자 테스트를 거쳐 평가되며, 참가자 투표로 가장 포용적인 서비스가 선정됩니다.' WHERE id = 22;
UPDATE hackathons SET description = 'p5.js와 Processing으로 예술과 코딩의 경계를 허무는 작품을 만드는 크리에이티브 코딩 해커톤입니다. 제너러티브 아트, 인터랙티브 설치 미술, 데이터 시각화 아트워크 등 코드로 표현하는 창작물을 자유롭게 만들어보세요. 1인 참가가 가능하며, 작품의 창의성과 시각적 완성도, 기술적 표현력이 종합적으로 평가됩니다.' WHERE id = 23;
UPDATE hackathons SET description = 'CARLA 시뮬레이터 환경에서 자율주행 알고리즘을 구현하고 평가하는 시뮬레이션 대회입니다. 경로 계획, 장애물 회피, 신호 인식, 주차 등 자율주행의 핵심 모듈을 직접 개발하고 다양한 시나리오에서 검증합니다. 안전성과 알고리즘 성능, 시뮬레이션 결과의 재현성이 핵심 평가 기준이며 현장 캠프 형식으로 진행됩니다.' WHERE id = 24;
UPDATE hackathons SET description = '인기 오픈소스 프로젝트의 이슈를 직접 해결하고 PR을 제출하며 기여 경험을 쌓는 컨트리뷰톤입니다. 멘토가 코드 리뷰와 메인테이너 커뮤니케이션을 도와주며, 머지된 PR의 임팩트와 코드 품질, 리뷰 대응력이 평가됩니다. 1인 참가가 가능하고 기여 경험이 부족한 분도 환영하며, 참가자 투표로 우수 기여자를 선정합니다.' WHERE id = 25;
UPDATE hackathons SET description = 'Raspberry Pi, Jetson Nano, Coral 같은 엣지 디바이스에서 AI 추론을 최적화하는 엣지 AI 해커톤입니다. 모델 양자화, 프루닝, 지식 증류, 컴파일러 최적화 등의 기법을 활용해 제한된 자원에서도 실시간 추론이 가능한 시스템을 구축합니다. 추론 속도, 모델 크기, 정확도 유지의 균형이 핵심 평가 지표가 됩니다.' WHERE id = 26;
UPDATE hackathons SET description = 'Spark, Kafka, Flink 등 대규모 데이터 처리 도구를 활용해 안정적인 데이터 파이프라인을 구축하는 챌린지입니다. 실시간 스트리밍, 배치 처리, 데이터 레이크 구축, 데이터 품질 검증까지 데이터 엔지니어링 전 영역을 다룹니다. 처리량과 안정성, 운영 효율, 장애 복구 시나리오가 종합적으로 평가됩니다.' WHERE id = 27;
UPDATE hackathons SET description = '카카오맵, 공공데이터, OpenWeather, 환율 API 등 다양한 공공·민간 API를 매시업해 새로운 가치를 창출하는 해커톤입니다. 단순 호출이 아니라 여러 API를 조합해 기존에 없던 사용자 경험을 만들어내는 창의력이 핵심입니다. 참가자 투표를 통해 가장 독창적이고 유용한 매시업 서비스가 수상팀으로 선정됩니다.' WHERE id = 28;
UPDATE hackathons SET description = 'Unity 또는 Unreal Engine으로 48시간 안에 플레이 가능한 게임 한 편을 완성하는 게임 잼입니다. 행사 시작 시점에 발표되는 테마에 따라 기획부터 그래픽, 사운드, 프로그래밍까지 모두 직접 수행해야 합니다. 무료 아트 에셋 라이브러리가 제공되며, 게임성·완성도·창의성이 종합적으로 평가됩니다.' WHERE id = 29;
UPDATE hackathons SET description = 'ROS2 기반 로봇 제어와 자율주행 알고리즘을 구현하는 로보틱스 챌린지입니다. 시뮬레이션 환경에서 알고리즘을 개발한 뒤 최종일에는 실제 로봇으로 미션을 수행해야 하며, 환경 인식, 경로 계획, 모션 제어, 에러 복구까지 종합적인 로봇 제어 역량이 검증됩니다. 캠프 형식으로 진행되며 ROS2 멘토가 상시 지원합니다.' WHERE id = 30;

-- ============================================================
-- 29. 평가 기준(criteria) 보강 — 누락된 21개 해커톤에 4개씩 추가
-- VOTE 타입에도 동일하게 추가하여 모든 해커톤이 최소 4개 보유
-- ============================================================
INSERT INTO criteria (hackathon_id, name, description, max_score) VALUES
-- H2 XR 메타버스 (VOTE)
(2, '몰입감',         'XR 기기에서 체감되는 몰입의 깊이',                    30),
(2, '기술 완성도',    'XR 인터랙션 및 렌더링 구현 수준',                     30),
(2, '창의성',         '독창적인 발상과 표현 방식',                           30),
(2, '발표력',         '시연 및 Q&A 대응',                                    10),
-- H4 모바일 앱 (VOTE)
(4, '사용자 경험',    '실제 사용 시 편의성과 직관성',                        35),
(4, '기술 완성도',    '실기기 동작 안정성 및 코드 품질',                     30),
(4, '시장성',         '잠재 사용자층과 비즈니스 모델',                       25),
(4, '발표력',         '데모 시연 품질',                                      10),
-- H7 그린테크 (VOTE)
(7, '환경 임팩트',    '탄소 감축 또는 환경 개선 정량 효과',                  35),
(7, '기술 완성도',    '서비스 구현 및 안정성',                               30),
(7, '지속 가능성',    '운영 지속 가능성 및 확장성',                          25),
(7, '발표력',         '발표 명확성 및 설득력',                               10),
-- H12 에듀테크 (VOTE)
(12, '학습 효과',     '실제 학습 향상 효과를 입증할 수 있는지',              35),
(12, '기술 완성도',   '서비스 구현 및 안정성',                               25),
(12, '사용자 경험',   '학습자 및 교사 UX',                                   30),
(12, '발표력',        '발표 및 시연 품질',                                   10),
-- H14 클라우드 네이티브 (VOTE)
(14, '아키텍처 설계', '마이크로서비스 및 클라우드 네이티브 설계 수준',       30),
(14, '확장성',        '수평 확장 및 무중단 배포 구현',                       30),
(14, '운영 효율',     '관측성 및 자동화 구성',                               30),
(14, '발표력',        '데모 및 Q&A 대응',                                    10),
-- H15 NLP 텍스트 마이닝 (SCORE)
(15, 'NLP 활용도',    '사전 학습 모델 및 NLP 기법 활용의 적절성',            30),
(15, '모델 성능',     '정량 지표(F1, 정확도 등) 기준 성능',                  35),
(15, '분석 깊이',     '결과 해석 및 인사이트 도출 수준',                     25),
(15, '발표력',        '데이터 기반 스토리텔링',                              10),
-- H16 DevOps 자동화 (VOTE)
(16, '자동화 수준',   'CI/CD 및 인프라 자동화 범위',                         35),
(16, '안정성',        '롤백 및 장애 대응 시나리오',                          30),
(16, '효율성',        '빌드·배포 시간 및 리소스 효율',                       25),
(16, '발표력',        '시연 및 Q&A 대응',                                    10),
-- H17 디자인 시스템 (SCORE)
(17, '디자인 일관성', '컴포넌트와 토큰의 일관성',                            30),
(17, '컴포넌트 완성도','재사용성과 API 설계 품질',                           30),
(17, '접근성',        'WCAG 가이드라인 준수 수준',                           30),
(17, '발표력',        '문서 및 발표 품질',                                   10),
-- H18 추천 시스템 (SCORE)
(18, '모델 성능',     'NDCG 및 Hit Rate 기준 성능',                          40),
(18, '추천 정확도',   '실제 사용 시 추천 만족도',                            25),
(18, '시스템 설계',   '학습·서빙 파이프라인 설계',                           25),
(18, '발표력',        '결과 해석 및 발표 품질',                              10),
-- H19 MLOps 파이프라인 (SCORE)
(19, '파이프라인 설계','학습·배포·모니터링 전 단계 구성',                    35),
(19, '재현성',        '실험 재현성 및 데이터·모델 버전 관리',                30),
(19, '자동화',        '워크플로우 자동화 수준',                              25),
(19, '발표력',        '시연 및 발표 품질',                                   10),
-- H20 음성 AI (VOTE)
(20, '음성 처리 정확도','STT/TTS 품질 및 실시간성',                          35),
(20, '사용성',        '실제 사용 시 자연스러움과 편의성',                    30),
(20, '응용 영역',     '새로운 사용자 경험 창출 정도',                        25),
(20, '발표력',        '데모 시연 품질',                                      10),
-- H21 컴퓨터 비전 (SCORE)
(21, '모델 정확도',   '객체 탐지·분류 등 정량 지표',                         40),
(21, '응용성',        '실제 산업 현장 적용 가능성',                          25),
(21, '기술 완성도',   '추론 효율 및 시스템 통합',                            25),
(21, '발표력',        '결과 발표 및 시연',                                   10),
-- H22 웹 접근성 (VOTE)
(22, 'WCAG 준수도',   '자동화 검사 및 수동 검증 결과',                       35),
(22, '사용성',        '보조 기술 사용자 테스트 결과',                        30),
(22, '기술 구현',     '접근성 구현의 기술적 완성도',                         25),
(22, '발표력',        '발표 및 시연 품질',                                   10),
-- H23 크리에이티브 코딩 (SCORE)
(23, '창의성',        '독창적 발상 및 표현',                                 35),
(23, '시각적 완성도', '결과물의 시각적 완성도',                              30),
(23, '기술 표현력',   '코드를 통한 표현의 정교함',                           25),
(23, '발표력',        '작품 설명 및 시연',                                   10),
-- H24 자율주행 시뮬레이션 (SCORE)
(24, '알고리즘 성능', '경로 계획 및 제어 알고리즘 품질',                     35),
(24, '안전성',        '충돌 회피 및 안전 운행 지표',                         30),
(24, '시뮬레이션 결과','다양한 시나리오에서의 성공률',                       25),
(24, '발표력',        '결과 발표 및 시연',                                   10),
-- H25 오픈소스 컨트리뷰톤 (VOTE)
(25, 'PR 품질',       '코드 품질 및 머지 가능성',                            35),
(25, '기여 임팩트',   '프로젝트에 미친 실질적 임팩트',                       30),
(25, '코드 리뷰 대응','메인테이너 리뷰에 대한 대응력',                       25),
(25, '발표력',        '기여 과정 발표',                                      10),
-- H26 Edge AI (SCORE)
(26, '추론 속도',     '엣지 디바이스 상의 실시간성',                         35),
(26, '모델 경량화',   '양자화·프루닝 등 경량화 기법 적용 수준',              30),
(26, '정확도 유지',   '경량화 이후 정확도 손실 최소화',                      25),
(26, '발표력',        '결과 발표 및 시연',                                   10),
-- H27 데이터 엔지니어링 (SCORE)
(27, '파이프라인 설계','데이터 파이프라인 아키텍처 품질',                    35),
(27, '처리량',        '대용량 데이터 처리 성능',                             25),
(27, '안정성',        '장애 복구 및 데이터 품질 검증',                       30),
(27, '발표력',        '결과 발표 및 시연',                                   10),
-- H28 API 이코노미 (VOTE)
(28, '매시업 창의성', 'API 조합의 독창성',                                   35),
(28, '서비스 완성도', '실제 동작하는 서비스의 완성도',                       30),
(28, '사용성',        '사용자가 체감하는 가치',                              25),
(28, '발표력',        '데모 시연 품질',                                      10),
-- H29 게임 개발 잼 (SCORE)
(29, '게임성',        '재미와 플레이 경험',                                  35),
(29, '완성도',        '버그 없이 끝까지 플레이 가능한 수준',                 30),
(29, '창의성',        '테마 해석 및 독창성',                                 25),
(29, '발표력',        '시연 및 발표 품질',                                   10),
-- H30 로보틱스 (SCORE)
(30, '로봇 제어',     '환경 인식과 모션 제어의 정밀도',                      30),
(30, '미션 수행',     '실제 미션 성공률 및 시간',                            35),
(30, '알고리즘',      '경로 계획 및 의사결정 알고리즘',                      25),
(30, '발표력',        '결과 발표 및 시연',                                   10);
