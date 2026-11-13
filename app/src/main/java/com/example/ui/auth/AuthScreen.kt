package com.example.ui.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.LocalAccountSessionManager
import com.example.ui.components.ReceiptDashedDividerLine
import com.example.ui.components.ReceiptTicketCard
import com.example.ui.components.SquircleIconBadge
import com.example.ui.components.ThemeModeSelectorBar
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.ReceiptMonoFontFamily
import com.example.ui.theme.rememberAppHapticPerformer
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "OneFamilyAuth"

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun resolveDefaultWebClientId(context: Context): String? {
    return runCatching { context.getString(R.string.default_web_client_id) }
        .getOrNull()
        ?.takeIf { it.isNotBlank() }
}

fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
) {
    LocalAccountSessionManager.ensureInitialized(context)
    val firebaseUser = runCatching { Firebase.auth.currentUser }.getOrNull()
    if (firebaseUser != null || LocalAccountSessionManager.getCurrentUser(context) != null) {
        onAuthSuccess()
        return
    }
    val clientId = resolveDefaultWebClientId(context)
    if (clientId == null) {
        onUnauthenticated()
        return
    }

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    scope.launch {
        try {
            val activityContext = context.findActivity() ?: context
            val result = credentialManager.getCredential(activityContext, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onUnauthenticated()
            }
        } catch (_: Exception) {
            onUnauthenticated()
        }
    }
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    val clientId = resolveDefaultWebClientId(context)
    if (clientId == null) {
        onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(signInOption)
        .build()

    scope.launch {
        try {
            val activityContext = context.findActivity() ?: context
            val result = credentialManager.getCredential(activityContext, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                LocalAccountSessionManager.signOut(context)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onAuthError("Unexpected credential type returned.")
            }
        } catch (e: GetCredentialCancellationException) {
            val msg = e.message.orEmpty()
            Log.w(TAG, "Google Sign-In cancelled or dismissed: $msg", e)
            if (msg.contains("28444") || msg.contains("Developer console", ignoreCase = true) || msg.contains("10:")) {
                onAuthError("Google Sign-In failed ($msg). Use Login as Mock User above.")
            } else {
                onAuthCancelled()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In failed", e)
            onAuthError(e.localizedMessage ?: "Sign in failed. Please try again.")
        }
    }
}

fun signOut(
    context: Context,
    credentialManager: CredentialManager,
    onSignOutComplete: () -> Unit,
    scope: CoroutineScope
) {
    scope.launch {
        kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w(TAG, "Failed to clear credential state: ${e.message}")
            } finally {
                LocalAccountSessionManager.signOut(context)
                runCatching { Firebase.auth.signOut() }
                onSignOutComplete()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val currentThemeMode by LocalAccountSessionManager.themeMode.collectAsStateWithLifecycle()
    val performHaptic = rememberAppHapticPerformer()

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var usernameInput by rememberSaveable { mutableStateOf("") }
    var passwordInput by rememberSaveable { mutableStateOf("") }
    var displayNameInput by rememberSaveable { mutableStateOf("") }
    var isCreateAccountMode by rememberSaveable { mutableStateOf(false) }
    var showPassword by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        LocalAccountSessionManager.ensureInitialized(context)
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = onAuthSuccess,
            onUnauthenticated = {},
            scope = coroutineScope
        )
    }

    val usernameError = when {
        usernameInput.isEmpty() -> null
        usernameInput.trim().length < 2 -> "Username must be at least 2 characters"
        usernameInput.trim().length > 40 -> "Username must be 40 characters or fewer"
        else -> null
    }
    val passwordError = when {
        passwordInput.isEmpty() -> null
        passwordInput.length < 3 -> "Password must be at least 3 characters"
        else -> null
    }
    val displayNameError = when {
        !isCreateAccountMode || displayNameInput.isEmpty() -> null
        displayNameInput.trim().length < 2 -> "Name must be at least 2 characters"
        displayNameInput.trim().length > 50 -> "Name must be 50 characters or fewer"
        else -> null
    }

    val performUsernameSubmit: () -> Unit = {
        performHaptic()
        errorMessage = null
        when {
            usernameInput.isBlank() -> {
                errorMessage = "Please enter your username."
            }
            passwordInput.isBlank() -> {
                errorMessage = "Please enter your password."
            }
            usernameError != null || passwordError != null || displayNameError != null -> {
                errorMessage = usernameError ?: passwordError ?: displayNameError
            }
            else -> {
                val result = if (isCreateAccountMode) {
                    LocalAccountSessionManager.registerUsernamePassword(
                        context = context,
                        rawUsername = usernameInput,
                        rawPassword = passwordInput,
                        rawDisplayName = displayNameInput
                    )
                } else {
                    LocalAccountSessionManager.signInWithUsernamePassword(
                        context = context,
                        rawUsername = usernameInput,
                        rawPassword = passwordInput
                    )
                }
                result.onSuccess {
                    runCatching { Firebase.auth.signOut() }
                    onAuthSuccess()
                }.onFailure { err ->
                    errorMessage = err.localizedMessage ?: "Login failed."
                }
            }
        }
    }

    val triggerGoogleSignIn: () -> Unit = {
        performHaptic()
        errorMessage = null
        isLoading = true
        onGoogleSignInClicked(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
                isLoading = false
                onAuthSuccess()
            },
            onAuthError = { msg ->
                isLoading = false
                errorMessage = msg
            },
            scope = coroutineScope,
            onAuthCancelled = {
                isLoading = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f),
                        Color.Transparent,
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.22f)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Editorial Welcome Hero (inspired by "Welcome to Rhythm" reference)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome to",
                    style = MaterialTheme.typography.headlineSmall.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "One Family",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.88f)
                ) {
                    Text(
                        text = "Personal & Family Paper Receipt Ledger",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            ThemeModeSelectorBar(
                currentMode = currentThemeMode,
                onSelectMode = {
                    performHaptic()
                    LocalAccountSessionManager.setThemeMode(context, it)
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (errorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            // Tactile Thermal Paper Receipt Slip
            ReceiptTicketCard(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Material 3 Checkmark SingleChoiceSegmentedButtonRow: Log In | New Member
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        val modes = listOf(false to "Log In", true to "New Member")
                        modes.forEachIndexed { idx, (createMode, label) ->
                            val selected = isCreateAccountMode == createMode
                            SegmentedButton(
                                selected = selected,
                                onClick = {
                                    performHaptic()
                                    isCreateAccountMode = createMode
                                    errorMessage = null
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = idx, count = modes.size),
                                icon = {
                                    SegmentedButtonDefaults.Icon(active = selected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                                        )
                                    }
                                }
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // 1-Tap Mock User Receipt Ticket Stub (test / pass)
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable {
                                performHaptic()
                                errorMessage = null
                                usernameInput = LocalAccountSessionManager.DEFAULT_TEST_USERNAME
                                passwordInput = LocalAccountSessionManager.DEFAULT_TEST_PASSWORD
                                isCreateAccountMode = false
                                val res = LocalAccountSessionManager.signInWithUsernamePassword(
                                    context = context,
                                    rawUsername = LocalAccountSessionManager.DEFAULT_TEST_USERNAME,
                                    rawPassword = LocalAccountSessionManager.DEFAULT_TEST_PASSWORD
                                )
                                res.onSuccess {
                                    runCatching { Firebase.auth.signOut() }
                                    onAuthSuccess()
                                }.onFailure { errorMessage = it.localizedMessage }
                            }
                            .testTag("quick_test_login_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SquircleIconBadge(
                                    icon = Icons.Default.Star,
                                    tint = MaterialTheme.colorScheme.onSecondary,
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    size = 34.dp,
                                    iconSize = 18.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Login as Mock User",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
                            ) {
                                Text(
                                    text = "test/pass",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = ReceiptMonoFontFamily),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = {
                            if (it.length <= 50) {
                                usernameInput = it
                                errorMessage = null
                            }
                        },
                        label = { Text("Username") },
                        placeholder = { Text("test") },
                        isError = usernameError != null,
                        supportingText = if (usernameError != null) {
                            { Text(usernameError) }
                        } else null,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("username_input")
                    )

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            if (it.length <= 64) {
                                passwordInput = it
                                errorMessage = null
                            }
                        },
                        label = { Text("Password") },
                        placeholder = { Text("pass") },
                        isError = passwordError != null,
                        supportingText = if (passwordError != null) {
                            { Text(passwordError) }
                        } else null,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Key,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showPassword) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input")
                    )

                    AnimatedVisibility(
                        visible = isCreateAccountMode,
                        enter = slideInVertically(
                            spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                        ) { -it / 3 } + expandVertically() + fadeIn(),
                        exit = slideOutVertically(
                            spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
                        ) { -it / 3 } + shrinkVertically() + fadeOut()
                    ) {
                        OutlinedTextField(
                            value = displayNameInput,
                            onValueChange = {
                                if (it.length <= 50) {
                                    displayNameInput = it
                                    errorMessage = null
                                }
                            },
                            label = { Text("Name") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Badge,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            isError = displayNameError != null,
                            supportingText = if (displayNameError != null) {
                                { Text(displayNameError) }
                            } else null,
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("display_name_input")
                        )
                    }

                    ReceiptDashedDividerLine()

                    OutlinedButton(
                        onClick = triggerGoogleSignIn,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("google_sign_in_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.AccountCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sign in with Google",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Symmetrical M3 Expressive Bottom Action Row (56dp Circle | Stadium CTA | 56dp Circle)
            val submitBtnColor by animateColorAsState(
                targetValue = if (isCreateAccountMode) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.primary
                },
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "auth_submit_btn_color"
            )
            val submitTextColor by animateColorAsState(
                targetValue = if (isCreateAccountMode) {
                    MaterialTheme.colorScheme.onTertiary
                } else {
                    MaterialTheme.colorScheme.onPrimary
                },
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "auth_submit_txt_color"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    onClick = {
                        performHaptic()
                        val nextMode = when (currentThemeMode) {
                            AppThemeMode.SYSTEM -> AppThemeMode.LIGHT
                            AppThemeMode.LIGHT -> AppThemeMode.DARK
                            AppThemeMode.DARK -> AppThemeMode.SYSTEM
                        }
                        LocalAccountSessionManager.setThemeMode(context, nextMode)
                    },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(56.dp)
                        .testTag("auth_quick_theme_cycle_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val modeIcon = when (currentThemeMode) {
                            AppThemeMode.LIGHT -> Icons.Default.LightMode
                            AppThemeMode.DARK -> Icons.Default.DarkMode
                            AppThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                        }
                        Icon(
                            imageVector = modeIcon,
                            contentDescription = "Cycle Theme Mode",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Button(
                    onClick = performUsernameSubmit,
                    enabled = !isLoading,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = submitBtnColor,
                        contentColor = submitTextColor
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp)
                        .testTag("username_sign_in_button")
                ) {
                    AnimatedContent(
                        targetState = isCreateAccountMode,
                        transitionSpec = {
                            if (targetState) {
                                (slideInHorizontally { it / 2 } + fadeIn())
                                    .togetherWith(slideOutHorizontally { -it / 2 } + fadeOut())
                            } else {
                                (slideInHorizontally { -it / 2 } + fadeIn())
                                    .togetherWith(slideOutHorizontally { it / 2 } + fadeOut())
                            }
                        },
                        label = "auth_submit_label_shift"
                    ) { createMode ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (createMode) "Join Home" else "Enter Home",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Surface(
                    onClick = triggerGoogleSignIn,
                    enabled = !isLoading,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(56.dp)
                        .testTag("auth_quick_google_circle_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.AccountCircle,
                            contentDescription = "Google Sign In",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
