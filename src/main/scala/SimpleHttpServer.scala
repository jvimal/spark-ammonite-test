package com.github.jvimal

import java.io.File
import java.net.InetAddress

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.bio.SocketConnector
import org.eclipse.jetty.server.handler.{DefaultHandler, HandlerList, ResourceHandler}
import org.eclipse.jetty.util.thread.QueuedThreadPool

class SimpleHttpServer(root: String) {
  private var server: Server = null
  private var port = 0

  start()

  def start() {
    if (server != null) {
      throw new Exception("Server is already started")
    } else {
      val (actualServer, actualPort) = doStart()
      server = actualServer
      port = actualPort
    }
  }

  /**
    * Actually start the HTTP server and return the server and port.
    */
  private def doStart(): (Server, Int) = {
    val server = new Server()

    val connector = new SocketConnector
    connector.setMaxIdleTime(60 * 1000)
    connector.setSoLingerTime(-1)
    connector.setPort(0)
    server.addConnector(connector)

    val threadPool = new QueuedThreadPool
    threadPool.setDaemon(true)
    server.setThreadPool(threadPool)
    val resHandler = new ResourceHandler
    resHandler.setResourceBase(new File(root).getAbsolutePath)

    val handlerList = new HandlerList
    handlerList.setHandlers(Array(resHandler, new DefaultHandler))

    server.setHandler(handlerList)

    server.start()
    val actualPort = server.getConnectors()(0).getLocalPort

    (server, actualPort)
  }

  def stop() {
    if (server == null) {
      throw new Exception("Server is already stopped")
    } else {
      server.stop()
      server = null
    }
  }

  /**
    * Get the URI of this HTTP server (http://host:port or https://host:port)
    */
  def uri: String = {
    if (server == null) {
      throw new Exception("Server is not started")
    } else {
      val host = InetAddress.getLocalHost().getHostName()
      s"http://${host}:$port"
    }
  }
}
