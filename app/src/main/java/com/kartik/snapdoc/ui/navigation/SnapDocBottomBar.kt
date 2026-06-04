package com.kartik.snapdoc.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import com.kartik.snapdoc.ui.theme.Hairline
import com.kartik.snapdoc.ui.theme.Ink4
import com.kartik.snapdoc.ui.theme.Primary

internal enum class BottomTab(
    val route: Any,
    val label: String,
    val icon: ImageVector,
) {
    Home(Routes.Home, "Home", Icons.Outlined.Home),
    History(Routes.History, "History", Icons.Outlined.History),
    Documents(Routes.Documents, "Documents", Icons.Outlined.Folder),
    Profile(Routes.Settings, "Profile", Icons.Outlined.Person),
}

internal fun NavDestination?.isBottomBarDestination(): Boolean = this != null && (
    hasRoute<Routes.Home>() ||
    hasRoute<Routes.History>() ||
    hasRoute<Routes.Documents>() ||
    hasRoute<Routes.Settings>()
)

@Composable
internal fun SnapDocBottomBar(
    navController: NavHostController,
    currentDestination: NavDestination?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Hairline))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 8.dp)
                .navigationBarsPadding(),
        ) {
            BottomTab.entries.forEach { tab ->
                val active = currentDestination.isOnTab(tab)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier
                        .clickable(
                            role = Role.Tab,
                            onClickLabel = tab.label,
                        ) {
                            if (!active) {
                                navController.navigate(tab.route) {
                                    popUpTo(Routes.Home) { saveState = true; inclusive = false }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                        .padding(vertical = 6.dp, horizontal = 14.dp),
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = null,
                        tint = if (active) Primary else Ink4,
                        modifier = Modifier.size(22.dp),
                    )
                    Text(
                        text = tab.label,
                        color = if (active) Primary else Ink4,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                        ),
                    )
                }
            }
        }
    }
}

private fun NavDestination?.isOnTab(tab: BottomTab): Boolean = when (tab) {
    BottomTab.Home -> this?.hasRoute<Routes.Home>() == true
    BottomTab.History -> this?.hasRoute<Routes.History>() == true
    BottomTab.Documents -> this?.hasRoute<Routes.Documents>() == true
    BottomTab.Profile -> this?.hasRoute<Routes.Settings>() == true
}
