import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
