import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.CancellationException
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
fun composeArmorTypeOverview(armorTypes: MutableList<ArmorType>) = synchronized(armorTypes) {
    println("composeArmorTypeOverview")
    var state by remember { mutableStateOf(armorTypes, policy = neverEqualPolicy()) }
    Column(verticalArrangement = Arrangement.Bottom) {
        Box(Modifier.weight(1f)) {
            LazyColumn(verticalArrangement = Arrangement.Top) {
                synchronized(armorTypes) {
                    items(state) { composeArmorTypeRow(it) }
                }
            }
        }
        TextButton(onClick = { state = changeState(state) { add(ArmorType("", "", EquipmentSlotType.Body)) } }) {
            Text("Add armor type")
        }
        keepRefreshing(state) { state = it }
    }
}

@Composable
fun composeArmorTypeRow(armorType: ArmorType) {
    println("composeArmorTypeRow")
    var state by remember { mutableStateOf(armorType, policy = neverEqualPolicy()) }

    Row {
        TextField(state.key, { state = changeState(state) { key = it } }, label = { Text("key") })
        TextField(state.name, { state = changeState(state) { name = it } }, label = { Text("name") })

        keepRefreshing(state) { state = it }
    }
}

fun main() = application {
    val armorTypes = mutableListOf(
        ArmorType("iron-armor", "Iron Armor", EquipmentSlotType.Body),
        ArmorType("k-shield", "Kite Shield", EquipmentSlotType.Head)
    )
    Window(onCloseRequest = ::exitApplication) {
        Column {
            Text("Inventory")
            Text("Armor types")
            composeArmorTypeOverview(armorTypes)
        }
        LaunchedEffect(Unit) {
            try {
                while (true) delay(1000)
            } finally {
                println("close connection")
            }
        }
    }

    Thread {
        sleep(3000)
        synchronized(armorTypes) {
            repeat(100_000) {
                armorTypes.add(ArmorType("a", "b", EquipmentSlotType.Accessory))
            }
        }
        repeat(100) { index ->
            sleep(1000)
            armorTypes[index].name = "Overrule $index"
        }
    }.start()
}
