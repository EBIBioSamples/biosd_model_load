#The BioSD Relational Database Loader

This is the Maven project that builds the command line loader for the object-model version of BioSD. 

You can build the command line by issuing a command like (see package.sh):

```export MVN_BUILD_OPTS='-Ptest.h2,h2_file'; ./package.sh```

You'll find the command line binary in a zip file in target/ 
test.* profiles are only used for JUnit tests. Profiles without such prefix are used to ship the zip file with an 
hibernate.properties file pointing at the database defined by the profile. You can see details about such profiles in the [common POM](https://github.com/EBIBioSamples/biosd_common) we use for BioSD-related projects.

See the [wiki](http://github.com/EBIBioSamples/biosd_model_load/wiki) for further details.
 
