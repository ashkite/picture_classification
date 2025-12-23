# Picture Classification (Android MVP)

휴대폰의 사진/영상을 **온디바이스에서** 분석해 날짜/장소별로 자동 분류하고,
인물/이벤트 태그까지 제공하는 Android 앱을 목표로 합니다.

## 목표

- Android MVP: 로컬 기기 내에서만 처리(업로드 없음)
- 장소: 도시/국가 단위 분류, 표시는 도시 중심
- 날짜: 연/월/일 기준
- 인물/이벤트 태그: 오탐보다 미탐을 우선하는 보수적 분류

## MVP 기능

- MediaStore 기반 스캔(사진/영상) + 증분 업데이트
- EXIF/메타데이터 파싱 및 날짜/시간 보정
- 오프라인 역지오코딩으로 도시/국가 매핑
- 위치 없는 항목은 별도 폴더로 분리
- 인물/이벤트 태그(수동 태깅 포함, 자동 분류는 모델 연동 예정)
- Compose UI: 장소/날짜/인물/이벤트/위치없음 탭
- 분류 수정/병합/태그 편집

## 분류 규칙 (핵심 로직)

- 기본 그룹: `도시(표시)` + `연/월/일`
- 위치 없음: `Location Unknown` 폴더로 분리 후 날짜별 하위 그룹
- 태그 부여: confidence 임계치 상향(예: 0.8 이상)으로 오탐 최소화

## ML 파이프라인 (TFLite)

- **이벤트 분류**: EfficientNet-Lite0 기반 멀티라벨 분류기
- **인물 판별**: BlazeFace 얼굴 검출 → 얼굴 >= 1이면 인물
- **선택 기능**: 얼굴 임베딩(예: MobileFaceNet) + 클러스터링(DBSCAN)로
  사용자 라벨링(이 사람은 누구?) 지원
- **정확도 정책**: 오탐보다 미탐 우선, 불확실하면 미태깅
- **최적화**: int8 양자화(대표 데이터 캘리브레이션), NNAPI/GPU delegate

### 초기 이벤트 카테고리

- 여행/관광, 자연/야외, 도시/거리, 음식/레스토랑, 카페/디저트
- 파티/축하, 결혼식, 가족/아이, 회의/업무, 공연/무대
- 스포츠/운동, 동물/반려동물, 해변/바다, 산/하이킹, 야경/밤

### 데이터셋 후보

- Places365, Open Images, Food-101, 이벤트/장면 데이터셋(공개)
- 라벨 매핑 테이블로 이벤트 카테고리와 연결

## 데이터 파이프라인

- 권한: `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `ACCESS_MEDIA_LOCATION`
- 파싱: ExifInterface/MediaMetadataRetriever
- 날짜: DateTimeOriginal > DATE_TAKEN > 파일 수정시간
- 타임존: EXIF 오프셋/위치 기반 보정
- 역지오코딩: 오프라인 도시 DB(예: GeoNames) + geohash 인덱스
- 초기 시드: `app/src/main/assets/cities_seed.csv`(GeoNames cities15000 기반)

## 문서

- `docs/tflite_pipeline.md`: 모델 학습/변환/배포 파이프라인
- `docs/label_mapping.md`: 이벤트 카테고리 라벨 매핑 초안
- `docs/offline_geocoding.md`: 오프라인 도시 매핑 설계
- `docs/privacy_policy.md`: 프라이버시 정책 초안
- `docs/release.md`: Play Console 릴리즈 가이드
- `docs/data_safety.md`: 데이터 안전성(Play Console) 초안
- `tools/geonames/build_cities_csv.py`: GeoNames CSV 생성 스크립트

## 저장소/아키텍처 (예정)

- Room + WorkManager + Compose
- 주요 테이블: `media_item`, `city`, `tag`, `media_tag`, `face_cluster`, `scan_state`
- 백그라운드 스캔: 배터리/충전 조건 기반, 증분 인덱싱

## 프라이버시

- 모든 분석은 기기 내에서 수행
- 네트워크 연결 없이 동작 가능
- 모델/도시 DB는 번들 포함(필요 시 Play Asset Delivery 전환 가능)

## 배포 흐름 (계획)

- AAB 빌드 → Play Console 내부 테스트 → 공개
- 데이터 안전/프라이버시 정책 문서화

## 로드맵 (요약)

1) 앱 스캐폴딩 + Room/WorkManager 기본 동작  
2) 오프라인 지오데이터 통합  
3) TFLite 모델 학습/변환/통합  
4) 정확도/성능 튜닝 및 사용자 수정 UX  
5) 배포 준비 및 테스트
