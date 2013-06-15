
name := "bartera"

version := "0.1.0-SNAPSHOT"

libraryDependencies ++= Seq(
	"org.squeryl" %% "squeryl" % "0.9.5-6",
	"com.h2database" % "h2" % "1.3.165",
//	"mysql" % "mysql-connector-java" % "5.1.18",
	"postgresql" % "postgresql" % "9.1-901.jdbc4",
//	"be.objectify" %% "deadbolt-scala" % "2.0-SNAPSHOT",
	"com.restfb" % "restfb" % "1.6.11",
	"commons-codec" % "commons-codec" % "1.7",
//	"org.ccil.cowan.tagsoup" % "tagsoup" % "1.2.1",
//	"net.sourceforge.nekohtml" % "nekohtml" % "1.9.18",
	"org.jsoup" % "jsoup" % "1.7.2",
	"de.sven-jacobs" % "loremipsum" % "1.0",
	"org.clapper" %% "grizzled-slf4j" % "1.0.1"
)
