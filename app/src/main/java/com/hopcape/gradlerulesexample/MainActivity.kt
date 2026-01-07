package com.hopcape.gradlerulesexample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.hopcape.gradlerulesexample.ui.theme.GradleRulesExampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GradleRulesExampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

private fun hugeFunction(){
    println("Doing something")
    println("Doing something")
    println("Doing something")
    println("Doing something")
    println("Doing something")
    println("Doing something")
    println("Doing something")
    println("Doing something")
    println("Doing something")
    println("Doing something")
    println("Doing something")
    println("Doing something")
    println("Doing something")
    println("Doing something")
    println("Doing something")
    println("Doing something")
    println("Doing something")
    println("Doing something")
    println("Doing something")
    println("Doing something")
    println("Doing something")
    println("Doing something")
    println("Doing something")
    println("Doing something")
    println("Doing something")
    println("Doing something")
    println("Doing something")
    println("Doing something")
    println("Doing something")
    println("Doing something")
    println("Doing something")
    println("Doing something")
    println("Doing something")
    println("Doing something")
    println("Doing something")
    println("Doing something")
    println("Doing something")
    println("Doing something")
    println("Doing something")
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GradleRulesExampleTheme {
        Greeting("Android")
    }
}