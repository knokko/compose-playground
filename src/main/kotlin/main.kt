import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.lang.Thread.sleep

private class TestObject(
    var x: Int = 1,
    var text: String = "hello $x"
) {
}

@Composable
@Preview
fun App() {
    println("App()")
    var test by remember { mutableStateOf(TestObject(), policy = neverEqualPolicy()) }

    Thread {
        repeat(50) {
            sleep(1000)
            test.x += 1
            test.text = "hm ${test.x}"
            test = test
            //test = TestObject(test.x, "hm ${test.x}")
            //test.update { it.x += 5; it.text = "changed ${it.x}"; TestObject(it.x, it.text) }
        }
    }.start()

    MaterialTheme {
        Button(
            onClick = { test.text = "whoops ${test.x}"; test = test },
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
