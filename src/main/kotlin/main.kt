import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.lang.Thread.sleep

private class TestObject(
    var x: Int = 1,
    var text: String = "hello $x"
) {
}

fun <T: Any> changeState(state: T, change: T.() -> Unit): T {
    synchronized(state) {
        change(state)
        // TODO Also acquire lock while reading
    }
    return state
}

@Composable
fun composeArmorTypeOverview(armorTypes: MutableList<ArmorType>) {
    println("composeArmorTypeOverview")
    Column(verticalArrangement = Arrangement.Bottom) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Top) {
            composeArmorTypesList(armorTypes)
        }
        composeArmorTypeToolbar(armorTypes)
    }
}

@Composable
fun composeArmorTypeToolbar(armorTypes: MutableList<ArmorType>) {
    Text("Hello")
}

@Composable
fun composeArmorTypesList(armorTypes: MutableList<ArmorType>) {
    println("composeArmorTypesList")
    Column {
        for (armorType in armorTypes) composeArmorTypeRow(armorType)
    }
}

@Composable
fun composeArmorTypeRow(armorType: ArmorType) {
    println("composeArmorTypeRow")
    var state by remember { mutableStateOf(armorType, policy = neverEqualPolicy()) }

    Row {
        TextField(state.key, { state = changeState(state) { key = it } }, label = { Text("key") })
        TextField(state.name, { state = changeState(state) { name = it } }, label = { Text("name") })
    }
}

@Composable
@Preview
fun App() {
    println("App()")
    var test by remember { mutableStateOf(TestObject(), policy = neverEqualPolicy()) }

    Thread {
        repeat(50) {
            sleep(1000)
            test = changeState(test) {
                x += 1
                text = "hm $x"
            }
        }
    }.start()

    MaterialTheme {
        Button(
            onClick = { test = changeState(test) { text = "whoops $x" } },
            modifier = Modifier.testTag("button")
        ) {
            Text(test.text)
        }
    }
}

fun main() = application {
    val armorTypes = mutableListOf(
        ArmorType("iron-armor", "Iron Armor", EquipmentSlotType.Body),
        ArmorType("k-shield", "Kite Shield", EquipmentSlotType.Head)
    )
    Window(onCloseRequest = ::exitApplication) {
        composeArmorTypeOverview(armorTypes)
    }
}
