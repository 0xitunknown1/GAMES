package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Tetris", appName)
  }

  @Test
  fun `blocks fall automatically in game loop`() = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.tetris.ui.TetrisViewModel(app)
    val initialY = vm.uiState.value.currentFigure?.y ?: 0
    println("Initial Y: $initialY")
    // Wait for gravity interval (750ms) to trigger automatic fall
    kotlinx.coroutines.delay(1100)
    val nextY = vm.uiState.value.currentFigure?.y ?: 0
    println("Next Y: $nextY")
    assertTrue("Piece should fall automatically without any user input", nextY > initialY)
  }
}
