import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.util.*

class TCPClient(private val address: String,
                private val port: Int,
                private val bufferSize: Int,
                useNagle: Boolean = false) {
    private val socket: Socket = Socket(address, port).apply { tcpNoDelay = useNagle }
    private val reader: InputStream = socket.getInputStream()
    private val writer: OutputStream = socket.getOutputStream()
    private var connected: Boolean = true
    private var buffer: ByteArray = ByteArray(bufferSize)
    private var sendFine = false

    fun sendFine() {
        sendFine = true
    }

    fun run() {
        write("SIZE:$bufferSize")
        while (connected) {
            if(sendFine) {
                write("FINE")
                connected = false
                socket.close()
            }
            try {
                Arrays.fill(buffer, DATA_SECRET)
                writer.write(buffer)
            }
            catch (e: Exception) {
                connected = false
                socket.close()
            }
        }
    }

    private fun write(msg: String) {
        if(msg.isNotBlank()) {
            writer.writeBytes(msg)
        }
    }
}

