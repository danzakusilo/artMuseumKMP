package dev.danya.museum.feature.artworks.ui.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import demo.core.ui.generated.resources.Res
import demo.core.ui.generated.resources.department_american_decorative_arts
import demo.core.ui.generated.resources.department_ancient_west_asian_art
import demo.core.ui.generated.resources.department_arms_and_armor
import demo.core.ui.generated.resources.department_arts_of_africa_oceania_americas
import demo.core.ui.generated.resources.department_asian_art
import demo.core.ui.generated.resources.department_drawings_and_prints
import demo.core.ui.generated.resources.department_egyptian_art
import demo.core.ui.generated.resources.department_european_paintings
import demo.core.ui.generated.resources.department_european_sculpture_and_decorative_arts
import demo.core.ui.generated.resources.department_greek_and_roman_art
import demo.core.ui.generated.resources.department_islamic_art
import demo.core.ui.generated.resources.department_medieval_art
import demo.core.ui.generated.resources.department_modern_art
import demo.core.ui.generated.resources.department_musical_instruments
import demo.core.ui.generated.resources.department_photographs
import demo.core.ui.generated.resources.department_the_cloisters
import demo.core.ui.generated.resources.department_the_costume_institute
import demo.core.ui.generated.resources.department_the_robert_lehman_collection
import demo.core.ui.generated.resources.feed_choose_department
import demo.core.ui.generated.resources.feed_shuffle
import dev.danya.museum.feature.artworks.domain.entity.Department
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepartmentPickerSheet(
    currentDepartment: Department,
    onSelectDepartment: (Department?) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
        ) {
            item {
                Text(
                    text = stringResource(Res.string.feed_choose_department),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            item {
                ListItem(
                    headlineContent = {
                        Text(stringResource(Res.string.feed_shuffle))
                    },
                    leadingContent = {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectDepartment(null) },
                    tonalElevation = 0.dp,
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }

            items(Department.entries.toList()) { department ->
                val isSelected = department == currentDepartment
                ListItem(
                    headlineContent = {
                        Text(
                            text = stringResource(department.stringResource()),
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectDepartment(department) },
                    tonalElevation = 0.dp,
                )
            }
        }
    }
}

internal fun Department.stringResource(): StringResource = when (this) {
    Department.AMERICAN_DECORATIVE_ARTS -> Res.string.department_american_decorative_arts
    Department.ANCIENT_WEST_ASIAN_ART -> Res.string.department_ancient_west_asian_art
    Department.ARMS_AND_ARMOR -> Res.string.department_arms_and_armor
    Department.ARTS_OF_AFRICA_OCEANIA_AMERICAS -> Res.string.department_arts_of_africa_oceania_americas
    Department.ASIAN_ART -> Res.string.department_asian_art
    Department.THE_CLOISTERS -> Res.string.department_the_cloisters
    Department.THE_COSTUME_INSTITUTE -> Res.string.department_the_costume_institute
    Department.DRAWINGS_AND_PRINTS -> Res.string.department_drawings_and_prints
    Department.EGYPTIAN_ART -> Res.string.department_egyptian_art
    Department.EUROPEAN_PAINTINGS -> Res.string.department_european_paintings
    Department.EUROPEAN_SCULPTURE_AND_DECORATIVE_ARTS -> Res.string.department_european_sculpture_and_decorative_arts
    Department.GREEK_AND_ROMAN_ART -> Res.string.department_greek_and_roman_art
    Department.ISLAMIC_ART -> Res.string.department_islamic_art
    Department.THE_ROBERT_LEHMAN_COLLECTION -> Res.string.department_the_robert_lehman_collection
    Department.MEDIEVAL_ART -> Res.string.department_medieval_art
    Department.MUSICAL_INSTRUMENTS -> Res.string.department_musical_instruments
    Department.PHOTOGRAPHS -> Res.string.department_photographs
    Department.MODERN_ART -> Res.string.department_modern_art
}
