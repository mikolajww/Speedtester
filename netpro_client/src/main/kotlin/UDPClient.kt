import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.*

class UDPClient(private val address: String,
                private val port: Int,
                private val bufferSize: Int) {
    private val socket: DatagramSocket = DatagramSocket()
    private var connected: Boolean = true
    private var buffer: ByteArray = ByteArray(bufferSize)
    private var sendFine = false

    fun sendFine() {
        sendFine = true
    }

    fun run() {
        val sizeMsg = "SIZE:$bufferSize"
        val buf = sizeMsg.toByteArray()
        val p = DatagramPacket(buf, buf.size, InetAddress.getByName(address), port)
        socket.send(p)
        while (connected) {
            try {
                if(sendFine) {
                    val fineMsg = "FINE"
                    val b = fineMsg.toByteArray()
                    socket.send(DatagramPacket(b, b.size, InetAddress.getByName(address), port))
                    connected = false
                    socket.close()
                }
                Arrays.fill(buffer, DATA_SECRET)
                val packet = DatagramPacket(buffer, buffer.size, InetAddress.getByName(address), port)
                socket.send(packet)
            }
            catch (e: Exception) {
                connected = false
                socket.close()
            }
        }
    }

}

