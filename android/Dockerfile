FROM gradle:8.5-jdk17 AS build

WORKDIR /app
COPY . .
RUN gradle assembleDebug --no-daemon

FROM scratch
COPY --from=build /app/app/build/outputs/apk/debug/app-debug.apk /app-debug.apk
