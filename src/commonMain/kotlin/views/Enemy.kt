package views

import com.soywiz.klock.milliseconds
import com.soywiz.klock.seconds
import com.soywiz.korge.time.delay
import com.soywiz.korge.tween.get
import com.soywiz.korge.tween.tween
import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors
import com.soywiz.korim.format.readBitmap
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korma.geom.Point
import com.soywiz.korma.interpolation.Easing
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Enemy(val direction: Point): Container() {

    enum class State{
        READY,
        APPEARING,
        MOVING,
        DYING
    }

    private lateinit var movingView: Image
    private lateinit var appearingView: Sprite

    var moveSpeed: Double = 50.0
    var radius: Float = 50f
    lateinit var state: State

    suspend fun loadEnemy(){
        state = Enemy.State.READY
        val appearingViewMap = resourcesVfs["game_scene/enemy/enemy_appearing.png"].readBitmap()
        appearingView = Sprite(initialAnimation = SpriteAnimation(
                spriteMap = appearingViewMap,
                spriteWidth = 15,
                spriteHeight = 15,
                columns = 16,
                rows = 1
        ), smoothing = false, anchorX = .5)
        appearingView.spriteDisplayTime = 40.milliseconds


        movingView = Image(resourcesVfs["game_scene/enemy/enemy_idle.png"].readBitmap(),
                smoothing = false, anchorX = .5)

        addChild(appearingView)
    }

    fun live() {
        state = Enemy.State.APPEARING
        removeChildren()
        addChild(appearingView)
        GlobalScope.launch {
            this@Enemy.tween(this@Enemy::scale[2.0], time = .3.seconds, easing = Easing.EASE_IN_OUT)
            this@Enemy.tween(this@Enemy::scale[1.0], time = .4.seconds, easing = Easing.EASE_IN_OUT)
        }
        appearingView.playAnimation()
        appearingView.onAnimationCompleted.once{
            println("On Live animation completed")
            state = Enemy.State.MOVING
            removeChildren()
            addChild(movingView)
        }
    }

    fun die(onDie: () -> Unit) {
        state = State.DYING
        removeChildren()
        addChild(appearingView)
        tint = Colors.DARKMAGENTA
        GlobalScope.launch {
            this@Enemy.tween(this@Enemy::scale[0.1], time = .5.seconds, easing = Easing.EASE_IN_OUT)
        }
        appearingView.playAnimation(reversed = true)
        appearingView.onAnimationCompleted.once{
            state = Enemy.State.READY
            tint = Colors.WHITE
            onDie()
            removeChildren()
        }
    }

    fun infect(onInfect: () -> Unit) {
        state = Enemy.State.READY
        tint = Colors.DARKMAGENTA
        GlobalScope.launch {
            delay(.3.seconds)
            tint = Colors.WHITE
            removeChildren()
            onInfect()
        }
    }

    fun resetEnemy(): Unit{
        state = State.READY
        scale = 1.0
        tint = Colors.WHITE
    }
}