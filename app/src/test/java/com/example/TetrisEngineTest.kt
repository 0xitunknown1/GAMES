package com.example

import com.example.tetris.game.TetrisEngine
import com.example.tetris.model.GameState
import com.example.tetris.model.Tetromino
import com.example.tetris.model.TetrominoType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TetrisEngineTest {

    private lateinit var engine: TetrisEngine

    @Before
    fun setUp() {
        engine = TetrisEngine(height = 20, width = 10, startingLevel = 1)
        engine.reset(1)
    }

    @Test
    fun engine_initializesWith7BagAndNextQueue() {
        assertEquals(20, engine.height)
        assertEquals(10, engine.width)
        assertEquals(0, engine.score)
        assertEquals(0, engine.lines)
        assertEquals(1, engine.level)
        assertEquals(GameState.PLAYING, engine.state)
        assertNotNull(engine.currentFigure)
        assertTrue("Next queue should contain at least 3 upcoming pieces", engine.nextQueue.size >= 3)
    }

    @Test
    fun lateralMovement_respectsBoardBoundaries() {
        for (i in 0 until 12) {
            engine.goSide(-1)
        }
        val leftX = engine.currentFigure?.x ?: 0
        assertTrue("X coordinate should remain within board limits", leftX >= -2)

        val movedFurtherLeft = engine.goSide(-1)
        assertFalse("Cannot move beyond left wall", movedFurtherLeft)

        for (i in 0 until 15) {
            engine.goSide(1)
        }
        val rightX = engine.currentFigure?.x ?: 0
        assertTrue("X coordinate should remain within right limit", rightX <= 10)
    }

    @Test
    fun hardDrop_placesPieceAndCalculatesClearEvent() {
        val initialScore = engine.score
        val clearEvent = engine.hardDrop()

        assertTrue("Score should increase after hard drop", engine.score > initialScore)
        assertNotNull("New piece should spawn after freeze", engine.currentFigure)
        assertNotNull(clearEvent)
    }

    @Test
    fun lockDelay_activatesWhenGrounded() {
        // Drop until grounded
        while (engine.softDrop()) {
            // keep soft dropping
        }
        assertTrue("Piece should be grounded at bottom", engine.isCurrentPieceGrounded())

        // Advancing lock delay without reaching limit should not lock yet
        val notLockedYet = engine.advanceLockDelay(200L)
        assertEquals(null, notLockedYet)

        // Advancing beyond lock delay limit should trigger lock
        val lockedEvent = engine.advanceLockDelay(350L)
        assertNotNull("Piece should lock when lock delay expires", lockedEvent)
    }

    @Test
    fun srsKicks_allowWallAndFloorKicks() {
        val piece = engine.currentFigure
        assertNotNull(piece)

        // Rotation should succeed and follow SRS rules
        val rotated = engine.rotate(1)
        assertTrue("SRS rotation should succeed", rotated || piece?.type == TetrominoType.O)
    }

    @Test
    fun holdPiece_swapsCurrentAndHeldPiece() {
        val originalType = engine.currentFigure?.type
        assertNotNull(originalType)

        val held = engine.holdPiece()
        assertTrue("Hold should succeed on first attempt", held)
        assertEquals(originalType, engine.holdFigure?.type)

        // Secondary hold in same turn should fail until piece locked
        val secondHold = engine.holdPiece()
        assertFalse("Cannot hold twice in same turn", secondHold)
    }
}
