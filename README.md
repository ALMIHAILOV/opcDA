## opcDA

These are cleaned up and modified version libraries based on the work of [j-interop](https://sourceforge.net/projects/j-interop/), openscada and [skyghis](https://github.com/skyghis/j-interop-ng).

Added support for Win10 and OPC HDA.

#Quickstart

Add to POM file:
<repositories>
		<repository>
			<id>opcDA-mvn-repo</id>
			<url>https://raw.github.com/ALMIHAILOV/opcDA/mvn-repo/</url>
			<snapshots>
				<enabled>true</enabled>
				<updatePolicy>always</updatePolicy>
			</snapshots>
		</repository>
</repositories>
<dependencies>
		<dependency>
			<groupId>com.github.almihailov</groupId>
			<artifactId>opcDA</artifactId>
			<version>1.0-SNAPSHOT</version>
		</dependency>
</dependencies>


