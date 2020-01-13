import java.io.OutputStream
import java.nio.charset.Charset
import kotlin.concurrent.thread

const val ANSI_RESET = "\u001B[0m"
const val ANSI_BLACK = "\u001B[30m"
const val ANSI_RED = "\u001B[31m"
const val ANSI_GREEN = "\u001B[32m"
const val ANSI_YELLOW = "\u001B[33m"
const val ANSI_BLUE = "\u001B[34m"
const val ANSI_PURPLE = "\u001B[35m"
const val ANSI_CYAN = "\u001B[36m"
const val ANSI_WHITE = "\u001B[37m"

const val DATA_SECRET:Byte = 42
fun Int.formatToKb(): Double { return this / 1000.0 }
fun Double.format(digits: Int): String { return java.lang.String.format("%.${digits}f", this)}

fun OutputStream.writeBytes(msg: String) {
    val data = msg.toByteArray(Charset.defaultCharset())
    this.write(data)
}

fun main(args: Array<String>) {
    println("Input port for TCP and UDP threads")
    val port = readLine()!!.toInt()
    println("Starting server on 127.0.0.1:$port")
    thread { TCPServer(port).run() }
    thread { UdpServer(port).run() }
}
