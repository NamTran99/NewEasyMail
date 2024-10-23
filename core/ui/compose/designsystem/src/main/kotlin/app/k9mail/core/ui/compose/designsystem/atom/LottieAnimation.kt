package app.k9mail.core.ui.compose.designsystem.atom

import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun LottieAnimationApp(
    @RawRes animRes: Int,
    iteration: Int = LottieConstants.IterateForever,
    modifier: Modifier = Modifier,

    ) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animRes))
    val progress by animateLottieCompositionAsState(composition, iterations = iteration)

    LottieAnimation(
        modifier = modifier,
        composition = composition,
        progress = { progress },
    )
}
