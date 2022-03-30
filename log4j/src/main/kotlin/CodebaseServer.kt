import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import java.io.ByteArrayOutputStream
import java.net.InetSocketAddress

class HttpFileHandler : HttpHandler {
    @Throws(Exception::class)
    override fun handle(httpExchange: HttpExchange) {
        println("new http request from " + httpExchange.remoteAddress + " " + httpExchange.requestURI)
        val uri = httpExchange.requestURI.path
        val inputStream = HttpFileHandler::class.java.getResourceAsStream(uri)
        val byteArrayOutputStream = ByteArrayOutputStream()
        if (inputStream == null) {
            println("Not Found")
        } else {
            while (inputStream.available() > 0) {
                byteArrayOutputStream.write(inputStream.read())
            }
            val bytes = byteArrayOutputStream.toByteArray()
            httpExchange.sendResponseHeaders(200, bytes.size.toLong())
            httpExchange.responseBody.write(bytes)
        }
        httpExchange.close()
    }
}

object CodebaseServer {

    @Throws(Exception::class)
    fun launchCodebaseURLServer(ip: String, port: Int) {
        println("Starting HTTP server");
        val httpServer = HttpServer.create(InetSocketAddress(ip, port), 0)
        httpServer.createContext("/", HttpFileHandler())
        httpServer.executor = null;
        httpServer.start();
    }
}
