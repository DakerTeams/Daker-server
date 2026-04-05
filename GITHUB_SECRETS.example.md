# GitHub Secrets Example

`Settings -> Secrets and variables -> Actions`에 아래 값을 등록합니다.

## Server Secrets

### `EC2_HOST`
```text
your-ec2-host
```

### `EC2_USER`
```text
ubuntu
```

### `EC2_PORT`
```text
22
```

### `EC2_SSH_KEY`
```text
-----BEGIN OPENSSH PRIVATE KEY-----
your-private-key
-----END OPENSSH PRIVATE KEY-----
```

### `SERVER_APP_DIR`
```text
/home/ubuntu/Daker-server
```

### `SERVER_BUILD_COMMAND`
```text
docker compose up -d && chmod +x gradlew && ./gradlew clean build -x test
```

### `SERVER_STOP_COMMAND`
```text
pkill -f 'java -jar' || true
```

### `SERVER_START_COMMAND`
```text
nohup java -jar build/libs/*.jar > app.log 2>&1 &
```

### `SERVER_HEALTHCHECK_URL`
```text
http://15.164.250.113:8080/swagger-ui/index.html
```

## Notes
- 이 파일은 예시 문서입니다.
- 실제 secret 값은 GitHub Actions Secrets에만 등록하고 Git에는 올리지 않습니다.
- `SERVER_START_COMMAND`와 `SERVER_HEALTHCHECK_URL`은 실제 운영 방식에 맞게 수정해야 합니다.
