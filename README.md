dispatcherFramework
===================

scheduler/dispatcher framework for multithreaded execution of jobs, written in java, very incomplete and in working state, feel free to clone anyways


add as eclipse project
======================
1. checkout into <topfolder>/dispatcherFramework
2. set eclipse workspace to <topfolder>
3. create new java project > project name "dispatcherFramework"
4. import JUnit3 library; this is done easiest if you navigate to one of the red x'es in sourcecode and click on it
5. add all JARs in the libs folder to build path; this is done easiest by right-clicking in them in the package explorer and "add to build path"
6. for logging:
   a. add the following log4j libs to the build path:
      - log4j-core-2.0-beta9.jar
      - log4j-api-2.0-beta9.jar
   b. add the folder where a logging configuration file log4j2.xml is located to the class path; in eclipse this can be done by adding the folder as source folder
   c. you can checkout logging libs and configuration from https://github.com/mrsfoo/mylog4j.git
7. for configuration files:
   a. checkout the project https://github.com/mrsfoo/gkConfigurations.git
   b. set system property com.zwb.config.configuration_path to the path of the top level folder you checked out to; reboot

