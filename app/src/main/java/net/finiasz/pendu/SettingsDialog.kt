package net.finiasz.pendu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.finiasz.pendu.ui.theme.PenduTheme
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(settingsManager: SettingsManager, dismissCallback: () -> Unit) {
    var dictDropDownExpanded by remember { mutableStateOf(false) }

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

            Column(
                modifier = Modifier
                    .weight(1f, false)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {


                // dictionnaire
                ExposedDropdownMenuBox(
                    expanded = dictDropDownExpanded,
                    onExpandedChange = {
                        dictDropDownExpanded = !dictDropDownExpanded
                    }
                ) {
                    CompositionLocalProvider (
                        LocalTextInputService provides null
                    ) {
                        OutlinedTextField(
                            value =  SettingsManager.dictionaries[settingsManager.dictionary.value] ?: "-",
                            label = { Text(
                                text = stringResource(id = R.string.label_dictionary),
                                fontSize = 16.sp,
                            )},
                            textStyle = TextStyle(
                                fontSize = 24.sp,
                            ),
                            onValueChange = {},
                            readOnly = true,
                            shape = RoundedCornerShape(16.dp),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dictDropDownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                    }

                    ExposedDropdownMenu(
                        expanded = dictDropDownExpanded,
                        onDismissRequest = { dictDropDownExpanded = false },
                    ) {
                        SettingsManager.dictionaries.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(
                                    modifier = Modifier.padding(8.dp),
                                    text = item.value,
                                    fontSize = 24.sp,
                                )},
                                onClick = {
                                    settingsManager.setDictionary(item.key)
                                    dictDropDownExpanded = false
                                    CoroutineScope(Dispatchers.IO).launch {
                                        settingsManager.countWords()
                                    }
                                }
                            )
                        }
                    }
                }


                // min length
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f, true),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Switch(
                                checked = settingsManager.minLength.value != 0,
                                onCheckedChange = {
                                    settingsManager.setMinLength(if (it) 5 else 0)
                                    CoroutineScope(Dispatchers.IO).launch {
                                        settingsManager.countWords()
                                    }
                                },
                                modifier = Modifier.padding(end = 16.dp),
                                colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.outlineVariant)
                            )
                            Text(
                                text = stringResource(id = R.string.settings_min_length),
                                fontSize = 24.sp,
                                modifier = Modifier.weight(weight = 1f, fill = true)
                            )
                        }
                        AnimatedVisibility(visible = settingsManager.minLength.value != 0) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                            ) {
                                Slider(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = (settingsManager.minLength.value - 3) / 7f,
                                    onValueChange = {
                                        settingsManager.setMinLength((it * 7 + 3).roundToInt())
                                    },
                                    onValueChangeFinished = {
                                        if (settingsManager.maxLength.value != 0 && settingsManager.maxLength.value < settingsManager.minLength.value) {
                                            settingsManager.setMaxLength(settingsManager.minLength.value)
                                        }
                                        CoroutineScope(Dispatchers.IO).launch {
                                            settingsManager.countWords()
                                        }
                                    },
                                    steps = 6,
                                    colors = SliderDefaults.colors(
                                        activeTickColor = MaterialTheme.colorScheme.outline,
                                        inactiveTickColor = MaterialTheme.colorScheme.outline,
                                        inactiveTrackColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                            }
                        }
                    }
                    Column {
                        AnimatedVisibility(visible = settingsManager.minLength.value != 0) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 16.dp, top = 8.dp)
                                    .width(72.dp)
                                    .height(80.dp)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = settingsManager.minLength.value.toString(),
                                    fontSize = 42.sp,
                                )
                            }
                        }
                    }
                }

                // max length
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f, true),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Switch(
                                checked = settingsManager.maxLength.value != 0,
                                onCheckedChange = {
                                    settingsManager.setMaxLength(if (it) 10 else 0)
                                    CoroutineScope(Dispatchers.IO).launch {
                                        settingsManager.countWords()
                                    }
                                },
                                modifier = Modifier.padding(end = 16.dp),
                                colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.outlineVariant)
                            )
                            Text(
                                text = stringResource(id = R.string.settings_max_length),
                                fontSize = 24.sp,
                                modifier = Modifier.weight(weight = 1f, fill = true)
                            )
                        }
                        AnimatedVisibility(visible = settingsManager.maxLength.value != 0) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                            ) {
                                Slider(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = (settingsManager.maxLength.value - 5) / 15f,
                                    onValueChange = {
                                        settingsManager.setMaxLength((it * 15 + 5).roundToInt())
                                    },
                                    onValueChangeFinished = {
                                        if (settingsManager.maxLength.value < settingsManager.minLength.value) {
                                            settingsManager.setMinLength(settingsManager.maxLength.value)
                                        }
                                        CoroutineScope(Dispatchers.IO).launch {
                                            settingsManager.countWords()
                                        }
                                    },
                                    steps = 14,
                                    colors = SliderDefaults.colors(
                                        activeTickColor = MaterialTheme.colorScheme.outline,
                                        inactiveTickColor = MaterialTheme.colorScheme.outline,
                                        inactiveTrackColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                            }
                        }
                    }
                    Column {
                        AnimatedVisibility(visible = settingsManager.maxLength.value != 0) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 16.dp, top = 8.dp)
                                    .width(72.dp)
                                    .height(80.dp)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = settingsManager.maxLength.value.toString(),
                                    fontSize = 42.sp,
                                )
                            }
                        }
                    }
                }



                // word count
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        modifier = Modifier.weight(1f, true),
                        text = stringResource(id = R.string.label_words),
                        fontSize = 20.sp,
                    )
                    Text(
                        text = settingsManager.wordsCount.value?.toString() ?: "-",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }




                // first letter
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Switch(
                        checked = (settingsManager.freeLetters.value and 1) != 0,
                        onCheckedChange = { settingsManager.setFreeLetters((settingsManager.freeLetters.value and 2) or if (it) 1 else 0) },
                        modifier = Modifier.padding(end = 16.dp),
                        colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.outlineVariant)
                    )
                    Text(
                        text = stringResource(id = R.string.settings_show_first_letter),
                        fontSize = 24.sp,
                        modifier = Modifier.weight(weight = 1f, fill = true)
                    )
                }

                // last letter
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Switch(
                        checked = (settingsManager.freeLetters.value and 2) != 0,
                        onCheckedChange = { settingsManager.setFreeLetters((settingsManager.freeLetters.value and 1) or if (it) 2 else 0) },
                        modifier = Modifier.padding(end = 16.dp),
                        colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.outlineVariant)
                    )
                    Text(
                        text = stringResource(id = R.string.settings_show_last_letter),
                        fontSize = 24.sp,
                        modifier = Modifier.weight(weight = 1f, fill = true)
                    )
                }

                // discard letters
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f, true),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Switch(
                                checked = settingsManager.discardLetters.value != 0,
                                onCheckedChange = {
                                    settingsManager.setDiscardLetters(if (it) 5 else 0)
                                },
                                modifier = Modifier.padding(end = 16.dp),
                                colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.outlineVariant)
                            )
                            Text(
                                text = stringResource(id = R.string.settings_discard_letters),
                                fontSize = 24.sp,
                                modifier = Modifier.weight(weight = 1f, fill = true)
                            )
                        }
                        AnimatedVisibility(visible = settingsManager.discardLetters.value != 0) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                            ) {
                                Slider(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = (settingsManager.discardLetters.value - 1) / 9f,
                                    onValueChange = {
                                        settingsManager.setDiscardLetters((it * 9 + 1).roundToInt())
                                    },
                                    steps = 8,
                                    colors = SliderDefaults.colors(
                                        activeTickColor = MaterialTheme.colorScheme.outline,
                                        inactiveTickColor = MaterialTheme.colorScheme.outline,
                                        inactiveTrackColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                            }
                        }
                    }
                    Column {
                        AnimatedVisibility(visible = settingsManager.discardLetters.value != 0) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 16.dp, top = 8.dp)
                                    .width(72.dp)
                                    .height(80.dp)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = settingsManager.discardLetters.value.toString(),
                                    fontSize = 42.sp,
                                )
                            }
                        }
                    }
                }



                // number of attempts
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.weight(1f, true)
                    ) {
                        Text(
                            text = stringResource(id = R.string.settings_number_of_attempts),
                            fontSize = 24.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Slider(
                            modifier = Modifier.fillMaxWidth(),
                            value = (settingsManager.totalAttempts.value - 4) / 4f,
                            onValueChange = {
                                settingsManager.setTotalAttempts((it * 4 + 4).roundToInt())
                            },
                            steps = 1,
                            colors = SliderDefaults.colors(
                                activeTickColor = MaterialTheme.colorScheme.outline,
                                inactiveTickColor = MaterialTheme.colorScheme.outline,
                                inactiveTrackColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .width(72.dp)
                            .height(80.dp)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = settingsManager.totalAttempts.value.toString(),
                            fontSize = 42.sp,
                        )
                    }

                }

            }


            // ok button
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(
                        text = stringResource(id = R.string.ok),
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun SettingsPreview() {
    PenduTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
            SettingsDialog(settingsManager = SettingsManager(LocalContext.current)) {
            }
        }
    }
}