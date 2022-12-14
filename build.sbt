import Dependencies._
import sbtrelease.Git
import sbtrelease.ReleaseStateTransformations._
import sbtrelease.Utilities.stateW

import java.awt.Desktop
import java.time.Instant
import scala.sys.process._
import scala.util.Try

val ReleaseBranch = "dev"
val ProductionBranch = "main"

inThisBuild {
  Seq(
    organization := "com.ruchij",
    scalaVersion := Dependencies.ScalaVersion,
    maintainer := "me@ruchij.com",
    scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xmax-inlines", "50"),
  )
}

lazy val shared =
  (project in file("./shared"))

lazy val api =
  (project in file("./api"))
    .enablePlugins(BuildInfoPlugin, JavaAppPackaging)
    .settings(
      name := "uptime-monitor-api",
      buildInfoKeys := Seq[BuildInfoKey](
        name, organization,
        version,
        scalaVersion,
        sbtVersion,
        buildTimestamp,
        gitBranch,
        gitCommit
      ),
      buildInfoPackage := "com.eed3si9n.ruchij",
      topLevelDirectory := None,
      Universal / javaOptions ++= Seq("-Dlogback.configurationFile=/opt/data/logback.xml"),
      libraryDependencies ++= Seq(
        http4sDsl,
        http4sEmberServer,
        http4sEmberClient,
        http4sCirce,
        circeGeneric,
        circeParser,
        circeLiteral,
        doobie,
        doobieHikari,
        postgresql,
        h2,
        redis4CatsEffects,
        jbcrypt,
        jodaTime,
        embeddedRedis,
        testContainers,
        postgresqlTestContainer,
        pureconfig,
        logbackClassic,
        scalaLogging
      ),
      libraryDependencies ++= Seq(scalaTest, scalaTestPlusMockito, pegdown).map(_ % Test)
    )
    .dependsOn(migration)

lazy val batch =
  (project in file("./batch"))
    .enablePlugins(JavaAppPackaging)
    .settings(
      name := "uptime-monitor-batch",
      topLevelDirectory := None
    )

lazy val worker =
  (project in file("./worker"))
    .enablePlugins(JavaAppPackaging)
    .settings(
      name := "uptime-monitor-worker",
      topLevelDirectory := None
    )

lazy val migration =
  (project in file("./migration"))
    .enablePlugins(JavaAppPackaging)
    .settings(
      name := "uptime-monitor-migration",
      topLevelDirectory := None,
      libraryDependencies ++= Seq(catsEffect, pureconfig, flywayCore, postgresql, h2, logbackClassic, scalaLogging),
      libraryDependencies ++= Seq(scalaTest).map(_ % Test)
    )

lazy val applicationStack =
  (project in file("./application-stack"))
    .settings(
      name := "application-stack"
    )
    .dependsOn(migration, api)

val verifyReleaseBranch = { state: State =>
  val git = Git.mkVcs(state.extract.get(baseDirectory))
  val branch = git.currentBranch

  if (branch != ReleaseBranch) {
    sys.error {
      s"The release branch is $ReleaseBranch, but the current branch is set to $branch"
    }
  } else state
}

val mergeReleaseToMaster = { state: State =>
  val git = Git.mkVcs(state.extract.get(baseDirectory))

  val (updatedState, releaseTag) = state.extract.runTask(releaseTagName, state)

  updatedState.log.info(s"Merging $releaseTag to $ProductionBranch...")

  val userInput: Option[ProcessBuilder] =
    SimpleReader.readLine("Push changes to the remote master branch (y/n)? [y]")
      .map(_.toLowerCase) match {
      case Some("y") | Some("")  =>
        updatedState.log.info(s"Pushing changes to remote master ($releaseTag)...")
        Some(git.cmd("push"))

      case _ =>
        updatedState.log.warn("Remember to push changes to remote master")
        None
    }

  val actions: List[ProcessBuilder] =
    List(git.cmd("checkout", ProductionBranch), git.cmd("pull", "--rebase"), git.cmd("merge", releaseTag)) ++
      userInput ++
      List(git.cmd("checkout", ReleaseBranch))

  actions.reduce(_ #&& _) !!

  updatedState.log.info(s"Successfully merged $releaseTag to $ProductionBranch")

  updatedState
}
releaseProcess := Seq(
  ReleaseStep(verifyReleaseBranch),
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  ReleaseStep(mergeReleaseToMaster),
  setNextVersion,
  commitNextVersion,
  pushChanges
)

lazy val buildTimestamp = BuildInfoKey.action("buildTimestamp") { Instant.now() }
lazy val gitBranch = BuildInfoKey.action("gitBranch") { runGitCommand("git rev-parse --abbrev-ref HEAD") }
lazy val gitCommit =  BuildInfoKey.action("gitCommit") { runGitCommand("git rev-parse --short HEAD") }

def runGitCommand(command: String): Option[String] = {
  val gitFolder = new File(".git")

  if (gitFolder.exists()) Try(command !!).toOption.map(_.trim).filter(_.nonEmpty) else None
}

addCommandAlias("cleanCompile", "clean; compile;")
addCommandAlias("cleanTest", "clean; test;")