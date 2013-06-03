
name := "bartera"

version := "0.1.0-SNAPSHOT"

libraryDependencies ++= Seq(
	"org.squeryl" %% "squeryl" % "0.9.5-6",
	"com.h2database" % "h2" % "1.3.165",
	"mysql" % "mysql-connector-java" % "5.1.18",
	"postgresql" % "postgresql" % "9.1-901.jdbc4",
	"com.restfb" % "restfb" % "1.6.11",
	"commons-codec" % "commons-codec" % "1.7",
	"org.clapper" %% "grizzled-slf4j" % "1.0.1"
)
