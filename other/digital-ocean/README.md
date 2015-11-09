Phen @ trantor, IGR
=====================

Installation procedure
----------------------

```
# In phen dir
$ mvn clean package

# from within IGR network
$ scp target/igr-0.2.0-SNAPSHOT.war hannac@10.10.10.4:~

# or from outside of IGr network
$ scp target/igr-0.2.0-SNAPSHOT.war hannac@cropnet.pl:~
$ ssh hannac@cropnet.pl:~
$ scp igr-0.2.0-SNAPSHOT.war hannac@10.10.10.4:~

$ ssh hannac@10.10.10.4:~
$ mv igr-0.2.0-SNAPSHOT.war igr.war
$ chown tomcat:tomcat igr.war
$ service tomcat7 stop
$ rm -rf /usr/share/apache-tomcat-7.0.65/webapps/igr*
$ mv igr.war /usr/share/apache-tomcat-7.0.65/webapps/
$ service tomcat7 start
```
   
Tomcat 7
--------
Dir: /usr/share/apache-tomcat7.0.65



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

