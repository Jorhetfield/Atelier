package com.jorhetfield.atelier.designsystem.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "Light", showBackground = true, backgroundColor = 0xFFFBF8F3)
@Preview(
    name = "Dark",
    showBackground = true,
    backgroundColor = 0xFF15130F,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
public annotation class AtelierPreviews

@Preview(name = "Phone", device = Devices.PIXEL_7_PRO, showSystemUi = true)
@Preview(
    name = "Phone dark",
    device = Devices.PIXEL_7_PRO,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Preview(name = "Foldable", device = Devices.PIXEL_FOLD, showSystemUi = true)
@Preview(name = "Tablet", device = Devices.PIXEL_TABLET, showSystemUi = true)
public annotation class AtelierScreenPreviews

@Preview(name = "Font large", showBackground = true, backgroundColor = 0xFFFBF8F3, fontScale = 1.5f)
@Preview(name = "Font huge", showBackground = true, backgroundColor = 0xFFFBF8F3, fontScale = 2f)
public annotation class AtelierFontScalePreviews
