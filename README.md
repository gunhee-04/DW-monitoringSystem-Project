
---


# DW-monitoringSystem-Project

## 프로젝트 소개
DW-monitoringSystem-Project는 AI 영상 탐지 서버로부터 전달받은 데이터를 기반으로 카메라와 위험구역을 관리하는 관제 시스템 프로젝트입니다.

Spring Boot 기반으로 관리자 페이지를 구현하였으며, 카메라 등록/수정/조회/삭제와 위험구역 설정 기능을 통해 실시간 모니터링 환경을 효율적으로 운영할 수 있도록 설계하였습니다.

---

## 주요 기능
- 카메라 등록, 수정, 상세 조회, 삭제
- 위험구역 등록, 수정, 조회, 삭제
- 카메라 상태 변경 API 제공
- 카메라 코드 기준 설정 정보 조회 API 제공
- 관리자 페이지에서 카메라 및 위험구역 통합 관리
- MySQL 기반 데이터 저장 및 관리

---

## 프로젝트 구조
```bash
DW-monitoringSystem-Project
└── safetysystem
    ├── src
    ├── build.gradle
    ├── settings.gradle
    └── gradlew
