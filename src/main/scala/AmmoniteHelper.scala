package com.github.jvimal

import java.io.File

import ammonite.repl.Storage
import com.google.common.io.Files
import org.apache.spark.{SparkConf, SparkContext}

object AmmoniteHelper {
  var sc: SparkContext = null
  var server: SimpleHttpServer = null
  var result: String = null
  var tmpDir: File = Files.createTempDir

  def createAmmoniteRepl(tmpDir: File, sparkContextGetter: String) = {
    // NOTE(jvimal): The following still don't work.  I will try to get them
    // working in the future.

    // def f(x: Int) = x * 10
    // rdd.map(f).collect => Doesn't work.  Can't find f() for some reason.

    // case class A(x: Int)
    // rdd.map(new A(_)).collect => Can't find class A for some reason.

    // But this is sufficient to get started and have something working.
    ammonite.repl.Main(
      predef =
        s"""
          | val sc = $sparkContextGetter
          | val rdd = sc.parallelize(1 to 5).map(x => x * 10)
        """.stripMargin,
      storageBackend = Storage.Folder(ammonite.ops.Path(System.getProperty("user.dir") + "/.ammonite")),
      classOutputDir = Some(tmpDir.toPath.toString)
    ).runCode("println(rdd.collect.toSeq.toString)")
  }

  def main(args: Array[String]) {
    tmpDir.deleteOnExit()
    server = new SimpleHttpServer(tmpDir.getAbsolutePath)
    val conf: SparkConf = new SparkConf()
      .setMaster("local")
      .setAppName("test")
      .set("spark.repl.class.uri", server.uri)
    sc = new SparkContext(conf)
    sc.setLogLevel("WARN")
    System.out.println("Storing ammonite classes at " + tmpDir.toString)

    createAmmoniteRepl(tmpDir, "com.github.jvimal.AmmoniteHelper.sc")
    sc.stop()
    server.stop()
  }
}
