#!/usr/bin/env bash
../liquibase/liquibase-3.6.2/liquibase \
    --driver=com.mysql.cj.jdbc.Driver \
    --classpath=.. \
    --changeLogFile=db/liquibase/changelog-master.xml \
    --url="jdbc:mysql://localhost:3306/bio_integration?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Europe/Moscow&allowPublicKeyRetrieval=true" \
    --username=root \
    --password=1234567890 \
    update