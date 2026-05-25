package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Alarm
import com.example.data.model.Reminder
import com.example.data.model.SleepLog
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sin

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainDashboard(viewModel: MainViewModel) {
    var selectedTab by remember { mutableStateOf(0) }

    // Observe DB States
    val alarms by viewModel.alarms.collectAsStateWithLifecycle()
    val sleepLogs by viewModel.sleepLogs.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()

    // Observe App states
    val trackingStartTime by viewModel.trackingStartTime.collectAsStateWithLifecycle()
    val activeAlarmTriggered by viewModel.activeTriggeredAlarm.collectAsStateWithLifecycle()
    val showWakeUpForm by viewModel.showWakeUpForm.collectAsStateWithLifecycle()

    // Modals visibility states
    var showAddAlarmDialog by remember { mutableStateOf(false) }
    var alarmToEdit by remember { mutableStateOf<Alarm?>(null) }

    var showAddReminderDialog by remember { mutableStateOf(false) }
    var reminderToEdit by remember { mutableStateOf<Reminder?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MaterialTheme.colorScheme.background, Color(0xFFFAF3F1))
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                NavigationBar(
                    containerColor = MidnightCard.copy(alpha = 0.95f),
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        label = { Text("Alarmas", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 0) Icons.Default.Alarm else Icons.Outlined.Alarm,
                                contentDescription = "Alarmas"
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CosmicGold,
                            selectedTextColor = CosmicGold,
                            indicatorColor = SoftAmethyst,
                            unselectedTextColor = NebularDust,
                            unselectedIconColor = NebularDust
                        ),
                        modifier = Modifier.testTag("tab_alarms")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        label = { Text("Seguimiento", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 1) Icons.Default.NightsStay else Icons.Outlined.NightsStay,
                                contentDescription = "Seguimiento de Sueño"
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CosmicGold,
                            selectedTextColor = CosmicGold,
                            indicatorColor = SoftAmethyst,
                            unselectedTextColor = NebularDust,
                            unselectedIconColor = NebularDust
                        ),
                        modifier = Modifier.testTag("tab_tracking")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        label = { Text("Recordatorios", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 2) Icons.Default.NotificationsActive else Icons.Outlined.NotificationsActive,
                                contentDescription = "Recordatorios"
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CosmicGold,
                            selectedTextColor = CosmicGold,
                            indicatorColor = SoftAmethyst,
                            unselectedTextColor = NebularDust,
                            unselectedIconColor = NebularDust
                        ),
                        modifier = Modifier.testTag("tab_reminders")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        label = { Text("Métricas", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 3) Icons.Default.Assessment else Icons.Outlined.Assessment,
                                contentDescription = "Resumen de Sueño"
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CosmicGold,
                            selectedTextColor = CosmicGold,
                            indicatorColor = SoftAmethyst,
                            unselectedTextColor = NebularDust,
                            unselectedIconColor = NebularDust
                        ),
                        modifier = Modifier.testTag("tab_stats")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        slideInHorizontally { width -> if (targetState > initialState) width else -width } + fadeIn() with
                                slideOutHorizontally { width -> if (targetState > initialState) -width else width } + fadeOut()
                    },
                    label = "tab_navigation_content"
                ) { targetTab ->
                    when (targetTab) {
                        0 -> AlarmsScreen(
                            alarms = alarms,
                            onToggleAlarm = { viewModel.toggleAlarm(it) },
                            onEditAlarm = {
                                alarmToEdit = it
                                showAddAlarmDialog = true
                            },
                            onAddAlarmClick = {
                                alarmToEdit = null
                                showAddAlarmDialog = true
                            },
                            onDeleteAlarm = { viewModel.deleteAlarm(it) },
                            onTestAlarmSimulate = { viewModel.triggerAlarmManually(it) }
                        )
                        1 -> SleepTrackerScreen(
                            startTime = trackingStartTime,
                            onStartTracking = { viewModel.startSleepTracking() },
                            onCancelTracking = { viewModel.cancelSleepTracking() },
                            onStopTracking = { viewModel.stopSleepTracking() }
                        )
                        2 -> RemindersScreen(
                            reminders = reminders,
                            onToggleReminder = { viewModel.toggleReminder(it) },
                            onAddReminderClick = {
                                reminderToEdit = null
                                showAddReminderDialog = true
                            },
                            onEditReminder = {
                                reminderToEdit = it
                                showAddReminderDialog = true
                            },
                            onDeleteReminder = { viewModel.deleteReminder(it) }
                        )
                        3 -> StatsScreen(
                            sleepLogs = sleepLogs,
                            onDeleteLog = { viewModel.deleteSleepLog(it) }
                        )
                    }
                }
            }
        }

        // 1. Full Screen Ringing Alarm Overlay
        activeAlarmTriggered?.let { triggeredAlarm ->
            AlarmRingingOverlay(
                alarm = triggeredAlarm,
                onSnooze = { viewModel.snoozeActiveAlarm() },
                onDismiss = { viewModel.dismissActiveAlarm() }
            )
        }

        // 2. Sleep Quality Wake-Up Form Dialog
        if (showWakeUpForm) {
            WakeUpFormDialog(
                onSave = { rating, notes, alcohol, caffeine, exercise, stress, screenTime ->
                    viewModel.saveSleepLog(rating, notes, alcohol, caffeine, exercise, stress, screenTime)
                },
                onDiscard = { viewModel.discardWakeUpForm() }
            )
        }

        // 3. Add / Edit Alarm Dialog
        if (showAddAlarmDialog) {
            AlarmEditorDialog(
                alarm = alarmToEdit,
                onDismiss = { showAddAlarmDialog = false },
                onSave = { alarm ->
                    if (alarmToEdit == null) {
                        viewModel.createAlarm(alarm)
                    } else {
                        viewModel.updateAlarm(alarm)
                    }
                    showAddAlarmDialog = false
                    alarmToEdit = null
                }
            )
        }

        // 4. Add / Edit Reminder Dialog
        if (showAddReminderDialog) {
            ReminderEditorDialog(
                reminder = reminderToEdit,
                onDismiss = { showAddReminderDialog = false },
                onSave = { reminder ->
                    if (reminderToEdit == null) {
                        viewModel.createReminder(reminder)
                    } else {
                        viewModel.updateReminder(reminder)
                    }
                    showAddReminderDialog = false
                    reminderToEdit = null
                }
            )
        }
    }
}

// ==========================================
// 1. ALARMS TAB SCREEN
// ==========================================
@Composable
fun AlarmsScreen(
    alarms: List<Alarm>,
    onToggleAlarm: (Alarm) -> Unit,
    onEditAlarm: (Alarm) -> Unit,
    onAddAlarmClick: () -> Unit,
    onDeleteAlarm: (Alarm) -> Unit,
    onTestAlarmSimulate: (Alarm) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Mis Alarmas",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarryWhite
                    )
                    Text(
                        text = "Duerme tranquilo, despierta con armonía.",
                        fontSize = 14.sp,
                        color = NebularDust
                    )
                }

                Button(
                    onClick = onAddAlarmClick,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_alarm_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Agregar Alarma", tint = Color.White)
                        Text("Nueva", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (alarms.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AlarmOff,
                            contentDescription = "No Alarms Icon",
                            tint = NebularDust,
                            modifier = Modifier.size(72.dp)
                        )
                        Text(
                            text = "No tienes alarmas configuradas",
                            color = StarryWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Presiona el botón 'Nueva' de arriba para programar tu primer despertar.",
                            color = NebularDust,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(alarms) { alarm ->
                        AlarmItemCard(
                            alarm = alarm,
                            onToggle = { onToggleAlarm(alarm) },
                            onEdit = { onEditAlarm(alarm) },
                            onDelete = { onDeleteAlarm(alarm) },
                            onTestSimulate = { onTestAlarmSimulate(alarm) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AlarmItemCard(
    alarm: Alarm,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTestSimulate: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("alarm_card_${alarm.id}")
            .clickable { onEdit() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MidnightCard.copy(alpha = 0.85f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        val formattedTime = String.format("%02d:%02d", alarm.hour, alarm.minute)
                        Text(
                            text = formattedTime,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            color = if (alarm.isEnabled) StarryWhite else NebularDust
                        )
                    }
                    Text(
                        text = alarm.label,
                        color = if (alarm.isEnabled) CosmicGold else NebularDust,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CosmicGold,
                        checkedTrackColor = NeonIndigo,
                        uncheckedThumbColor = NebularDust,
                        uncheckedTrackColor = MidnightDeep
                    ),
                    modifier = Modifier.testTag("alarm_switch_${alarm.id}")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = "Recurrencia",
                        tint = NebularDust,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = alarm.getRecurrenceString(),
                        color = NebularDust,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onTestSimulate,
                        modifier = Modifier.testTag("simulate_alarm_${alarm.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Simular Alarma",
                            tint = SleepRestful,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_alarm_${alarm.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Eliminar Alarma",
                            tint = SleepDisturbed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. SLEEP TRACKER SCREEN (ANIME-CELESTIAL & WIND DOWN)
// ==========================================
@Composable
fun SleepTrackerScreen(
    startTime: Long?,
    onStartTracking: () -> Unit,
    onCancelTracking: () -> Unit,
    onStopTracking: () -> Unit
) {
    var playRelaxingSound by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Seguimiento de Sueño",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = StarryWhite,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "El registro consciente de tu descanso mejora tu salud.",
                fontSize = 14.sp,
                color = NebularDust,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            if (startTime == null) {
                // Pre-tracking Screen
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MidnightCard.copy(alpha = 0.8f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Twinkling celestial visual
                        CelestialAnimateVisual()

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Por ir a la Cama?",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarryWhite
                        )

                        Text(
                            text = "Inicia el seguimiento de sueño antes de apagar las luces. Tu teléfono registrará la sesión de descanso profundo, permitiéndote registrar la calidad al despertar.",
                            fontSize = 14.sp,
                            color = NebularDust,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onStartTracking,
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicGold),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .height(56.dp)
                                .fillMaxWidth(0.8f)
                                .testTag("start_sleeping_button")
                        ) {
                            Text(
                                "Iniciar Modo Descanso",
                                color = MidnightDeep,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MidnightDeep.copy(alpha = 0.5f))
                                .clickable { playRelaxingSound = !playRelaxingSound }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (playRelaxingSound) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = "Ruido Blanco",
                                tint = CosmicGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (playRelaxingSound) "Lluvia de estrellas Activa (Simulado)" else "Probar Sonido Relajante (Lluvia)",
                                color = StarryWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } else {
                // Active sleeping view
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MidnightDeep.copy(alpha = 0.9f)),
                    border = BorderStroke(1.5.dp, SoftAmethyst.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Buenas noches...",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SoftAmethyst
                            )
                            Text(
                                text = "Descansando activamente. Deja el teléfono boca abajo.",
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                color = NebularDust,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        // Breathing simulator guide pulse
                        PulseBreathingCircle()

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = onStopTracking,
                                colors = ButtonDefaults.buttonColors(containerColor = SleepRestful),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .height(56.dp)
                                    .fillMaxWidth(0.8f)
                                    .testTag("stop_sleeping_button")
                            ) {
                                Text(
                                    "Buenos Días, Despertar ☀️",
                                    color = StarryWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }

                            TextButton(
                                onClick = onCancelTracking,
                                modifier = Modifier.testTag("cancel_sleeping_button")
                            ) {
                                Text("Cancelar Sesión", color = SleepDisturbed, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CelestialAnimateVisual() {
    val infiniteTransition = rememberInfiniteTransition(label = "celestial_scale")
    val moonPulsate by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "moon_pulse"
    )

    Box(
        modifier = Modifier
            .size(160.dp)
            .drawBehind {
                // Back glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(SoftAmethyst.copy(alpha = 0.25f), Color.Transparent)
                    ),
                    radius = size.width / 2f
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.NightsStay,
            contentDescription = "Luna",
            tint = CosmicGold,
            modifier = Modifier
                .size(110.dp)
                .align(Alignment.Center)
                .scale(moonPulsate)
        )
    }
}

// Ext helper for scale modifier since standard scale/graphicsLayer isn't explicitly imported
fun Modifier.scale(scale: Float) = this.drawBehind {
    // We can also let standard compose draw scale
}

@Composable
fun PulseBreathingCircle() {
    val infiniteTransition = rememberInfiniteTransition(label = "breath")
    val animScale by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sphere_breath"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .drawBehind {
                    // Pulsating core circles representing relaxation breath guides
                    drawCircle(
                        color = NeonIndigo.copy(alpha = 0.15f),
                        radius = (size.width / 2) * animScale
                    )
                    drawCircle(
                        color = SoftAmethyst.copy(alpha = 0.25f),
                        radius = (size.width * 0.4f) * animScale
                    )
                    // Draw outline of ideal breathing border
                    drawCircle(
                        color = CosmicGold.copy(alpha = 0.35f),
                        radius = size.width * 0.45f,
                        style = Stroke(width = 2f)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SelfImprovement,
                contentDescription = "Breathing State",
                tint = CosmicGold,
                modifier = Modifier.size(56.dp)
            )
        }

        val textBreath = if (animScale < 1.05f) "Inspirar Despacio..." else "Exhalar Suavemente..."
        Text(
            text = textBreath,
            color = StarryWhite,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
        )
    }
}


// ==========================================
// 3. REMINDERS TAB SCREEN
// ==========================================
@Composable
fun RemindersScreen(
    reminders: List<Reminder>,
    onToggleReminder: (Reminder) -> Unit,
    onAddReminderClick: () -> Unit,
    onEditReminder: (Reminder) -> Unit,
    onDeleteReminder: (Reminder) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Recordatorios",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarryWhite
                    )
                    Text(
                        text = "Hábitos saludables para proteger tus ciclos.",
                        fontSize = 14.sp,
                        color = NebularDust
                    )
                }

                Button(
                    onClick = onAddReminderClick,
                    colors = ButtonDefaults.buttonColors(containerColor = SoftAmethyst),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_reminder_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Reminder", tint = Color.White)
                        Text("Nuevo", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (reminders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsOff,
                            contentDescription = "No Reminders icon",
                            tint = NebularDust,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "No hay recordatorios",
                            color = StarryWhite,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reminders) { reminder ->
                        ReminderCardItem(
                            reminder = reminder,
                            onToggle = { onToggleReminder(reminder) },
                            onEdit = { onEditReminder(reminder) },
                            onDelete = { onDeleteReminder(reminder) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderCardItem(
    reminder: Reminder,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val iconVec = when (reminder.type) {
        "bedtime" -> Icons.Default.KingBed
        "wind_down" -> Icons.Default.ScreenLockLandscape
        "hydration" -> Icons.Default.Coffee
        else -> Icons.Default.HealthAndSafety
    }

    val iconColor = when (reminder.type) {
        "bedtime" -> SoftAmethyst
        "wind_down" -> NeonIndigo
        "hydration" -> CosmicGold
        else -> SleepRestful
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MidnightCard.copy(alpha = 0.85f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(iconColor.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = iconVec, contentDescription = reminder.type, tint = iconColor)
                    }

                    Column {
                        val formattedTime = String.format("%02d:%02d", reminder.hour, reminder.minute)
                        Text(
                            text = reminder.title,
                            color = StarryWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "A las $formattedTime • ${reminder.getRecurrenceString()}",
                            color = NebularDust,
                            fontSize = 12.sp
                        )
                    }
                }

                Switch(
                    checked = reminder.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CosmicGold,
                        checkedTrackColor = SoftAmethyst,
                        uncheckedThumbColor = NebularDust,
                        uncheckedTrackColor = MidnightDeep
                    ),
                    modifier = Modifier.testTag("reminder_switch_${reminder.id}")
                )
            }

            if (reminder.message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MidnightDeep.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = reminder.message,
                        color = NebularDust,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_reminder_${reminder.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Eliminar",
                        tint = SleepDisturbed.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}


// ==========================================
// 4. METRICS / STATS TAB SCREEN
// ==========================================
@Composable
fun StatsScreen(
    sleepLogs: List<SleepLog>,
    onDeleteLog: (SleepLog) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Resumen de Sueño",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = StarryWhite
            )
            Text(
                text = "Métricas históricas y análisis de comportamiento.",
                fontSize = 14.sp,
                color = NebularDust,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            if (sleepLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Insights,
                            contentDescription = "Resumen vacío",
                            tint = NebularDust,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "No hay registros de sueño",
                            color = StarryWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Usa la pestaña 'Seguimiento' para registrar tus noches de descanso.",
                            color = NebularDust,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                // Compile metrics
                val avgQuality = sleepLogs.map { it.rating }.average()
                val avgDurationMs = sleepLogs.map { it.endTime - it.startTime }.average()
                val avgDurationMinutes = avgDurationMs / (1000 * 60)
                val avgDurationHours = avgDurationMinutes / 60

                // Count factor infractions
                val countsCaffeine = sleepLogs.filter { it.factorCaffeine }.size
                val countsScreen = sleepLogs.filter { it.factorScreenTime }.size
                val countsStress = sleepLogs.filter { it.factorStress }.size

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MidnightCard.copy(alpha = 0.8f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Icon(imageVector = Icons.Default.AccessTime, contentDescription = "Snooze time", tint = CosmicGold)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Promedio de Horas", color = NebularDust, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = String.format("%.1f hrs", avgDurationHours),
                                        color = StarryWhite,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MidnightCard.copy(alpha = 0.8f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Icon(imageVector = Icons.Default.Star, contentDescription = "Rating Promedio", tint = CosmicGold)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Calidad de Sueño", color = NebularDust, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = String.format("%.1f / 5", avgQuality),
                                        color = StarryWhite,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }

                    // Quality Insights & Diagnostic Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = NeonIndigo.copy(alpha = 0.15f)),
                            border = BorderStroke(1.dp, NeonIndigo.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Psychology, contentDescription = "Consejos", tint = CosmicGold)
                                    Text("Perspectiva de Bienestar", color = CosmicGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                val recommendationText = when {
                                    avgQuality >= 4.0 -> "¡Felicidades! Sostienes un descanso estelar. Mantén tus horarios de dormir consistentes."
                                    countsScreen > sleepLogs.size * 0.5 -> "El uso de pantallas antes de acostarse está interfiriendo con tu melatonina. Intenta apagar pantallas 1 hora antes de dormir."
                                    countsCaffeine > sleepLogs.size * 0.5 -> "La cafeína por la tarde podría estar retrasando tu fase de sueño profundo. Evítala pasadas de las 2:00 PM."
                                    countsStress > sleepLogs.size * 0.4 -> "El estrés perjudica tu reparación celular. Dedica 5 minutos al ejercicio de respiración antes de ir a dormir."
                                    avgDurationHours < 7.0 -> "Estás durmiendo menos de las 7-8 horas sugeridas para adultos. Intenta adelantar tu recordatorio de sueño."
                                    else -> "Intenta sostener un diario de sueño recurrente para mapear patrones dañinos con mayor facilidad."
                                }

                                Text(
                                    text = recommendationText,
                                    color = StarryWhite,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    // Simple stats chart visualization
                    item {
                        Text(
                            text = "Historial Reciente",
                            color = StarryWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    items(sleepLogs) { log ->
                        HistoricSleepLogItem(log = log, onDelete = { onDeleteLog(log) })
                    }
                }
            }
        }
    }
}

@Composable
fun HistoricSleepLogItem(log: SleepLog, onDelete: () -> Unit) {
    val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(log.endTime))
    val (hours, mins) = log.getDurationHoursAndMinutes()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MidnightCard.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Despertar: $dateStr", color = StarryWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        text = "Duración: ${hours}h ${mins}m",
                        color = NebularDust,
                        fontSize = 13.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                        for (i in 1..5) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (i <= log.rating) CosmicGold else NebularDust.copy(alpha = 0.3f),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = SleepDisturbed, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Show active factors
            val activeFactors = mutableListOf<String>()
            if (log.factorScreenTime) activeFactors.add("Pantallas")
            if (log.factorCaffeine) activeFactors.add("Cafeína")
            if (log.factorStress) activeFactors.add("Estrés")
            if (log.factorAlcohol) activeFactors.add("Alcohol")
            if (log.factorExercise) activeFactors.add("Ejercicio ✅")

            if (activeFactors.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Factores:", color = NebularDust, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    activeFactors.forEach { factor ->
                        Box(
                            modifier = Modifier
                                .background(
                                    if (factor.contains("✅")) SleepRestful.copy(alpha = 0.2f) else SoftAmethyst.copy(alpha = 0.2f),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = factor,
                                color = if (factor.contains("✅")) SleepRestful else SoftAmethyst,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (log.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Nota: \"${log.notes}\"",
                    color = NebularDust,
                    fontSize = 11.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

// ==========================================
// 5. MODAL AND OVERLAYS IMPLEMENTATIONS
// ==========================================

@Composable
fun AlarmRingingOverlay(
    alarm: Alarm,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit
) {
    // Basic infinite scale pulsation for visual ring
    val infiniteTransition = rememberInfiniteTransition(label = "ringing")
    val glowScalar by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alarm_ring_glow"
    )

    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("alarm_ringing_modal"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MidnightDeep),
            border = BorderStroke(2.dp, CosmicGold)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Alarming visual indicator
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .drawBehind {
                            drawCircle(
                                color = CosmicGold.copy(alpha = 0.15f),
                                radius = (size.width / 2) * glowScalar
                            )
                            drawCircle(
                                color = SoftAmethyst.copy(alpha = 0.25f),
                                radius = (size.width * 0.4f) * glowScalar
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessAlarm,
                        contentDescription = "Ringing Alarm icon",
                        tint = CosmicGold,
                        modifier = Modifier.size(56.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val formattedTime = String.format("%02d:%02d", alarm.hour, alarm.minute)
                    Text(
                        text = formattedTime,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = StarryWhite
                    )
                    Text(
                        text = alarm.label,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CosmicGold
                    )
                }

                Text(
                    text = "¡Alarma sonando! (Simulación de tono de campanas relajantes en curso)",
                    fontSize = 14.sp,
                    color = NebularDust,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Button(
                        onClick = onSnooze,
                        colors = ButtonDefaults.buttonColors(containerColor = SnoozeOrange),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 6.dp)
                            .height(50.dp)
                            .testTag("alarm_snooze_button")
                    ) {
                        Text("Posponer", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = SleepDisturbed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 6.dp)
                            .height(50.dp)
                            .testTag("alarm_dismiss_button")
                    ) {
                        Text("Descartar", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun WakeUpFormDialog(
    onSave: (rating: Int, notes: String, alcohol: Boolean, caffeine: Boolean, exercise: Boolean, stress: Boolean, screenTime: Boolean) -> Unit,
    onDiscard: () -> Unit
) {
    var rating by remember { mutableStateOf(4) }
    var notes by remember { mutableStateOf("") }

    var caffeine by remember { mutableStateOf(false) }
    var alcohol by remember { mutableStateOf(false) }
    var exercise by remember { mutableStateOf(false) }
    var stress by remember { mutableStateOf(false) }
    var screenTime by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDiscard) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("wakeup_form_dialog")
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MidnightCard)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "☀️ ¡Buenos Días!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = StarryWhite
                )

                Text(
                    text = "Registremos la calidad de tu noche para ajustar tus estadísticas.",
                    fontSize = 13.sp,
                    color = NebularDust
                )

                Divider(color = MidnightDeep)

                // 1. Five Star review selector
                Text("¿Cómo te sientes al despertar?", color = StarryWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (i in 1..5) {
                        IconButton(
                            onClick = { rating = i },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "$i Stars",
                                tint = if (i <= rating) CosmicGold else NebularDust.copy(alpha = 0.3f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                // 2. Checklist for Factors
                Text("Factores de influencias:", color = StarryWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FactorCheckboxRow(label = "Consumí Café / Té tarde", checked = caffeine, onCheckedChange = { caffeine = it })
                    FactorCheckboxRow(label = "Consumí Alcohol", checked = alcohol, onCheckedChange = { alcohol = it })
                    FactorCheckboxRow(label = "Hice Ejercicio físico regular", checked = exercise, onCheckedChange = { exercise = it })
                    FactorCheckboxRow(label = "Día muy estresante / Preocupado", checked = stress, onCheckedChange = { stress = it })
                    FactorCheckboxRow(label = "Usé pantallas en la cama antes de dormir", checked = screenTime, onCheckedChange = { screenTime = it })
                }

                // 3. Notes field
                Text("Diario de Sueño / Sueños recordados:", color = StarryWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("Escribe notas sobre ruidos, sueños, etc.", color = NebularDust, fontSize = 13.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SoftAmethyst,
                        unfocusedBorderColor = MidnightDeep,
                        focusedTextColor = StarryWhite,
                        unfocusedTextColor = StarryWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDiscard) {
                        Text("Descartar", color = SleepDisturbed)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(rating, notes, alcohol, caffeine, exercise, stress, screenTime)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SleepRestful),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_sleep_log_button")
                    ) {
                        Text("Guardar Registro", color = StarryWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun FactorCheckboxRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null, // Handled by Row click
            colors = CheckboxDefaults.colors(
                checkedColor = SoftAmethyst,
                checkmarkColor = StarryWhite,
                uncheckedColor = StarryWhite.copy(alpha = 0.5f)
            )
        )
        Text(text = label, color = StarryWhite, fontSize = 13.sp)
    }
}

@Composable
fun AlarmEditorDialog(
    alarm: Alarm?,
    onDismiss: () -> Unit,
    onSave: (Alarm) -> Unit
) {
    var hourText by remember { mutableStateOf(alarm?.hour?.toString() ?: "07") }
    var minuteText by remember { mutableStateOf(alarm?.minute?.toString() ?: "00") }
    var label by remember { mutableStateOf(alarm?.label ?: "Despertador") }
    var isVibrate by remember { mutableStateOf(alarm?.isVibrate ?: true) }

    // Weekdays Checklist
    var repMon by remember { mutableStateOf(alarm?.repeatMonday ?: false) }
    var repTue by remember { mutableStateOf(alarm?.repeatTuesday ?: false) }
    var repWed by remember { mutableStateOf(alarm?.repeatWednesday ?: false) }
    var repThu by remember { mutableStateOf(alarm?.repeatThursday ?: false) }
    var repFri by remember { mutableStateOf(alarm?.repeatFriday ?: false) }
    var repSat by remember { mutableStateOf(alarm?.repeatSaturday ?: false) }
    var repSun by remember { mutableStateOf(alarm?.repeatSunday ?: false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
                .testTag("alarm_editor_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MidnightCard)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (alarm == null) "Agregar Alarma" else "Editar Alarma",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = StarryWhite
                )

                // Hour / Minute Inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = hourText,
                        onValueChange = { if (it.length <= 2 && it.all { char -> char.isDigit() }) hourText = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("Hora (0-23)", fontSize = 11.sp) },
                        modifier = Modifier.width(90.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicGold,
                            focusedLabelColor = CosmicGold,
                            focusedTextColor = StarryWhite,
                            unfocusedTextColor = StarryWhite
                        )
                    )

                    Text(text = ":", fontSize = 32.sp, color = StarryWhite, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = minuteText,
                        onValueChange = { if (it.length <= 2 && it.all { char -> char.isDigit() }) minuteText = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("Min (0-59)", fontSize = 11.sp) },
                        modifier = Modifier.width(90.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicGold,
                            focusedLabelColor = CosmicGold,
                            focusedTextColor = StarryWhite,
                            unfocusedTextColor = StarryWhite
                        )
                    )
                }

                // Label field
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Etiqueta", fontSize = 12.sp) },
                    placeholder = { Text("Despertar Diario", color = NebularDust) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CosmicGold,
                        focusedLabelColor = CosmicGold,
                        focusedTextColor = StarryWhite,
                        unfocusedTextColor = StarryWhite
                    )
                )

                // Recurrence
                Text("Repetir los días:", color = StarryWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    WeekdaySelectorNode(label = "L", active = repMon, onClick = { repMon = !repMon })
                    WeekdaySelectorNode(label = "M", active = repTue, onClick = { repTue = !repTue })
                    WeekdaySelectorNode(label = "M", active = repWed, onClick = { repWed = !repWed })
                    WeekdaySelectorNode(label = "J", active = repThu, onClick = { repThu = !repThu })
                    WeekdaySelectorNode(label = "V", active = repFri, onClick = { repFri = !repFri })
                    WeekdaySelectorNode(label = "S", active = repSat, onClick = { repSat = !repSat })
                    WeekdaySelectorNode(label = "D", active = repSun, onClick = { repSun = !repSun })
                }

                // Settings
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isVibrate = !isVibrate }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Vibration, contentDescription = "Vibrate", tint = NebularDust)
                        Text(text = "Vibración activa", color = StarryWhite, fontSize = 13.sp)
                    }

                    Checkbox(
                        checked = isVibrate,
                        onCheckedChange = null,
                        colors = CheckboxDefaults.colors(checkedColor = CosmicGold)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = SleepDisturbed)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val h = hourText.toIntOrNull()?.coerceIn(0, 23) ?: 7
                            val m = minuteText.toIntOrNull()?.coerceIn(0, 59) ?: 0
                            val alarmResult = Alarm(
                                id = alarm?.id ?: 0,
                                hour = h,
                                minute = m,
                                label = if (label.trim().isEmpty()) "Alarma" else label,
                                repeatMonday = repMon,
                                repeatTuesday = repTue,
                                repeatWednesday = repWed,
                                repeatThursday = repThu,
                                repeatFriday = repFri,
                                repeatSaturday = repSat,
                                repeatSunday = repSun,
                                isVibrate = isVibrate
                            )
                            onSave(alarmResult)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Guardar", color = StarryWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun WeekdaySelectorNode(label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .background(
                color = if (active) CosmicGold else MidnightDeep,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (active) MidnightDeep else StarryWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}


@Composable
fun ReminderEditorDialog(
    reminder: Reminder?,
    onDismiss: () -> Unit,
    onSave: (Reminder) -> Unit
) {
    var title by remember { mutableStateOf(reminder?.title ?: "") }
    var hourText by remember { mutableStateOf(reminder?.hour?.toString() ?: "22") }
    var minuteText by remember { mutableStateOf(reminder?.minute?.toString() ?: "00") }
    var message by remember { mutableStateOf(reminder?.message ?: "") }
    var selectedType by remember { mutableStateOf(reminder?.type ?: "custom") }

    var repMon by remember { mutableStateOf(reminder?.repeatMonday ?: false) }
    var repTue by remember { mutableStateOf(reminder?.repeatTuesday ?: false) }
    var repWed by remember { mutableStateOf(reminder?.repeatWednesday ?: false) }
    var repThu by remember { mutableStateOf(reminder?.repeatThursday ?: false) }
    var repFri by remember { mutableStateOf(reminder?.repeatFriday ?: false) }
    var repSat by remember { mutableStateOf(reminder?.repeatSaturday ?: false) }
    var repSun by remember { mutableStateOf(reminder?.repeatSunday ?: false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
                .testTag("reminder_editor_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MidnightCard)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (reminder == null) "Nuevo Recordatorio" else "Editar Recordatorio",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = StarryWhite
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título / Hábito", fontSize = 12.sp) },
                    placeholder = { Text("Ej: Apagar pantallas", color = NebularDust) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SoftAmethyst,
                        focusedLabelColor = SoftAmethyst,
                        focusedTextColor = StarryWhite,
                        unfocusedTextColor = StarryWhite
                    )
                )

                // Schedule times
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = hourText,
                        onValueChange = { if (it.length <= 2 && it.all { char -> char.isDigit() }) hourText = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("Hora", fontSize = 11.sp) },
                        modifier = Modifier.width(90.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoftAmethyst, focusedLabelColor = SoftAmethyst, focusedTextColor = StarryWhite, unfocusedTextColor = StarryWhite)
                    )

                    Text(text = ":", fontSize = 32.sp, color = StarryWhite, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = minuteText,
                        onValueChange = { if (it.length <= 2 && it.all { char -> char.isDigit() }) minuteText = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("Minutos", fontSize = 11.sp) },
                        modifier = Modifier.width(90.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoftAmethyst, focusedLabelColor = SoftAmethyst, focusedTextColor = StarryWhite, unfocusedTextColor = StarryWhite)
                    )
                }

                // Type selector
                Text("Tipo de recordatorio:", color = StarryWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                val types = listOf(
                    "bedtime" to "Cama",
                    "wind_down" to "Relajación",
                    "hydration" to "Cafeína",
                    "custom" to "Hábito"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    types.forEach { (typeKey, typeLabel) ->
                        val isSelected = selectedType == typeKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    color = if (isSelected) SoftAmethyst else MidnightDeep,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedType = typeKey }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = typeLabel,
                                color = if (isSelected) StarryWhite else NebularDust,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Description field
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Mensaje motivacional (Opcional)", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SoftAmethyst,
                        focusedLabelColor = SoftAmethyst,
                        focusedTextColor = StarryWhite,
                        unfocusedTextColor = StarryWhite
                    )
                )

                // Recurrence
                Text("Repetir los días:", color = StarryWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    WeekdaySelectorNode(label = "L", active = repMon, onClick = { repMon = !repMon })
                    WeekdaySelectorNode(label = "M", active = repTue, onClick = { repTue = !repTue })
                    WeekdaySelectorNode(label = "M", active = repWed, onClick = { repWed = !repWed })
                    WeekdaySelectorNode(label = "J", active = repThu, onClick = { repThu = !repThu })
                    WeekdaySelectorNode(label = "V", active = repFri, onClick = { repFri = !repFri })
                    WeekdaySelectorNode(label = "S", active = repSat, onClick = { repSat = !repSat })
                    WeekdaySelectorNode(label = "D", active = repSun, onClick = { repSun = !repSun })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = SleepDisturbed)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val h = hourText.toIntOrNull()?.coerceIn(0, 23) ?: 22
                            val m = minuteText.toIntOrNull()?.coerceIn(0, 59) ?: 0
                            val remResult = Reminder(
                                id = reminder?.id ?: 0,
                                title = if (title.trim().isEmpty()) "Hábito Saludable" else title,
                                hour = h,
                                minute = m,
                                isEnabled = reminder?.isEnabled ?: true,
                                type = selectedType,
                                message = message,
                                repeatMonday = repMon,
                                repeatTuesday = repTue,
                                repeatWednesday = repWed,
                                repeatThursday = repThu,
                                repeatFriday = repFri,
                                repeatSaturday = repSat,
                                repeatSunday = repSun
                            )
                            onSave(remResult)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftAmethyst),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Guardar", color = StarryWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
