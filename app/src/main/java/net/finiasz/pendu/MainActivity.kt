package net.finiasz.pendu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.finiasz.pendu.LetterState.*
import net.finiasz.pendu.ui.theme.PenduTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PenduTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
                    Plateau()
                }
            }
        }

        onBackPressedDispatcher.addCallback(onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                moveTaskToBack(true)
            }
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Plateau(gameViewModel : GameViewModel = viewModel()) {
    val state : GameState by gameViewModel.state.collectAsState()
    val reloadConfirmation = remember { mutableStateOf(false) }
    val showSettings = remember { mutableStateOf(false) }
    val showHint = remember { mutableStateOf(false) }

    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }

    fun reset() {
        CoroutineScope(Dispatchers.IO).launch {
            gameViewModel.reset(settingsManager)
        }
    }

    LaunchedEffect(Unit) {
        if (gameViewModel.state.value.mot == null) {
            settingsManager.countWords()
            reset()
        }
    }

    val sizes : Sizes = with(LocalConfiguration.current) {
        if (this.screenWidthDp > this.screenHeightDp) {
            val letterSize : Float = this.screenHeightDp / 9.7f
            val motLetterSize : Float =
                (letterSize * 1.5f).coerceAtMost(state.mot?.let { (this.screenWidthDp  - letterSize * 1.2f) * 1.25f / it.length } ?: Float.MAX_VALUE)

            val hangmanWidthDp = this.screenWidthDp - letterSize * 10.6f
            val hangmanHeightDp = this.screenHeightDp - letterSize * 3.5f

            Sizes(
                true,
                letterSizeDp = letterSize,
                letterSizeSp = letterSize * .7f / this.fontScale,
                motLetterSizeDp = motLetterSize,
                motLetterSizeSp = motLetterSize * .7f / this.fontScale,
                hangmanWidthDp = hangmanWidthDp,
                hangmanHeightDp = hangmanHeightDp,
            )
        } else {
            val letterSize : Float = this.screenWidthDp * .1f
            val motLetterSize : Float =
                (this.screenWidthDp * .125f).coerceAtMost(state.mot?.let { (this.screenWidthDp  - letterSize * 1.2f) * 1.25f / it.length } ?: Float.MAX_VALUE)
            val hangmanWidthDp = this.screenWidthDp - letterSize * 1.2f
            val hangmanHeightDp = hangmanWidthDp.coerceAtMost(this.screenHeightDp - letterSize * 9.4f)

            Sizes(
                false,
                letterSizeDp = letterSize,
                letterSizeSp = letterSize * .7f / this.fontScale,
                motLetterSizeDp = motLetterSize,
                motLetterSizeSp = motLetterSize * .7f / this.fontScale,
                hangmanWidthDp = hangmanWidthDp,
                hangmanHeightDp = hangmanHeightDp,
            )
        }
    }

    if (sizes.landscape) {
        Column (
            Modifier
                .padding(
                    start = (sizes.letterSizeDp * .6).dp,
                    end = (sizes.letterSizeDp * .6).dp,
                    bottom = (sizes.letterSizeDp * .6).dp
                )
                .fillMaxWidth()
                .fillMaxHeight(),
        ) {
            // target mot
            Mot(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((2.9 * sizes.letterSizeDp).dp),
                sizes = sizes,
                state = state
            )

            Row(
                modifier = Modifier
                    .weight(1f, true)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // hangman drawing
                Box(
                    modifier = Modifier
                        .width(sizes.hangmanWidthDp.dp)
                        .height(sizes.hangmanHeightDp.dp)
                        .border(
                            width = when (state.won) {
                                Won.NOT_WON -> 1.dp
                                else -> 5.dp
                            },
                            brush = SolidColor(
                                when (state.won) {
                                    Won.WON -> MaterialTheme.colorScheme.tertiaryContainer
                                    Won.LOST -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.outline
                                }
                            ),
                            shape = RoundedCornerShape(8.dp),
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Flag(
                        modifier = Modifier
                            .padding(start = (sizes.letterSizeDp * .3f).dp, top = (sizes.letterSizeDp * .3f).dp)
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        dictionary = settingsManager.dictionary.value,
                        fontSize = sizes.letterSizeSp.sp
                    )

                    if (state.mot != null) {
                        Pendu(
                            totalAttempts = state.erreursMax,
                            remainingAttempts = state.erreursRestantes
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width((8.8 * sizes.letterSizeDp).dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // letters
                    listOf(
                        listOf('A', 'B', 'C', 'D', 'E', 'F', 'G'),
                        listOf('H', 'I', 'J', 'K', 'L', 'M', 'N'),
                        listOf('O', 'P', 'Q', 'R', 'S', 'T', 'U'),
                        listOf('V', 'W', 'X', 'Y', 'Z')
                    ).forEach {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(
                                (sizes.letterSizeDp * .3).dp,
                                Alignment.CenterHorizontally
                            )
                        ) {
                            it.forEach {
                                Lettre(
                                    char = it,
                                    letterStates = state.letterStates,
                                    voyelles = settingsManager.voyelles.value,
                                    won = state.won,
                                    sizes = sizes,
                                    click = gameViewModel::lettreClick
                                )
                            }
                        }
                    }




                    // controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        SettingsButton(sizes = sizes) { showSettings.value = true }
                        HintButton(sizes = sizes, enabled = state.won == Won.NOT_WON) { showHint.value = true }
                        ResetButton(sizes = sizes) {
                            if (state.won == Won.NOT_WON) {
                                reloadConfirmation.value = true
                            } else {
                                reset()
                            }
                        }
                    }
                }
            }
        }
    } else {
        Column(
            Modifier
                .padding((sizes.letterSizeDp * .6).dp)
                .fillMaxWidth()
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // hangman drawing
            Box(
                modifier = Modifier
                    .width(sizes.hangmanWidthDp.dp)
                    .height(sizes.hangmanHeightDp.dp)
                    .border(
                        width = when (state.won) {
                            Won.NOT_WON -> 1.dp
                            else -> 5.dp
                        },
                        brush = SolidColor(
                            when (state.won) {
                                Won.WON -> MaterialTheme.colorScheme.tertiaryContainer
                                Won.LOST -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.outline
                            }
                        ),
                        shape = RoundedCornerShape(8.dp),
                    ),
                contentAlignment = Alignment.Center
            ) {
                Flag(
                    modifier = Modifier
                        .padding(start = (sizes.letterSizeDp * .3f).dp, top = (sizes.letterSizeDp * .3f).dp)
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    dictionary = settingsManager.dictionary.value,
                    fontSize = sizes.letterSizeSp.sp
                )

                if (state.mot != null) {
                    Pendu(
                        totalAttempts = state.erreursMax,
                        remainingAttempts = state.erreursRestantes
                    )
                }
            }


            // target mot
            Mot(modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = (2 * sizes.letterSizeDp).dp)
                .weight(1f, true),
                sizes = sizes,
                state = state
            )

            // input letters
            Column (
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy((sizes.letterSizeDp * .3).dp, Alignment.CenterVertically)
            ) {
                listOf(
                    listOf('A', 'B', 'C', 'D', 'E', 'F', 'G'),
                    listOf('H', 'I', 'J', 'K', 'L', 'M', 'N'),
                    listOf('O', 'P', 'Q', 'R', 'S', 'T', 'U'),
                    listOf('V', 'W', 'X', 'Y', 'Z')
                ).forEach {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(
                            (sizes.letterSizeDp * .3).dp,
                            Alignment.CenterHorizontally
                        )
                    ) {
                        it.forEach {
                            Lettre(
                                char = it,
                                letterStates = state.letterStates,
                                voyelles = settingsManager.voyelles.value,
                                won = state.won,
                                sizes = sizes,
                                click = gameViewModel::lettreClick
                            )
                        }
                    }
                }
            }


            // controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = (sizes.letterSizeDp * .3).dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                SettingsButton(sizes = sizes) { showSettings.value = true }
                HintButton(sizes = sizes, enabled = state.won == Won.NOT_WON) { showHint.value = true }
                ResetButton(sizes = sizes) {
                    if (state.won == Won.NOT_WON) {
                        reloadConfirmation.value = true
                    } else {
                        reset()
                    }
                }
            }
        }
    }


    if (reloadConfirmation.value) {
        AlertDialog(
            onDismissRequest = {
                reloadConfirmation.value = false
            },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .wrapContentWidth()
                .padding(horizontal = 16.dp),
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.reload_confirmation),
                    fontSize = TextUnit(24f, TextUnitType.Sp),
                    lineHeight = TextUnit(32f, TextUnitType.Sp),
                )
                Row(
                    modifier = Modifier
                        .padding(top = 32.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            reloadConfirmation.value = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outline)
                    ) {
                        Text(
                            text = stringResource(id = R.string.cancel),
                            fontSize = TextUnit(24f, TextUnitType.Sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = {
                            reloadConfirmation.value = false
                            reset()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Text(
                            text = stringResource(id = R.string.ok),
                            fontSize = TextUnit(24f, TextUnitType.Sp),
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                }
            }
        }
    }

    if (showSettings.value) {
        SettingsDialog(settingsManager = settingsManager) {
            showSettings.value = false
        }
    }
    if (showHint.value) {
        HintDialog(viewModel = gameViewModel, state = state) {
            showHint.value = false
        }
    }
}



@Composable
fun Mot(modifier: Modifier = Modifier, sizes: Sizes, state: GameState) {
    Row (
        modifier = modifier.padding(bottom = (sizes.letterSizeDp *.3f).dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ){
        state.mot?.forEachIndexed {index, char ->
            val underlineColor = MaterialTheme.colorScheme.onSurface
            Box (
                modifier = Modifier
                    .height(sizes.motLetterSizeDp.dp)
                    .width((sizes.motLetterSizeDp * .8).dp)
                    .then(
                        if (state.lettresTrouvees?.get(index) != null)
                            Modifier.drawBehind {
                                val y = size.height - density
                                drawLine(
                                    color = underlineColor,
                                    start = Offset(size.width * .1f, y),
                                    end = Offset(size.width * .9f, y),
                                    strokeWidth = 2 * density
                                )
                            }
                        else
                            Modifier
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text (
                    text = if (state.lettresTrouvees?.get(index) != false || state.won != Won.NOT_WON) char.toString() else "",
                    fontSize = sizes.motLetterSizeSp.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (state.lettresTrouvees?.get(index) == null)
                        MaterialTheme.colorScheme.onSurface
                    else if (state.lettresTrouvees[index] == false && state.won == Won.LOST)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.tertiaryContainer,
                )
            }
        }
    }
}

@Composable
fun Pendu(modifier: Modifier = Modifier, totalAttempts : Int, remainingAttempts : Int) {
    Image(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .alpha(.15f),
        painter = painterResource(id = when(totalAttempts) {
            8 -> {
                when (remainingAttempts) {
                    8 -> R.drawable.hangman_1
                    7 -> R.drawable.hangman_2
                    6 -> R.drawable.hangman_3
                    5 -> R.drawable.hangman_4
                    4 -> R.drawable.hangman_5
                    3 -> R.drawable.hangman_6
                    2 -> R.drawable.hangman_7
                    else -> R.drawable.hangman_8
                }
            }
            6 -> {
                when (remainingAttempts) {
                    6 -> R.drawable.hangman_3
                    5 -> R.drawable.hangman_4
                    4 -> R.drawable.hangman_5
                    3 -> R.drawable.hangman_6
                    2 -> R.drawable.hangman_7
                    else -> R.drawable.hangman_8
                }
            }
            else -> {
                when (remainingAttempts) {
                    4 -> R.drawable.hangman_3
                    3 -> R.drawable.hangman_4
                    2 -> R.drawable.hangman_6
                    else -> R.drawable.hangman_8
                }
            }
        }),
        contentDescription = "",
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
    )
    Image(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        painter = painterResource(id = when(totalAttempts) {
            8 -> {
                when (remainingAttempts) {
                    8 -> R.drawable.hangman_0
                    7 -> R.drawable.hangman_1
                    6 -> R.drawable.hangman_2
                    5 -> R.drawable.hangman_3
                    4 -> R.drawable.hangman_4
                    3 -> R.drawable.hangman_5
                    2 -> R.drawable.hangman_6
                    1 -> R.drawable.hangman_7
                    else -> R.drawable.hangman_8
                }
            }
            6 -> {
                when (remainingAttempts) {
                    6 -> R.drawable.hangman_2
                    5 -> R.drawable.hangman_3
                    4 -> R.drawable.hangman_4
                    3 -> R.drawable.hangman_5
                    2 -> R.drawable.hangman_6
                    1 -> R.drawable.hangman_7
                    else -> R.drawable.hangman_8
                }
            }
            else -> {
                when (remainingAttempts) {
                    4 -> R.drawable.hangman_2
                    3 -> R.drawable.hangman_3
                    2 -> R.drawable.hangman_4
                    1 -> R.drawable.hangman_6
                    else -> R.drawable.hangman_8
                }
            }
        }),
        contentDescription = "",
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
    )
}

@Composable
fun Lettre(char: Char, letterStates : MutableList<LetterState>, voyelles : List<Char>, won : Won, sizes: Sizes, click : (Char) -> Unit) {
    val lettrePos = char - 'A'
    Box(modifier = Modifier
        .size(sizes.letterSizeDp.dp)
        .then(
            if (letterStates[lettrePos] != UNKNOWN)
                Modifier.clip(RoundedCornerShape(4.dp))
            else
                Modifier.border(
                    width = if (voyelles.contains(char)) 2.dp else 1.dp,
                    brush = SolidColor(MaterialTheme.colorScheme.onSurface),
                    shape = RoundedCornerShape(4.dp)
                )
        )
        .background(
            color =
            when (letterStates[lettrePos]) {
                UNKNOWN -> Color.Transparent
                WRONG -> MaterialTheme.colorScheme.error
                CORRECT -> MaterialTheme.colorScheme.tertiaryContainer
                HINT_ABSENT, HINT_PRESENT -> MaterialTheme.colorScheme.outlineVariant
            }
        )
        .clickable(enabled = won == Won.NOT_WON) { click(char) },
        contentAlignment = Alignment.TopCenter
    ) {
        Text(
            text = char.toString(),
            color = if (letterStates[lettrePos] == UNKNOWN) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface,
            textAlign = TextAlign.Center,
            fontSize = sizes.letterSizeSp.sp,
        )
        if (letterStates[lettrePos] == HINT_ABSENT) {
            Image(
                painter = painterResource(id = R.drawable.barred),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
        }
    }
}

@Composable
fun Flag(modifier: Modifier = Modifier, dictionary: String, fontSize: TextUnit) {
    val flag = when {
        dictionary.startsWith("fr") -> "\uD83C\uDDEB\uD83C\uDDF7"
        dictionary.startsWith("en") -> "\uD83C\uDDEC\uD83C\uDDE7"
        else -> ""
    }

    Text(
        modifier = modifier
            .alpha(.5f),
        text = flag,
        fontSize = fontSize
    )
}


@Composable
fun SettingsButton(sizes: Sizes, click: () -> Unit) {
    Image(painter = painterResource(id = R.drawable.settings),
        contentDescription = null,
        modifier = Modifier
            .width((2.3 * sizes.letterSizeDp).dp)
            .height(sizes.letterSizeDp.dp)
            .border(
                1.dp,
                MaterialTheme.colorScheme.primaryContainer,
                RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                click()
            }
            .padding(vertical = (sizes.letterSizeDp / 8).dp),
        colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primaryContainer),
        contentScale = ContentScale.Fit
    )
}

@Composable
fun ResetButton(sizes: Sizes, click: () -> Unit) {
    Image(painter = painterResource(id = R.drawable.reset),
        contentDescription = null,
        modifier = Modifier
            .width((2.3 * sizes.letterSizeDp).dp)
            .height(sizes.letterSizeDp.dp)
            .border(1.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(color = MaterialTheme.colorScheme.tertiary)
            .clickable {
                click()
            }
            .padding(vertical = (sizes.letterSizeDp / 8).dp),
        colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onError),
        contentScale = ContentScale.Fit
    )
}

@Composable
fun HintButton(sizes: Sizes, enabled: Boolean, click: () -> Unit) {
    Image(painter = painterResource(id = R.drawable.hint),
        contentDescription = null,
        modifier = Modifier
            .alpha(if (enabled) 1f else .5f)
            .width((2.3 * sizes.letterSizeDp).dp)
            .height(sizes.letterSizeDp.dp)
            .border(1.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(color = MaterialTheme.colorScheme.outlineVariant)
            .then(
                if (enabled)
                    Modifier.clickable {
                        click.invoke()
                    }
                else
                    Modifier
            )
            .padding(vertical = (sizes.letterSizeDp / 8).dp),
        colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onError),
        contentScale = ContentScale.Fit
    )
}



data class Sizes(
    val landscape: Boolean,
    val letterSizeDp: Float,
    val letterSizeSp: Float,
    val motLetterSizeDp: Float,
    val motLetterSizeSp: Float,
    val hangmanWidthDp: Float,
    val hangmanHeightDp: Float,
)


@Preview(device = "spec:width=1920px,height=1080px,dpi=300")
@Composable
fun PlateauPreview() {
    PenduTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
            Plateau()
        }
    }
}
