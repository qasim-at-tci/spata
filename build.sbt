lazy val basicSettings = Seq(
  organization := "info.fingo",
  organizationName := "FINGO sp. z o.o.",
  organizationHomepage := Some(url("http://fingo.info")),
  startYear := Some(2020),
  name := "spata",
  description := "Functional, stream based CSV processor for Scala",
  scalaVersion := "3.3.1-RC2"
)

addCommandAlias("check", "; scalafmtCheck ; scalafix --check")
addCommandAlias("mima", "; mimaReportBinaryIssues")

lazy val PerformanceTest = config("perf").extend(Test)
def perfFilter(name: String): Boolean = name.endsWith("PTS")
def unitFilter(name: String): Boolean = name.endsWith("TS") && !perfFilter(name)

lazy val root = (project in file("."))
  .enablePlugins(AutomateHeaderPlugin)
  .settings(basicSettings*)
  .settings(publishSettings*)
  .configs(PerformanceTest)
  .settings(
    licenses += ("Apache-2.0", new URI("https://www.apache.org/licenses/LICENSE-2.0.txt").toURL),
    versionScheme := Some("semver-spec"),
    headerLicenseStyle := HeaderLicenseStyle.SpdxSyntax,
    headerEmptyLine := false,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.5.0",
      "co.fs2" %% "fs2-core" % "3.7.0",
      "co.fs2" %% "fs2-io" % "3.7.0",
      "org.slf4j" % "slf4j-api" % "2.0.7",
      "org.scalatest" %% "scalatest" % "3.2.16" % Test,
      ("com.storm-enroute" %% "scalameter" % "0.21").cross(CrossVersion.for3Use2_13) % Test
        exclude("org.scala-lang.modules", "scala-xml_2.13"),
      "org.slf4j" % "slf4j-simple" % "2.0.7" % Test
    ),
    testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework"),
    inConfig(PerformanceTest)(Defaults.testTasks),
    Test / testOptions := Seq(Tests.Filter(unitFilter)),
    Test / fork := true,
    PerformanceTest / testOptions := Seq(Tests.Filter(perfFilter)),
    PerformanceTest / logBuffered := false,
    PerformanceTest / parallelExecution := false,
    javaOptions += "-Dfile.encoding=UTF-8",
    scalacOptions ++= scalacSettings,
    Compile / console / scalacOptions --= Seq("-Xfatal-warnings"),
    mimaPreviousArtifacts := Set("info.fingo" %% "spata" % "3.1.0"),
    semanticdbEnabled := false,
    autoAPIMappings := true
  )

import xerial.sbt.Sonatype.GitHubHosting
lazy val publishSettings = Seq(
  sonatypeProjectHosting := Some(GitHubHosting("fingo", "spata", "robert.marek@fingo.info")),
  publishMavenStyle := true,
  publishTo := sonatypePublishToBundle.value,
  pgpPublicRing := file("ci/public-key.asc"),
  pgpSecretRing := file("ci/secret-key.asc"),
  developers := List(Developer("susuro", "Robert Marek", "robert.marek@fingo.info", url("https://github.com/susuro")))
)

lazy val scalacSettings = Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-explain", // Explain errors in more detail.
  "-explain-types", // Explain type errors in more detail.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials,higherKinds,implicitConversions", // Enable language features.
  "-new-syntax",  // Require Scala 3 syntax
  "-pagewidth:120", // Set output page width.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Wvalue-discard", // Warn about unused expression results.
  "-Wunused:all",  // Warn about unused code
  "-Wconf:cat=deprecation:w,any:e", // Fail the compilation if there are any warnings except deprecation.
  "-Xtarget:11", // Set target JVM version.
  "-Xverify-signatures" // Verify generic signatures in generated bytecode.
)
