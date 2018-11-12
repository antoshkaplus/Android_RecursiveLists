
To change API version, change in the following places:
- backend/build.gradle
- backend/src/main/webapp/js/init.js
- backend/src/main/java/com/antoshkaplus/recursivelists/backend/ItemsEndpoint.java

From the main folder run:
    gradle appengineDeploy
It may ask you to log in into gcloud

How to run dev servel locally is unclear, probably appengineRun
More information can be found here:
    https://cloud.google.com/appengine/docs/standard/java/tools/migrate-gradle