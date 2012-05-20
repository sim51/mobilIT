import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

  val appName = "mobilIT"
  val appVersion = "1.0-SNAPSHOT"

  val cloudbees = "Cloudbees public snapshot" at "https://repository-andy-petrella.forge.cloudbees.com/snapshot"
  val neo4jPlayPlugin = "be.nextlab" %% "neo4j-rest-play-plugin" % "0.0.1-SNAPSHOT" changing()

  // Only compile the bootstrap bootstrap.less file and any other *.less file in the stylesheets directory
  def customLessEntryPoints(base: File): PathFinder = (
    (base / "app" / "assets" / "stylesheets" / "bootstrap" * "bootstrap.less") +++
      (base / "app" / "assets" / "stylesheets" * "*.less")
    )

  val appDependencies = Seq(
    // Add your project dependencies here,
    neo4jPlayPlugin
  )

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
    lessEntryPoints <<= baseDirectory(customLessEntryPoints),
    resolvers ++= Seq(cloudbees)
  )

}
