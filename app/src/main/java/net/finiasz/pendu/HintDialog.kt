package net.finiasz.pendu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import net.finiasz.pendu.ui.theme.PenduTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HintDialog(viewModel: GameViewModel, state: GameState, dismissCallback: () -> Unit) {
    val unusedExists = state.unusedLetterExists()

    AlertDialog(
        onDismissRequest = {
            dismissCallback.invoke()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .wrapContentWidth()
            .wrapContentHeight()
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {

            // reveal a letter
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text (
                    modifier = Modifier.weight(1f, true),
                    text = stringResource(R.string.reveal_letter),
                    fontSize = TextUnit(24f, TextUnitType.Sp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = {
                        viewModel.revealClick()
                        dismissCallback.invoke()
                    },
                    enabled = state.lettresManquantes > 0,
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text(
                        text = stringResource(id = R.string.ok),
                        fontSize = TextUnit(24f, TextUnitType.Sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }


            // discard a letter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text (
                    modifier = Modifier.weight(1f, true),
                    text = stringResource(R.string.discard_letter),
                    fontSize = TextUnit(24f, TextUnitType.Sp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    modifier = Modifier
                        .alpha(if (unusedExists) 1f else .5f),
                    onClick = {
                        viewModel.discardClick()
                        dismissCallback.invoke()
                    },
                    enabled = unusedExists,
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Text(
                        text = stringResource(id = R.string.ok),
                        fontSize = TextUnit(24f, TextUnitType.Sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }



            // cancel button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        dismissCallback.invoke()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outline)
                ) {
                    Text(
                        text = stringResource(id = R.string.cancel),
                        fontSize = TextUnit(24f, TextUnitType.Sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun HintPreview() {
    PenduTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
            HintDialog(viewModel = GameViewModel(),state = GameState()) {
            }
        }
    }
}