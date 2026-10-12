-- =====================================================
-- campingsafe 점수 계산용 추가 SQL
-- 기존 campingsafe.sql(덤프)를 복원한 뒤 이어서 실행
-- =====================================================
SET search_path TO campingsafe, public;

-- 1) PostGIS + 위치 컬럼 (거리 계산용)
CREATE EXTENSION IF NOT EXISTS postgis;

ALTER TABLE facilities ADD COLUMN IF NOT EXISTS geom geography(Point, 4326);

UPDATE facilities
SET geom = ST_SetSRID(ST_MakePoint(lng, lat), 4326)::geography
WHERE lat IS NOT NULL AND lng IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_facilities_geom ON facilities USING GIST (geom);
CREATE INDEX IF NOT EXISTS idx_facilities_type ON facilities (type);

-- 2) 가중치 테이블 (type 값은 실제 데이터 기준: police / er / fire / store / toilet)
DROP TABLE IF EXISTS score_weights;
CREATE TABLE score_weights (
  facility_type VARCHAR(100) PRIMARY KEY,
  weight        INT NOT NULL,   -- 최대 점수 (합계 100)
  full_dist_m   INT NOT NULL,   -- 이 거리 안이면 만점
  zero_dist_m   INT NOT NULL    -- 이 거리 밖이면 0점
);

INSERT INTO score_weights VALUES
('police', 30,  1000, 10000),
('er',     25,  5000, 50000),
('fire',   15,  3000, 15000),
('store',  15,   500,  3000),
('toilet', 15,   200,  1000);

-- 3) 점검: 가중치 type 중 시설 데이터에 없는 것 (0행이어야 정상)
SELECT w.facility_type
FROM score_weights w
WHERE NOT EXISTS (SELECT 1 FROM facilities f WHERE f.type = w.facility_type);

-- 4) 점수 테스트 (인제읍 근처 좌표 예시 — ST_MakePoint(lng, lat) 순서 주의)
WITH p AS (
  SELECT ST_SetSRID(ST_MakePoint(128.1700, 38.0690), 4326)::geography AS g
)
SELECT w.facility_type,
       n.name,
       ROUND(n.dist::numeric)            AS dist_m,
       ROUND((
         w.weight
         * GREATEST(0, LEAST(1, (w.zero_dist_m - n.dist) / (w.zero_dist_m - w.full_dist_m)))
       )::numeric, 1)                     AS point
FROM score_weights w
CROSS JOIN p
LEFT JOIN LATERAL (
  SELECT f.name, ST_Distance(f.geom, p.g) AS dist
  FROM facilities f
  WHERE f.type = w.facility_type
    AND f.is_active
    AND f.geom IS NOT NULL
  ORDER BY f.geom <-> p.g
  LIMIT 1
) n ON true
ORDER BY w.weight DESC;

-- 5) 검증: 캠핑장(camp) 67곳 전체 점수 분포 보기
SELECT c.name,
       ROUND(SUM(
         w.weight * GREATEST(0, LEAST(1, (w.zero_dist_m - n.dist) / (w.zero_dist_m - w.full_dist_m)))
       )::numeric, 1) AS total_score
FROM facilities c
CROSS JOIN score_weights w
LEFT JOIN LATERAL (
  SELECT ST_Distance(f.geom, c.geom) AS dist
  FROM facilities f
  WHERE f.type = w.facility_type AND f.is_active AND f.geom IS NOT NULL
  ORDER BY f.geom <-> c.geom
  LIMIT 1
) n ON true
WHERE c.type = 'camp' AND c.geom IS NOT NULL
GROUP BY c.name
ORDER BY total_score DESC;
