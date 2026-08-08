addSbtPlugin("org.scala-js"      % "sbt-scalajs"      % "1.22.0")
addSbtPlugin("org.scalameta"     % "sbt-scalafmt"     % "2.6.2")
addSbtPlugin("ch.epfl.scala"     % "sbt-scalafix"     % "0.14.7")
addSbtPlugin("rocks.earlyeffect" % "sbt-dynver-ci"    % "0.2.2")
addSbtPlugin("com.github.sbt"    % "sbt-pgp"          % "2.3.1")
addSbtPlugin("rocks.earlyeffect" % "sbt-zipx"         % "0.1.6")
addSbtPlugin("rocks.earlyeffect" % "sbt-specular"     % "0.11.0")

// sbt-sonatype has no sbt 2 artifact; sbt 2.x includes Sonatype Central support built-in via
// localStaging.value and publishTo (see build.sbt). scalajs-bundler also has no sbt 2 artifact;
// we use Vite-style bundling for examples instead, with @JSImport for Preact.
