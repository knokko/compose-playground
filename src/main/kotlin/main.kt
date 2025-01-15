import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.github.knokko.bitser.connection.BitClient
import com.github.knokko.bitser.connection.BitListConnection
import com.github.knokko.bitser.connection.BitStructConnection
import com.github.knokko.bitser.serialize.Bitser
import kotlinx.coroutines.delay
import java.lang.Thread.sleep

fun <T: Any> changeState(state: T, change: T.() -> Unit): T {
    synchronized(state) {
        change(state)
    }
    return state
}

@Composable
private fun <T> keepRefreshing(state: T, onChange: (T) -> Unit) {
    LaunchedEffect(Unit) {
        try {
            while (true) {
                delay(1000)
                val wasModified = true
                if (wasModified) onChange(state)
            }
        } finally {
            if (state !is List<*>) println("cancelled state $state")
        }
    }
}

@Composable
fun composeArmorTypeOverview(armorTypes: BitListConnection<ArmorType>) = synchronized(armorTypes.list) {
    println("composeArmorTypeOverview")
    var state by remember { mutableStateOf(armorTypes, policy = neverEqualPolicy()) }
    Column(verticalArrangement = Arrangement.Bottom) {
        Box(Modifier.weight(1f)) {
            LazyColumn(verticalArrangement = Arrangement.Top) {
                synchronized(armorTypes.list) {
                    itemsIndexed(state.list) { index, _ -> composeArmorTypeRow(armorTypes.getChildStruct(index)) }
                }
            }
        }
        TextButton(onClick = { state = changeState(state) { addDelayed(ArmorType("", "", EquipmentSlotType.Body)) } }) {
            Text("Add armor type")
        }
        keepRefreshing(state) { state = it }
    }
}

@Composable
fun composeArmorTypeRow(armorType: BitStructConnection<ArmorType>) {
    println("composeArmorTypeRow")
    var connection by remember { mutableStateOf(armorType, policy = neverEqualPolicy()) }

    Row {
        TextField(connection.state.key, { connection = changeState(connection) { state.key = it } }, label = { Text("key") })
        TextField(connection.state.name, { connection = changeState(connection) { state.name = it } }, label = { Text("name") })

        keepRefreshing(connection) {
            connection.checkForChanges()
            connection = it
        }
    }
}

fun main() = application {
    val bitser = Bitser(true)
    val client = BitClient.tcp(bitser, Root::class.java, "localhost", 2102)
    Window(onCloseRequest = ::exitApplication) {
        Column {
            Text("Inventory")
            Text("Armor types")
            composeArmorTypeOverview(client.root.getChildList("armorTypes"))
        }
        LaunchedEffect(Unit) {
            try {
                while (true) delay(1000)
            } finally {
                println("close connection")
                client.close()
            }
        }
    }

    Thread {
        sleep(3000)
        repeat(10) {
            client.root.getChildList<ArmorType>("armorTypes").addDelayed(ArmorType("a", "b", EquipmentSlotType.Accessory))
        }
        repeat(10) { index ->
            sleep(1000)
            val armorType = client.root.getChildList<ArmorType>("armorTypes").getChildStruct<ArmorType>(index)
            armorType.state.name = "Overrule $index"
            armorType.checkForChanges()
        }
    }.start()
}
