# campingsafe (혼자 차박 안심 판정)

PostgreSQL(PostGIS) → Spring Boot API (8080) → Vue + 카카오맵 (5173)
자세한 설치 방법과 에러 해결은 노션의 "팀원 PC 실행 가이드 (campingsafe)" 페이지를 보세요.

## 폴더
- backend/   : Spring Boot + MyBatis (STS/이클립스에서 Import → Existing Maven Projects)
- frontend/  : Vue 3 + Vite (VS Code)
- db/        : campingsafe_score_add.sql (PostGIS·점수 가중치) — 덤프(campingsafe.sql)를 먼저 복원한 뒤 실행

## 빠른 실행
1. pgAdmin: campingsafe.sql 복원 → db/campingsafe_score_add.sql 실행
2. backend/src/main/resources/application.properties 의 YOUR_PASSWORD → 본인 PostgreSQL 비밀번호
3. 이클립스: CampingsafeApplication 우클릭 → Run As → Spring Boot App
   - 확인: http://localhost:8080/api/score/camps
4. frontend/.env 파일을 만들고 전달받은 카카오 키 한 줄 붙여넣기
   VITE_KAKAO_JS_KEY=전달받은키
5. VS Code 터미널
   cd frontend
   npm install
   npm run dev
   → http://localhost:5173
