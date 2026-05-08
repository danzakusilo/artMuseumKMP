package dev.danya.museum.feature.homescreen.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import demo.feature.homescreen.ui.generated.resources.Res
import demo.feature.homescreen.ui.generated.resources.promo_discover
import demo.feature.homescreen.ui.generated.resources.promo_gallery
import demo.feature.homescreen.ui.generated.resources.promo_heart
import demo.feature.homescreen.ui.generated.resources.promo_search
import dev.danya.museum.core.ui.theme.extendedColors
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun HomeScreen(
    onNavigateToArtworks: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToFeed: () -> Unit,
    onNavigateToSearch: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val extended = MaterialTheme.extendedColors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Museum",
            style = MaterialTheme.typography.headlineLarge,
            color = colors.onBackground,
        )

        PromoCard(
            title = "Browse Favourites",
            subtitle = "Your saved masterpieces",
            containerColor = extended.favoriteContainer,
            contentColor = extended.onFavoriteContainer,
            illustration = Res.drawable.promo_heart,
            onClick = onNavigateToFavorites,
        )

        PromoCard(
            title = "Discover",
            subtitle = "Swipe through art",
            containerColor = colors.secondaryContainer,
            contentColor = colors.onSecondaryContainer,
            illustration = Res.drawable.promo_discover,
            onClick = onNavigateToFeed,
        )

        PromoCard(
            title = "Search",
            subtitle = "Find any artwork",
            containerColor = colors.tertiaryContainer,
            contentColor = colors.onTertiaryContainer,
            illustration = Res.drawable.promo_search,
            onClick = onNavigateToSearch,
        )
    }
}

@Composable
private fun PromoCard(
    title: String,
    subtitle: String,
    containerColor: Color,
    contentColor: Color,
    illustration: DrawableResource,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = contentColor,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.7f),
                )
            }
            Image(
                painter = painterResource(illustration),
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(
                    color = contentColor.copy(alpha = 0.35f),
                    blendMode = BlendMode.SrcIn,
                ),
            )
        }
    }
}
