package com.bennysamuel.livem.onboarding

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.bennysamuel.livem.theme.JournalDarkColorScheme
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.coroutines.delay
import livem.composeapp.generated.resources.Res
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit
) {
    val pagerState = rememberPagerState { 3 }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            if (!pagerState.isScrollInProgress) {
                pagerState.animateScrollToPage(
                    (pagerState.currentPage + 1) % pagerState.pageCount
                )
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val isTablet = screenWidth > 600.dp

        val parallaxOffset = pagerState.offsetForPage(0) * 120f

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { translationX = -parallaxOffset }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = JournalDarkColorScheme.surfaceVariant.copy(alpha = 0.6f),
                    radius = size.minDimension * 0.4f,
                    center = Offset(size.width * 0.85f, size.height * 0.15f)
                )
                drawCircle(
                    color = JournalDarkColorScheme.outline.copy(alpha = 0.3f),
                    radius = size.minDimension * 0.25f,
                    center = Offset(size.width * 0.15f, size.height * 0.75f)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (isTablet) screenWidth * 0.15f else 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(if (isTablet) 80.dp else 60.dp))

            Text(
                text = "Liv Em",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Light,
                    letterSpacing = 6.sp,
                    fontSize = if (isTablet) 42.sp else 32.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(if (screenHeight < 700.dp) 20.dp else 40.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isTablet) 450.dp else 340.dp),
                contentPadding = PaddingValues(horizontal = if (isTablet) 60.dp else 40.dp),
                pageSpacing = 20.dp
            ) { page ->
                val pageOffset = pagerState.offsetForPage(page).absoluteValue

                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            val scale = lerp(0.85f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                            scaleX = scale
                            scaleY = scale
                            alpha = lerp(0.5f, 1f, 1f - pageOffset.coerceIn(0f, 1f))

                            val rotation = lerp(0f, 8f, pageOffset.coerceIn(0f, 1f))
                            rotationY = if (pagerState.currentPage > page) rotation else -rotation
                        }
                        .clip(RoundedCornerShape(28.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CompottieLottie(
                        fileName = when (page) {
                            0 -> LottieRes.Write
                            1 -> LottieRes.Fit
                            else -> LottieRes.List
                        }
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            androidx.compose.animation.AnimatedContent(
                targetState = pagerState.currentPage,
                transitionSpec = {
                    (fadeIn() + scaleIn(initialScale = 0.95f))
                        .togetherWith(fadeOut() + scaleOut(targetScale = 0.95f))
                }
            ) { targetPage ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = when (targetPage) {
                            0 -> "Write Your Story"
                            1 -> "Track Progress"
                            else -> "Curate Daily"
                        },
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = when (targetPage) {
                            0 -> "Capture your thoughts and moments in a distraction-free environment."
                            1 -> "Monitor your habits and fitness goals with minimalist precision."
                            else -> "Organize your life's details into beautiful, curated lists."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            PagerDots(total = 3, selected = pagerState.currentPage)

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    "Begin Journey",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun androidx.compose.foundation.pager.PagerState.offsetForPage(page: Int) =
    (currentPage - page) + currentPageOffsetFraction

@Composable
fun PagerDots(total: Int, selected: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { index ->
            val isSelected = index == selected
            Box(
                modifier = Modifier
                    .size(width = if (isSelected) 20.dp else 8.dp, height = 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )
            )
        }
    }
}
@Composable
fun CompottieLottie(
    fileName: LottieRes,
    modifier: Modifier = Modifier
) {

    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/${fileName.file}").decodeToString()
        )
    }
    val progress by animateLottieCompositionAsState(composition)

    Image(
        painter = rememberLottiePainter(
            composition = composition,
            progress = { progress },
        ),
        contentDescription = "Lottie animation",
        modifier = modifier.fillMaxSize()

    )
}



enum class LottieRes(val file: String) {
    Write("write.json"),
    Fit("fitness.json"),
    List("list.json")
}
