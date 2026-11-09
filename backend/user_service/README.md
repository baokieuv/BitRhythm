# user-service

Spring Boot 3.3 / Java 21. Cấu trúc theo tài liệu thiết kế (S1 = MVP, S2 = giai đoạn 2).

## Trạng thái
- **[S1] đã có code**: profile, settings, avatar (MinIO presigned), follow (cursor, bộ đếm atomic), outbox + `NEW_FOLLOWER`.
- **[S2] mới là khung**: entity, repository, migration `V2__stage2.sql`, class rỗng có TODO (lifecycle, search, block, admin, artist, internal).

## Chạy
```bash
# cần Postgres (db user_service), Kafka, MinIO, auth-service (JWKS)
mvn spring-boot:run
```
Biến môi trường: `DB_URL, DB_USER, DB_PASSWORD, AUTH_JWK_SET_URI, KAFKA_BOOTSTRAP, MINIO_ENDPOINT, MINIO_PUBLIC_ENDPOINT, MINIO_ACCESS_KEY, MINIO_SECRET_KEY, NOTIFICATION_TOPIC`.

## Việc cần làm khi gắn vào hệ thống thật
1. **core**: project này tự chứa `ApiResponseCode`, `GlobalExceptionHandler`, `CursorPage`, outbox. Thay bằng bản của core (`ApiResponseCode`, `BaseEntity`, `OutboxEvent`, `ServiceTokenProvider`) rồi xóa bản tạm.
2. **SecurityConfig**: copy chain của notification-service (kiểm tra TOKEN_ACCESS / TOKEN_SERVICE + scope). Bản hiện tại chỉ xác thực JWT cơ bản; `/internal/**` đang `denyAll`.
3. **NotificationEvent**: đối chiếu với hợp đồng thật của notification-service (tên topic, field).
4. **MinIO**: tạo bucket `avatars` và đặt policy đọc công khai (anonymous download). Bucket audio để private.
5. **Gateway**: đổi route từ `Path=/api/user/**, StripPrefix=2` thành `Path=/api/users/**, StripPrefix=1` (giống `/api/notifications`).
6. **auth-service** (S2): phát `USER_REGISTERED`, `USER_DELETED/USER_DISABLED`; endpoint `POST /internal/users/{id}/roles` (scope `roles:write`).
7. Thứ tự route `GET /users/search` (S2) phải không bị `/users/{id}` nuốt (`{id}` là `Long` nên sẽ lỗi 400 nếu trùng).

## API [S1]
| Endpoint | Mô tả |
|---|---|
| `GET/PUT /users/me`, `GET /users/{id}` | hồ sơ |
| `GET/PUT /users/me/settings` | cài đặt |
| `POST /users/me/avatar/upload-url`, `POST /users/me/avatar/confirm`, `DELETE /users/me/avatar` | avatar |
| `PUT/DELETE /users/{id}/follow`, `GET /users/{id}/followers`, `GET /users/{id}/following` | follow (cursor) |

Presigned URL: client `PUT` thẳng lên MinIO bằng `uploadUrl`, sau đó gọi `confirm` với `key`.
