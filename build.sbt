import org.scalajs.linker.interface.ModuleSplitStyle

ThisBuild / scalaVersion := "3.7.0"
ThisBuild / organization := "com.burdinov"

lazy val root = project
  .in(file("."))
  .aggregate(tacTicToeJVM, tacTicToeJS)
  .settings(
    name := "tac-tic-toe"
  )

lazy val tacTicToe = crossProject(JSPlatform, JVMPlatform)
  .in(file("."))
  .settings(
    name := "tac-tic-toe",
    scalacOptions ++= Seq("-encoding", "utf-8", "-deprecation", "-feature"),
    libraryDependencies ++= Seq(
      "org.scalameta" %%% "munit" % "1.0.4" % Test
    )
  )
  .jvmSettings(
    // JVM-specific settings
    libraryDependencies ++= Seq(
      // Add JVM dependencies here
    ),
    testFrameworks += new TestFramework("munit.Framework")
  )
  .jsSettings(
    // Scala.js-specific settings
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    scalaJSLinkerConfig ~= { _.withModuleSplitStyle(ModuleSplitStyle.SmallModulesFor(List("com.burdinov.tactictoe"))) },
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "2.8.0"
    )
  )

lazy val tacTicToeJVM = tacTicToe.jvm
lazy val tacTicToeJS = tacTicToe.js 