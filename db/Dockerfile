FROM postgres:16-alpine

ENV POSTGRES_USER=app_user \
    POSTGRES_PASSWORD=app_pass \
    POSTGRES_DB=musiguessr

COPY initdb/ /docker-entrypoint-initdb.d/

RUN apk add --no-cache postgresql-client