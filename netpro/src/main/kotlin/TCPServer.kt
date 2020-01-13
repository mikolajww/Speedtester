import java.io.*
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread




class TCPServer(port: Int) {
    private val server: ServerSocket = ServerSocket(port)
    val TCP_HEADER = "$ANSI_GREEN(TCP)$ANSI_RESET"
    var isHandling: Boolean = false

    fun run() {
        while (true) {
            with(server.accept()) {
                if (!isHandling) {
                    thread { ClientHandler(this, this@TCPServer).handle() }
                } else {
                    thread { BusyHandler(this).handle() }
                }
            }
        }
    }

    inner class ClientHandler(private val client: Socket, private val parentServer: TCPServer) {
        private val reader: InputStream = client.getInputStream()
        private val writer: OutputStream = client.getOutputStream()
        private var running: Boolean = true
        private lateinit var buffer: ByteArray

        fun handle() {
            parentServer.isHandling = true
            //println("$TCP_HEADER Client ${client.inetAddress.hostAddress} connected.")
            val bufferSize = getBufferSize()
            println("$TCP_HEADER Allocating buffer of size ${bufferSize.formatToKb()} Kbytes")
            buffer = ByteArray(bufferSize)
            var totalBytes: Long = 0
            var totalTime: Long = 0
            var lastPrintTime: Long = 0
            while(running) {
                try {
                    val start = System.nanoTime()
                    val bytesRead = read(buffer)
                    val timeInMs = (System.nanoTime() - start) / 1_000_000.0
                    if(bytesRead <= 0) {
                        close()
                    }
                    if(String(buffer, 0, bufferSize).contains("FINE", ignoreCase = true)) {
                        close()
                    }
                    if(System.currentTimeMillis() - lastPrintTime >= 2_000L) {

                        println("$TCP_HEADER Received $ANSI_GREEN${bytesRead.formatToKb()}$ANSI_RESET Kbytes in $ANSI_GREEN$timeInMs$ANSI_RESET ms, speed $ANSI_GREEN${(bytesRead / timeInMs).format(2)}$ANSI_RESET  Kb/s")
                        lastPrintTime = System.currentTimeMillis()
                    }
                    buffer.fill(0)
                }
                catch (e: Exception) {
                    close()
                }
            }
        }

        private fun read(buffer: ByteArray): Int {
            var total = 0
            var count = 0
            while (reader.read(buffer).also { count = it } > 0 && total < buffer.size) {
                total += count
                //println("Read a chunk of $count bytes")
            }
            return total
        }

        private fun getBufferSize(): Int {
            val msgSizeBuffer = ByteArray(1024)
            val read: Int = reader.read(msgSizeBuffer)
            val sizeMsg = String(msgSizeBuffer, 0, read)
            val tokens = sizeMsg.split(":").toTypedArray()
            return tokens[1].toInt()
        }

        private fun close() {
            println("$TCP_HEADER Client ${client.inetAddress.hostAddress} disconnected.")
            running = false
            parentServer.isHandling = false
            client.close()
        }
    }

    inner class BusyHandler(private val client: Socket) {
        private val reader: BufferedReader = BufferedReader(InputStreamReader(client.getInputStream()))
        private val writer: OutputStream = client.getOutputStream()

        fun handle() {
            writer.writeBytes("BUSY\n")
            client.close()
        }

    }
}
