Phen Ansible Playbook
=====================

Installation procedure
----------------------

```
# In phen dir
$ mvn clean package
$ scp target/igr-0.2.0-SNAPSHOT.war root@188.166.67.119:~
$ ssh root@188.166.67.119
$ mv igr-0.2.0-SNAPSHOT.war igr.war
$ chown tomcat7:tomcat7 igr.war
$ service tomcat7 stop
$ rm -rf /var/lib/tomcat7/webapps/igr*
$ mv igr.war /var/lib/tomcat7/webapps/
$ service tomcat7 start
```
    


Tomcat 7
--------

Dir: /var/lib/tomcat7

