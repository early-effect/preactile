/* Vendored from scala-js/scala-js-env-jsdom-nodejs v1.1.1 (BSD-3-Clause, (c) LAMP/EPFL).
 *
 * Why this lives in the meta-build: `scalajs-env-jsdom-nodejs` publishes only Scala
 * 2.11/2.12/2.13 artifacts, but sbt 2.x compiles the meta-build on Scala 3, so there is no
 * `_3` artifact to depend on. The env is a single file against the stable `org.scalajs.jsenv`
 * API, which sbt-scalajs already provides on the meta-build classpath (scalajs-js-envs +
 * scalajs-env-nodejs, the latter supplying jimfs). build.sbt references it by its original
 * fully-qualified name, so keep the package and class names as published upstream.
 */
package org.scalajs.jsenv.jsdomnodejs

import scala.util.control.NonFatal

import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, StandardCopyOption}
import java.net.URI

import com.google.common.jimfs.Jimfs

import org.scalajs.jsenv.*
import org.scalajs.jsenv.nodejs.*
import org.scalajs.jsenv.JSUtils.escapeJS

class JSDOMNodeJSEnv(config: JSDOMNodeJSEnv.Config) extends JSEnv:

  def this() = this(JSDOMNodeJSEnv.Config())

  val name: String = "Node.js with JSDOM"

  def start(input: Seq[Input], runConfig: RunConfig): JSRun =
    JSDOMNodeJSEnv.validator.validate(runConfig)
    val scripts = validateInput(input)
    try internalStart(codeWithJSDOMContext(scripts), runConfig)
    catch
      case NonFatal(t) =>
        JSRun.failed(t)

  def startWithCom(input: Seq[Input], runConfig: RunConfig, onMessage: String => Unit): JSComRun =
    JSDOMNodeJSEnv.validator.validate(runConfig)
    val scripts = validateInput(input)
    ComRun.start(runConfig, onMessage) { comLoader =>
      internalStart(comLoader :: codeWithJSDOMContext(scripts), runConfig)
    }

  private def validateInput(input: Seq[Input]): List[Path] =
    input.map {
      case Input.Script(script) =>
        script

      case Input.ESModule(module) =>
        // ES modules are supported by loading via dynamic import in Node.js
        module

      case Input.CommonJSModule(module) =>
        // CommonJS modules work fine with vm.runInThisContext
        module

      case _ =>
        throw new UnsupportedInputException(input)
    }.toList

  private def internalStart(files: List[Path], runConfig: RunConfig): JSRun =
    val command = config.executable :: config.args
    val externalConfig = ExternalJSRun
      .Config()
      .withEnv(env)
      .withRunConfig(runConfig)
    ExternalJSRun.start(command, externalConfig)(JSDOMNodeJSEnv.write(files))

  private def env: Map[String, String] =
    Map("NODE_MODULE_CONTEXTS" -> "0") ++ config.env

  private def codeWithJSDOMContext(scripts: List[Path]): List[Path] =
    val scriptsURIs = scripts.map(JSDOMNodeJSEnv.materialize(_))
    val scriptsURIsAsJSStrings =
      scriptsURIs.map(uri => "\"" + escapeJS(uri.toASCIIString) + "\"")
    val scriptsURIsJSArray = scriptsURIsAsJSStrings.mkString("[", ", ", "]")
    val jsDOMCode =
      s"""
         |(function () {
         |  var jsdom = require("jsdom");
         |
         |  var virtualConsole = new jsdom.VirtualConsole();
         |  if (typeof virtualConsole.forwardTo === 'function')
         |    virtualConsole.forwardTo(console, { omitJSDOMErrors: true })
         |  else
         |    virtualConsole.sendTo(console, { omitJSDOMErrors: true });
         |  virtualConsole.on("jsdomError", function (error) {
         |    /* #42 Counter-hack the hack that React's development mode uses
         |     * to bypass browsers' debugging tools. If we detect that we are
         |     * called from that hack, we do nothing.
         |     */
         |    var isWithinReactsInvokeGuardedCallbackDevHack_issue42 =
         |      new Error("").stack.indexOf("invokeGuardedCallbackDev") >= 0;
         |    if (isWithinReactsInvokeGuardedCallbackDevHack_issue42)
         |      return;
         |
         |    try {
         |      // Display as much info about the error as possible
         |      if (error.detail && error.detail.stack) {
         |        console.error("" + error.detail);
         |        console.error(error.detail.stack);
         |      } else {
         |        console.error(error);
         |      }
         |    } finally {
         |      // Whatever happens, kill the process so that the run fails
         |      process.exit(1);
         |    }
         |  });
         |
         |  var dom = new jsdom.JSDOM("", {
         |    virtualConsole: virtualConsole,
         |    url: "http://localhost/",
         |
         |    /* Allow unrestricted <script> tags. This is exactly as
         |     * "dangerous" as the arbitrary execution of script files we
         |     * do in the non-jsdom Node.js env.
         |     */
         |    resources: "usable",
         |    runScripts: "dangerously"
         |  });
         |
         |  var window = dom.window;
         |  window["scalajsCom"] = global.scalajsCom;
         |
         |  // Expose DOM globals on the global scope so Scala.js DOM bindings can find them.
         |  global.document = window.document;
         |  global.window = window;
         |  global.navigator = window.navigator;
         |  global.location = window.location;
         |  global.history = window.history;
         |  global.screen = window.screen;
         |  global.HTMLElement = window.HTMLElement;
         |  global.Element = window.Element;
         |  global.Node = window.Node;
         |
         |  // Load scripts using vm.runInThisContext (compatible with how write() executes this wrapper).
         |  var fs = require("fs");
         |  var vm = require("vm");
         |  var path = require("path");
         |  var url = require("url");
         |  var scriptsSrcs = $scriptsURIsJSArray;
         |  try {
         |    for (var i = 0; i < scriptsSrcs.length; i++) {
         |      var src = scriptsSrcs[i];
         |      // file:// URLs need to be converted to paths for fs.readFileSync
         |      if (src.startsWith("file:")) {
         |        src = url.fileURLToPath(src);
         |      }
         |      vm.runInThisContext(fs.readFileSync(src, "utf-8"), { filename: src, displayErrors: true });
         |    }
         |  } catch (e) {
         |    console.error("Failed to load script:", e);
         |    process.exit(1);
         |  }
         |})();
         |""".stripMargin
    List(
      Files.write(Jimfs.newFileSystem().getPath("codeWithJSDOMContext.js"), jsDOMCode.getBytes(StandardCharsets.UTF_8))
    )
  end codeWithJSDOMContext
end JSDOMNodeJSEnv

object JSDOMNodeJSEnv:
  private lazy val validator = ExternalJSRun.supports(RunConfig.Validator())

  // Copied from NodeJSEnv.scala upstream. Uses vm.runInThisContext which doesn't support dynamic import(),
  // but Scala.js outputs regular JS that works fine with this approach.
  private def write(files: List[Path])(out: OutputStream): Unit =
    val p = new PrintStream(out, false, "UTF8")
    try
      def writeRunScript(path: Path): Unit =
        try
          val f      = path.toFile
          val pathJS = "\"" + escapeJS(f.getAbsolutePath) + "\""
          p.println(s"""
            require('vm').runInThisContext(
              require('fs').readFileSync($pathJS, { encoding: "utf-8" }),
              { filename: $pathJS, displayErrors: true }
            );
          """)
        catch
          case _: UnsupportedOperationException =>
            val code   = new String(Files.readAllBytes(path), StandardCharsets.UTF_8)
            val codeJS = "\"" + escapeJS(code) + "\""
            val pathJS = "\"" + escapeJS(path.toString) + "\""
            p.println(s"""
              require('vm').runInThisContext(
                $codeJS,
                { filename: $pathJS, displayErrors: true }
              );
            """)

      for file <- files do writeRunScript(file)
    finally p.close()
    end try
  end write

  // tmpSuffixRE and tmpFile copied from HTMLRunnerBuilder.scala in Scala.js

  private val tmpSuffixRE = """[a-zA-Z0-9-_.]*$""".r

  private def tmpFile(path: String, in: InputStream): URI =
    try
      /* - createTempFile requires a prefix of at least 3 chars
       * - we use a safe part of the path as suffix so the extension stays (some
       *   browsers need that) and there is a clue which file it came from.
       */
      val suffix = tmpSuffixRE.findFirstIn(path).orNull

      val f = File.createTempFile("tmp-", suffix)
      f.deleteOnExit()
      Files.copy(in, f.toPath(), StandardCopyOption.REPLACE_EXISTING)
      f.toURI()
    finally in.close()

  private def materialize(path: Path): URI =
    try path.toFile.toURI
    catch
      case _: UnsupportedOperationException =>
        tmpFile(path.toString, Files.newInputStream(path))

  final class Config private (
      val executable: String,
      val args: List[String],
      val env: Map[String, String],
  ):
    private def this() =
      this(
        executable = "node",
        args = Nil,
        env = Map.empty,
      )

    def withExecutable(executable: String): Config =
      copy(executable = executable)

    def withArgs(args: List[String]): Config =
      copy(args = args)

    def withEnv(env: Map[String, String]): Config =
      copy(env = env)

    private def copy(
        executable: String = executable,
        args: List[String] = args,
        env: Map[String, String] = env,
    ): Config =
      new Config(executable, args, env)
  end Config

  object Config:
    /** Returns a default configuration for a [[JSDOMNodeJSEnv]].
      *
      * The defaults are:
      *
      *   - `executable`: `"node"`
      *   - `args`: `Nil`
      *   - `env`: `Map.empty`
      */
    def apply(): Config = new Config()
  end Config
end JSDOMNodeJSEnv
