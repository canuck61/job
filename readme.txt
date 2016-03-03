Git repository is located at https://github.com/canuck61/job.git

No Continuous integration was done.

Maven will produce a jar file in the target directory

To produce the jar file conatining the Calculator executable:

mvn package

This will produce an jar file in the target directory called:

calculator.jar

To run the Calculator change into the same directory the jar resides in and run:

To see the help enter:

java -jar calculator.jar -h


java -jar calculator.jar -debug "<expression>"


Examples:

java -jar calculator.jar "add(2,4)"


java -jar calculator.jar -debug "sub(4,3)"

The logfile will be produced in the directory you invoke the executable from.
