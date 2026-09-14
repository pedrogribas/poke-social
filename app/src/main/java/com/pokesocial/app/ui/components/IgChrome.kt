package com.pokesocial.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokesocial.app.domain.model.StoryGroup
import com.pokesocial.app.ui.theme.IgBlack
import com.pokesocial.app.ui.theme.IgHeart
import com.pokesocial.app.ui.theme.IgLightGray
import com.pokesocial.app.ui.theme.IgWhite

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
)

@Composable
fun IgHomeTopBar(
    onActivityClick: () -> Unit = {},
    onCreateClick: () -> Unit = {},
    onMessagesClick: () -> Unit = {},
    unreadMessages: Int = 0
) {
    Column(Modifier.fillMaxWidth().background(IgWhite)) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(start = 14.dp, end = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "Instagram",
                    fontFamily = FontFamily.Cursive,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = IgBlack
                )
                Icon(
                    Icons.Outlined.KeyboardArrowDown,
                    contentDescription = null,
                    tint = IgBlack,
                    modifier = Modifier
                        .padding(start = 2.dp)
                        .size(20.dp)
                )
            }
            IconButton(onClick = onCreateClick) {
                Icon(Icons.Outlined.AddBox, contentDescription = "Criar", tint = IgBlack)
            }
            IconButton(onClick = onActivityClick) {
                Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Atividade", tint = IgBlack)
            }
            Box {
                IconButton(onClick = onMessagesClick) {
                    Icon(
                        Icons.AutoMirrored.Outlined.Send,
                        contentDescription = "Mensagens",
                        tint = IgBlack
                    )
                }
                if (unreadMessages > 0) {
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 8.dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(IgHeart),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (unreadMessages > 9) "9+" else unreadMessages.toString(),
                            color = IgWhite,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        HorizontalDivider(color = IgLightGray, thickness = 0.5.dp)
    }
}

@Composable
fun IgSimpleTopBar(
    title: String,
    navigationIcon: (@Composable () -> Unit)? = null,
    onTitleClick: (() -> Unit)? = null,
    titleBold: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Column(Modifier.fillMaxWidth().background(IgWhite)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp)
                .height(44.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            navigationIcon?.invoke()
            Text(
                title,
                fontWeight = if (titleBold) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = if (titleBold) 20.sp else 16.sp,
                color = IgBlack,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .then(if (onTitleClick != null) Modifier.clickable(onClick = onTitleClick) else Modifier)
            )
            actions()
        }
        HorizontalDivider(color = IgLightGray, thickness = 0.5.dp)
    }
}

@Composable
fun IgBottomBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(containerColor = IgWhite, tonalElevation = 0.dp) {
        items.forEach { item ->
            val selected = currentRoute != null && currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        if (selected) item.selectedIcon else item.icon,
                        contentDescription = item.label,
                        tint = IgBlack
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = IgWhite,
                    selectedIconColor = IgBlack,
                    unselectedIconColor = IgBlack
                )
            )
        }
    }
}

@Composable
fun StoryRail(
    groups: List<StoryGroup>,
    onStoryClick: (StoryGroup) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth().background(IgWhite)
    ) {
        items(groups, key = { it.author.id }) { group ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(74.dp)
                    .clickable { onStoryClick(group) }
            ) {
                IgAvatar(
                    imageUrl = group.author.avatarUrl,
                    size = 66.dp,
                    showStoryRing = true,
                    seen = group.allSeen,
                    showAddBadge = group.author.isMe
                )
                Text(
                    if (group.author.isMe) "Seu story" else group.author.username,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = IgBlack,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
