package com.example.parkora.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parkora.ui.theme.DarkNavy
import com.example.parkora.ui.theme.TextGray

/**
 * Two tabs inside the yellow header, like "Ongoing | Past" in Figure 4.
 * The selected tab has dark bold text and a thick dark line under it.
 */
@Composable
fun TabSwitcher(tabs: List<String>, selectedIndex: Int, onTabSelected: (Int) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        tabs.forEachIndexed { index, title ->
            val selected = index == selectedIndex
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(index) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selected) DarkNavy else TextGray,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(if (selected) DarkNavy else Color.Transparent, RoundedCornerShape(2.dp))
                )
            }
        }
    }
}
