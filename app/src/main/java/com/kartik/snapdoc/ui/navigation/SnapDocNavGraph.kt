package com.kartik.snapdoc.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.kartik.snapdoc.ui.screens.camera.CameraScreen
import com.kartik.snapdoc.ui.screens.doc_detail.DocDetailScreen
import com.kartik.snapdoc.ui.screens.export.ExportScreen
import com.kartik.snapdoc.ui.screens.home.HomeScreen
import com.kartik.snapdoc.ui.screens.onboarding.OnboardingScreen
import com.kartik.snapdoc.ui.screens.preview.PreviewScreen
import com.kartik.snapdoc.ui.screens.print_sheet.PrintSheetScreen
import com.kartik.snapdoc.ui.screens.processing.ProcessingScreen
import com.kartik.snapdoc.ui.screens.review.ReviewScreen
import com.kartik.snapdoc.ui.screens.settings.SettingsScreen
import com.kartik.snapdoc.ui.screens.splash.SplashScreen

@Composable
fun SnapDocNavGraph(
    navController: NavHostController,
    startDestination: String = Routes.SPLASH,
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onFirstLaunch = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onReturning = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onDocClick = { docId -> navController.navigate(Routes.docDetail(docId)) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) },
            )
        }

        composable(
            route = Routes.DOC_DETAIL,
            arguments = listOf(navArgument(Routes.Args.DOC_ID) { type = NavType.StringType }),
        ) {
            DocDetailScreen(
                onBack = { navController.popBackStack() },
                onTakePhoto = { docId -> navController.navigate(Routes.camera(docId)) },
            )
        }

        composable(
            route = Routes.CAMERA,
            arguments = listOf(navArgument(Routes.Args.DOC_ID) { type = NavType.StringType }),
        ) {
            CameraScreen(
                onClose = { navController.popBackStack() },
                onCaptured = { docId, uri ->
                    navController.navigate(Routes.review(docId, uri))
                },
            )
        }

        composable(
            route = Routes.REVIEW,
            arguments = listOf(
                navArgument(Routes.Args.DOC_ID) { type = NavType.StringType },
                navArgument(Routes.Args.IMAGE_URI) { type = NavType.StringType },
            ),
        ) {
            ReviewScreen(
                onRetake = { navController.popBackStack() },
                onUsePhoto = { docId, uri ->
                    navController.navigate(Routes.processing(docId, uri))
                },
            )
        }

        composable(
            route = Routes.PROCESSING,
            arguments = listOf(
                navArgument(Routes.Args.DOC_ID) { type = NavType.StringType },
                navArgument(Routes.Args.IMAGE_URI) { type = NavType.StringType },
            ),
        ) {
            ProcessingScreen(
                onDone = { docId, uri ->
                    navController.navigate(Routes.preview(docId, uri)) {
                        popUpTo(Routes.PROCESSING) { inclusive = true }
                    }
                },
                onError = { navController.popBackStack(Routes.CAMERA, inclusive = false) },
            )
        }

        composable(
            route = Routes.PREVIEW,
            arguments = listOf(
                navArgument(Routes.Args.DOC_ID) { type = NavType.StringType },
                navArgument(Routes.Args.IMAGE_URI) { type = NavType.StringType },
            ),
        ) {
            PreviewScreen(
                onBack = { navController.popBackStack() },
                onExport = { docId, uri -> navController.navigate(Routes.export(docId, uri)) },
                onPrintSheet = { docId, uri -> navController.navigate(Routes.printSheet(docId, uri)) },
            )
        }

        composable(
            route = Routes.EXPORT,
            arguments = listOf(
                navArgument(Routes.Args.DOC_ID) { type = NavType.StringType },
                navArgument(Routes.Args.IMAGE_URI) { type = NavType.StringType },
            ),
        ) {
            ExportScreen(
                onDone = {
                    navController.popBackStack(Routes.HOME, inclusive = false)
                },
            )
        }

        composable(
            route = Routes.PRINT_SHEET,
            arguments = listOf(
                navArgument(Routes.Args.DOC_ID) { type = NavType.StringType },
                navArgument(Routes.Args.IMAGE_URI) { type = NavType.StringType },
            ),
        ) {
            PrintSheetScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
