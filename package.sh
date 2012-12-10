mvn clean package -DargLine="-Xms512m -Xmx2G -XX:PermSize=128m -XX:MaxPermSize=1G" \
    -Dsurefire.useFile=true \
    -Ptest.hsql,hsql_file # see pom.xml for other profiles

