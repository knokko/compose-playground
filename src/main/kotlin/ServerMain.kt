import com.github.knokko.bitser.connection.BitServer
import com.github.knokko.bitser.serialize.Bitser

fun main() {
	val bitser = Bitser(true)
	val root = Root()
	val server = BitServer.tcp(bitser, root, 2102)

	readlnOrNull()
	server.stop()

	println("final armor types are ${root.armorTypes}")
}
