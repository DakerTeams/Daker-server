# GitHub Secrets Example

`Settings -> Secrets and variables -> Actions`에 아래 값을 등록합니다.

## Server Secrets

### `EC2_HOST`
```text
15.164.250.113
```

### `EC2_USER`
```text
ec2-user
```

### `EC2_PORT`
```text
22
```

### `EC2_SSH_KEY`
```text
-----BEGIN RSA PRIVATE KEY-----
MIIEpQIBAAKCAQEAxgMTEOQ8jAIJzxFeW+JyyJPelcvsBwDsz3JTqlsIf055bp6L
EdKbvLeKsOng40cWlX1rUzbiIHolXqQ8FGd5qiVmu+Y/X/SbVQ3DKBoRxyas63iW
FFIFUAYy+rRMHaHYs3LAVS+Ra0rlS455BDMRRZ5LGFF8YhRNUhOoep5Mj6kiLV9m
r2BeQEx4oTrmeP/WrGjwqAhYs/0cbWs+0AN63VwHVyVg4KqL3kXENBSr21eoVC3q
fqL58g0xAMP7Kcw5jm2KYUq2wIS6kBGpaAN+TSVy2m4cKVHnwOgQkZEKBirKC2w9
by3ryK2T14x+jw1RqRJ/egEaYOlGZH/suvHb5wIDAQABAoIBAE/Vd39s9fhg7gF9
eDTFpQ+3hpuQnzXnqpKs/oyFrXReeFkgNOPXrRC2U4WmlhQsWi+lZHsleqArfxuc
TdjQK15tO/FwEIcogOQW62Mkt5w6vKcndCL3Ossb5Lvtq26bDVq0XlU6G4BNrwr6
g1ItIL6X5qEEYYjNtKrt4BSn9ChLC2WZ+gg7jhk/h8q5240qMjJQuhD0AXE5/ILL
S6/NL4oz6Qgn1nKLLTK9nX3WdANwuNM01Xm/kGDfVIt70wsGLcL4SW3r99SJxXuL
Ar/AgZPFo8zJkpv/wxg05dU3vDKY9sdcswv8Snr+Nw4cgPIJsS7NbPxgXdsw3B5w
MQN7XtkCgYEA9/p7sbpCmpbzfS13Tz8ypKOhHgFghYpEPXEjIQajc+DYPU6LjoU6
8v2QKpHtLeK3Bbq4WF6KpyzownSttPM8DTKQ8smzvBgki78T995W6Ch6sh1pbCci
cA9jq9jTm91oOrvO3J0cGlrh7kuGFTVNWygIoTaDD9yWjuyOuy6ZWfsCgYEAzGrR
V0lpFGhZ0KfN9GtmSkKmoFJ7Hj8wElFC/Ijev/wuWAI3ptUlmUQgC7Yh7qtQyhcw
y4Y2M5aShznHhVNf7S2yBFMRkgu3T8YRNotRmSxMqXtTk88zZnK500a9PgEGmUSE
SmeFhg8fRJS9QziR9aqCXdMhjVhq3f813IjYLgUCgYEAuZlNWTE6Rm/yn98WrEZ1
xgn+PfHz5x8cggwaqc5JKiLAVepiw3HuNA4aB/KeXTiRmYUuEl34UrVnJJulo4hl
is8s87qJfp9nQvzpmhxcWXhqlMM0s1D7EnpCNE2d28u0Bjmo0y/357Xb4bm/CkKR
ukdVZivhnk6QlTpyfaCcAF8CgYEAgeOh72/TGEeSQnZN9PBFiAw/6oFegAKwngMK
qIKj36PzIrMN/7FSecgDAM4TU5+B71e7BqfaSv9zTf+V2w72VjQB+KN796wAJ+14
RItUJxsx/0NBARvY0lE16Jz7ZKmsMrmJwelzu5JiUOaeOFd6z3ov6kRc/n5fMX6Y
D/DYRK0CgYEAsCOYmCYOzXEBWb1VoAipX/FsmMonbA+TS3Y3PdBvbd+Qmz/ywPj5
kwCLkR+rUNBvve1bDjRufKLzXM6/ViKth4xnloCbqf1KBnscaAwXMUPdw+fEieMi
727z8bWIQ1+X4/dgvBM2rf7w7BvkImGYcgTZCo+5AEBSyspFwzQfZWM=
-----END RSA PRIVATE KEY----- 

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
