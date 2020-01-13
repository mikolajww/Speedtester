import java.io.OutputStream
import java.nio.charset.Charset
import kotlin.concurrent.thread

const val DATA_SECRET: Byte = 42

fun OutputStream.writeBytes(msg: String) {
    val data = msg.toByteArray(Charset.defaultCharset())
    this.write(data)
}

lateinit var udpClient: UDPClient
lateinit var tcpClient: TCPClient

fun main(args: Array<String>) {
    println("Input port number of the server")
    val port = readLine()!!.toInt()
    println("Connecting to 127.0.0.1:$port")
    println("Input buffer size for the communication (in Kbytes)")
    var bufferSize: Int?
    do {
        bufferSize = readLine()!!.toIntOrNull()
    } while (bufferSize == null)
    println("Do you want to use Nagle's algorighm? y/n")
    val useNagle = readLine()!!.toCharArray()[0].equals('y', ignoreCase = true)
    println("${if(useNagle) "U" else "Not u"}sing Nagle's algorithm")
    println("While running, type FINE:tcp or FINE:udp to stop tcp or udp thread respectively, or FINE to stop all threads")
    thread { TCPClient("localhost", port, bufferSize * 1000, useNagle = useNagle)
            .also { tcpClient = it }
            .run()
     }
    thread { UDPClient("localhost", port, bufferSize * 1000)
            .also { udpClient = it }
            .run()
    }
    serverLoop@ while(true) {
        when(readLine()) {
            "FINE" -> {
                tcpClient.sendFine()
                udpClient.sendFine()
            }
            "FINE:tcp" -> tcpClient.sendFine()
            "FINE:udp" -> udpClient.sendFine()
            "CLOSE" -> {
                println("Shutting down...")
                tcpClient.sendFine()
                udpClient.sendFine()
                break@serverLoop
            }
        }

    }
}
