# PRR Labo 2
The git repository for 2017 PRR course labo 2.

# Use and start the project

## Start the RMI registry

To start the RMI registry, you need to go to the **target/classes** folder created by the `mvn clean install` command and type 

```
rmiregistry
``` 

in a terminal.

## Start the Lamport application

The Lamport application exports a bunch of RMI interfaces. You need to start the jar by typing the following command from the maven project folder: 

```bash
-jar -Djava.rmi.server.codebase=file:<absolute path to the maven project>/target/classes/ target/lamport_application.jar 
```

See the Oracle documentation on RMI : [Getting started using Java RMI](https://docs.oracle.com/javase/7/docs/technotes/guides/rmi/hello/hello-world.html)

## Start the User application

The user application only uses the RMI interfaces, so you can execute it as a regular jar archive.
