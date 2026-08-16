package com.checklisted.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.checklisted.app.ui.goal.GoalEditorScreen
import com.checklisted.app.ui.today.TodayScreen

sealed interface Destination {
    val route: String

    data object Today : Destination {
        override val route: String = "today"
    }

    /**
     * Create and edit share one destination.
     *
     * The goal id is optional: absent means "new goal", which keeps a single form and
     * a single view model instead of two that would drift apart.
     */
    data object GoalEditor : Destination {
        const val ARG_GOAL_ID = "goalId"
        override val route: String = "goal?$ARG_GOAL_ID={$ARG_GOAL_ID}"

        fun create(): String = "goal"

        fun edit(goalId: String): String = "goal?$ARG_GOAL_ID=$goalId"
    }
}

@Composable
fun ChecklistedNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Destination.Today.route,
        modifier = modifier,
    ) {
        composable(Destination.Today.route) {
            TodayScreen(
                onCreateGoal = { navController.navigate(Destination.GoalEditor.create()) },
                onOpenGoal = { goalId -> navController.navigate(Destination.GoalEditor.edit(goalId)) },
            )
        }

        composable(
            route = Destination.GoalEditor.route,
            arguments = listOf(
                navArgument(Destination.GoalEditor.ARG_GOAL_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            GoalEditorScreen(onDone = { navController.popBackStack() })
        }
    }
}
