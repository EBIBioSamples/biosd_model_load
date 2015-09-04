mvn clean package $MVN_BUILD_OPTS -DargLine="-Xms512m -Xmx2G -XX:PermSize=128m -XX:MaxPermSize=1G" \
    -Dsurefire.useFile=true


