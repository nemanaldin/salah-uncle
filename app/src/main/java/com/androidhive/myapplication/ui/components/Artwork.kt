package com.androidhive.myapplication.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.androidhive.myapplication.R

@DrawableRes
fun lessonArtwork(id: String): Int = when {
    id == "wudu" || id.startsWith("purification-") -> R.drawable.heritage_wudu
    id == "ghusl" -> R.drawable.heritage_ghusl
    id == "tayammum" -> R.drawable.heritage_tayammum
    id == "sajdah-sahw" -> R.drawable.heritage_sujud
    id == "dhikr" || id == "tasbih-prayer" -> R.drawable.heritage_dhikr
    id == "wudu-remembrance" -> R.drawable.heritage_dua
    else -> R.drawable.heritage_prayer
}

@Composable
fun PhotoPanel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Box(modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp))) {
        Image(painterResource(R.drawable.heritage_mosque), null,
            Modifier.matchParentSize(), contentScale = ContentScale.Crop, alignment = Alignment.CenterStart)
        Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color(0x660B2725), Color(0xDD0B2725)))))
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

@Composable
fun IllustratedHeading(title: String, @DrawableRes artwork: Int, subtitle: String? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
            subtitle?.let { SupportingText(it) }
        }
        Image(painterResource(artwork), null, Modifier.size(82.dp), contentScale = ContentScale.Fit)
    }
}
