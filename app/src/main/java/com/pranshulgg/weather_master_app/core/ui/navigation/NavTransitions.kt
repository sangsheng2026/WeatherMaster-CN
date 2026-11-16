package com.pranshulgg.weather_master_app.core.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi

/**
 * Navigation transitions.
 *
 * Keep these short and directional: during a predictive-back gesture the platform already
 * scales the page down, so a long full-screen fade on top of it felt like the page "hung"
 * for half a second after the finger was lifted. A small offset plus a quick fade keeps the
 * gesture and the committed transition on the same visual direction.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
object NavTransitions {

    private const val ENTER_DURATION = 280
    private const val EXIT_DURATION = 240
    private const val POP_ENTER_DURATION = 240
    private const val POP_EXIT_DURATION = 260

    fun enter(): EnterTransition =
        slideInHorizontally(
            animationSpec = tween(ENTER_DURATION),
            initialOffsetX = { it / 8 }
        ) + fadeIn(tween(ENTER_DURATION))

    fun exit(): ExitTransition =
        slideOutHorizontally(
            animationSpec = tween(EXIT_DURATION),
            targetOffsetX = { -it / 12 }
        ) + fadeOut(tween(EXIT_DURATION))

    fun popEnter(): EnterTransition =
        slideInHorizontally(
            animationSpec = tween(POP_ENTER_DURATION),
            initialOffsetX = { -it / 12 }
        ) + fadeIn(tween(POP_ENTER_DURATION))

    fun popExit(): ExitTransition =
        slideOutHorizontally(
            animationSpec = tween(POP_EXIT_DURATION),
            targetOffsetX = { it / 8 }
        ) + fadeOut(tween(POP_EXIT_DURATION))
}