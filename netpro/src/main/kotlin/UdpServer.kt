import java.net.DatagramPacket
import java.net.DatagramSocket
import kotlin.concurrent.thread

class UdpServer(port: Int) {
    private val serverSocket: DatagramSocket = DatagramSocket(port)
    private lateinit var buffer: ByteArray
    private var firstMessage: Boolean = true
    val UDP_HEADER = "$ANSI_PURPLE(UDP)$ANSI_RESET"
    var running = true

    fun run() {
        handle()
    }

    fun handle() {
        var size = -1
        var lastPrintTime: Long = 0
        while (running) {
            if(firstMessage) {
                size = getBufferSize() ?: -1
                if(size > 0) {
                    println("$UDP_HEADER Allocating buffer of size ${size.formatToKb()} Kbytes")
                    firstMessage = false
                    buffer = ByteArray(size)
                    continue
                }
            }
            val receivePacket = DatagramPacket(buffer, buffer.size)
            val start = System.nanoTime()
            serverSocket.receive(receivePacket)
            val timeInMs = (System.nanoTime() - start) / 1_000_000.0
            if(String(buffer, 0, buffer.size).contains("FINE", ignoreCase = true)) {
                close(receivePacket.address.toString() + ":" + receivePacket.port.toString())
            }
            if(System.currentTimeMillis() - lastPrintTime >= 2_000L) {
                val errorCount = getErrorPercent(buffer, DATA_SECRET) * 100
                println("$UDP_HEADER Received $ANSI_PURPLE${receivePacket.length.formatToKb()}$ANSI_RESET Kbytes in $ANSI_PURPLE$timeInMs$ANSI_RESET ms, speed $ANSI_PURPLE${(receivePacket.length / timeInMs).format(2)}$ANSI_RESET Kb/s | Error = $errorCount%")
                lastPrintTime = System.currentTimeMillis()
            }
        }
    }

    private fun close(clientAddr: String) {
        println("$UDP_HEADER Client $clientAddr disconnected.")
        running = false
        firstMessage = true
        serverSocket.close()
    }

    private fun getErrorPercent(buffer: ByteArray, value:Byte): Double {
        var errorCount = 0L
        for (b in buffer) {
            if(b != value) {
                errorCount++
            }
        }
        return errorCount/(buffer.size * 1.0)
    }

    private fun getBufferSize(): Int? {
        val msgSizeBuffer = ByteArray(1024)
        val receivePacket = DatagramPacket(msgSizeBuffer, msgSizeBuffer.size);
        serverSocket.receive(receivePacket)
        val sizeMsg = String(msgSizeBuffer, 0, receivePacket.length)
        val tokens = sizeMsg.split(":").toTypedArray()
        return tokens[1].toIntOrNull()
    }
}

