# Daker-server CI/CD

## Branch Strategy
- `main`: production deployment branch
- `feature/*`: work branches

## CI
- Trigger: pull request to `main`
- Trigger: push to `main`
- Workflow: `.github/workflows/ci.yml`
- Action:
  - Set up JDK 21
  - Cache Gradle dependencies
  - Run `./gradlew test`

## CD
- Trigger: push to `main`
- Workflow: `.github/workflows/deploy-main.yml`
- Deployment target: production server
- Method: GitHub Actions SSH deploy

## Required GitHub Secrets
- `EC2_HOST`
- `EC2_USER`
- `EC2_PORT`
- `EC2_SSH_KEY`
- `SERVER_APP_DIR`
- `SERVER_BUILD_COMMAND`
- `SERVER_STOP_COMMAND`
- `SERVER_START_COMMAND`
- `SERVER_HEALTHCHECK_URL`

## Example Secret Values
```text
SERVER_APP_DIR=/home/ubuntu/Daker-server
SERVER_BUILD_COMMAND=docker compose up -d && chmod +x gradlew && ./gradlew clean build -x test
SERVER_STOP_COMMAND=pkill -f 'java -jar' || true
SERVER_START_COMMAND=nohup java -jar build/libs/*.jar > app.log 2>&1 &
SERVER_HEALTHCHECK_URL=http://15.164.250.113:8080/actuator/health
```

## Notes
- `bin/` is a local build artifact and should not be committed.
- `application-local.yml` and `application-prod.yml` should stay out of Git.
- If the server uses `systemd` or a custom process manager, replace the stop/start commands with the actual production commands.
- If there is no health endpoint yet, use a lightweight reachable URL or leave `SERVER_HEALTHCHECK_URL` unset.
