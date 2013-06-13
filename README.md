#The BioSD Relational Database Loader

This is the Maven project that builds the command line loader for the object-model version of BioSD. 

You can build the command line by issuing a command like (see package.sh):

```mvn -DargLine="-Xms2G -Xmx4G -XX:PermSize=128m -XX:MaxPermSize=256m" -Ptest.hsql,hsql_file clean package```

You'll find the command line binary in a zip file in target/ 
test.* profiles are only used for JUnit tests. Profiles without such prefix are used to ship the zip file with an 
hibernate.properties file pointing at the database defined by the profile.

Currently available profiles for the EBI are oracle_test and oracle_dev.

See the [wiki](http://github.com/EBIBioSamples/biosd_model_load/wiki) for details.
 
