package com.kartik.snapdoc.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
    startDestination: Any = Routes.Splash,
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable<Routes.Splash> {
            SplashScreen(
                onFirstLaunch = {
                    navController.navigate(Routes.Onboarding) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                },
                onReturning = {
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                },
            )
        }

        composable<Routes.Onboarding> {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Onboarding) { inclusive = true }
                    }
                },
            )
        }

        composable<Routes.Home> {
            HomeScreen(
                onDocClick = { docId -> navController.navigate(Routes.DocDetail(docId)) },
                onSettingsClick = { navController.navigate(Routes.Settings) },
            )
        }

        composable<Routes.DocDetail> {
            DocDetailScreen(
                onBack = { navController.popBackStack() },
                onTakePhoto = { docId -> navController.navigate(Routes.Camera(docId)) },
            )
        }

        composable<Routes.Camera> {
            CameraScreen(
                onClose = { navController.popBackStack() },
                onCaptured = { docId, uri ->
                    navController.navigate(Routes.Review(docId, uri))
                },
            )
        }

        composable<Routes.Review> {
            ReviewScreen(
                onRetake = { navController.popBackStack() },
                onUsePhoto = { docId, uri ->
                    navController.navigate(Routes.Processing(docId, uri))
                },
            )
        }

        composable<Routes.Processing> {
            ProcessingScreen(
                onDone = { docId, uri ->
                    navController.navigate(Routes.Preview(docId, uri)) {
                        popUpTo<Routes.Processing> { inclusive = true }
                    }
                },
                onError = { navController.popBackStack<Routes.Camera>(inclusive = false) },
            )
        }

        composable<Routes.Preview> {
            PreviewScreen(
                onBack = { navController.popBackStack() },
                onExport = { docId, uri -> navController.navigate(Routes.Export(docId, uri)) },
                onPrintSheet = { docId, uri -> navController.navigate(Routes.PrintSheet(docId, uri)) },
            )
        }

        composable<Routes.Export> {
            ExportScreen(
                onDone = {
                    navController.popBackStack(Routes.Home, inclusive = false)
                },
            )
        }

        composable<Routes.PrintSheet> {
            PrintSheetScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable<Routes.Settings> {
            SettingsScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
