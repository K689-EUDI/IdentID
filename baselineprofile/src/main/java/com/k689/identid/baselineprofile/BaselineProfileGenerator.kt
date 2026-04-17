package com.k689.identid.baselineprofile

import android.os.SystemClock
import android.view.KeyEvent
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.Until
import androidx.test.uiautomator.UiObject2
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun startupAndDashboardNavigation() =
        baselineProfileRule.collect(packageName = PACKAGE_NAME) {
            pressHome()
            startActivityAndWait()
            device.waitForIdle()
            unlockWalletIfNeeded()
            continueAfterFirstRunSetupIfNeeded()
            device.waitForIdle()
        }

    private fun MacrobenchmarkScope.unlockWalletIfNeeded() {
        repeat(MAX_UNLOCK_STEPS) { attempt ->
            waitForPossibleLockUi(
                timeoutMs = if (attempt == 0) LOCK_UI_APPEAR_TIMEOUT_MS else SHORT_TIMEOUT_MS,
            )

            when {
                hasAppObject(HOME_TAB_SELECTOR) -> {
                    return
                }
                hasObject(SUCCESS_PRIMARY_BUTTON_SELECTOR) || hasObject(GO_HOME_BUTTON_TEXT_SELECTOR) -> {
                    return
                }
                hasObject(PIN_SETUP_TITLE_SELECTOR) -> {
                    enterPin(PIN_CODE)
                    submitPinStep()
                }
                hasObject(PIN_SETUP_REENTER_SELECTOR) -> {
                    enterPin(PIN_CODE)
                    submitPinStep()
                }
                hasObject(BIOMETRIC_PIN_TEXT_SELECTOR) || hasObject(BIOMETRIC_PIN_SUBTITLE_SELECTOR) -> {
                    enterPin(PIN_CODE)
                }
                hasObject(EDIT_TEXT_SELECTOR) -> {
                    enterPin(PIN_CODE)
                    submitPinStep()
                }
                hasBiometricPromptVisible() -> {
                    dismissBiometricPromptIfShowing()
                }
                else -> Unit
            }

            device.waitForIdle()
        }
    }

    private fun MacrobenchmarkScope.continueAfterFirstRunSetupIfNeeded() {
        clickIfPresent(SUCCESS_PRIMARY_BUTTON_SELECTOR, SHORT_TIMEOUT_MS)
        clickIfPresent(GO_HOME_BUTTON_TEXT_SELECTOR, SHORT_TIMEOUT_MS)
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.enterPin(pin: String) {
        waitForAppObject(pinFieldSelector(index = 0), SHORT_TIMEOUT_MS)?.click()
            ?: waitForObject(EDIT_TEXT_SELECTOR, SHORT_TIMEOUT_MS)?.click()
        device.waitForIdle()

        pin.forEach { digit ->
            device.pressKeyCode(digit.toKeyCode())
        }

        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.submitPinStep() {
        device.pressBack()
        device.waitForIdle()

        if (clickIfPresent(PIN_PRIMARY_BUTTON_SELECTOR, UI_TIMEOUT_MS)) {
            return
        }

        if (clickIfPresent(CONFIRM_BUTTON_TEXT_SELECTOR, UI_TIMEOUT_MS)) {
            return
        }

        clickIfPresent(NEXT_BUTTON_TEXT_SELECTOR, UI_TIMEOUT_MS)
    }

    private fun MacrobenchmarkScope.dismissBiometricPromptIfShowing() {
        val systemPromptVisible = hasBiometricPromptVisible()

        if (!systemPromptVisible) {
            return
        }

        val dismissed =
            clickIfPresent(USE_PIN_BUTTON_TEXT_SELECTOR, SHORT_TIMEOUT_MS) ||
                clickIfPresent(USE_PASSWORD_BUTTON_TEXT_SELECTOR, SHORT_TIMEOUT_MS) ||
                clickIfPresent(BIOMETRIC_PROMPT_CANCEL_TEXT_SELECTOR, SHORT_TIMEOUT_MS) ||
                clickIfPresent(CANCEL_BUTTON_TEXT_SELECTOR, SHORT_TIMEOUT_MS) ||
                clickIfPresent(ANDROID_PROMPT_BUTTON_SELECTOR, SHORT_TIMEOUT_MS)

        if (!dismissed) return

        waitForPossibleLockUi(timeoutMs = LOCK_UI_APPEAR_TIMEOUT_MS)

        if (hasObject(BIOMETRIC_PIN_TEXT_SELECTOR) || hasObject(BIOMETRIC_PIN_SUBTITLE_SELECTOR)) {
            enterPin(PIN_CODE)
        }

        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.hasBiometricPromptVisible(): Boolean =
        device.currentPackageName != PACKAGE_NAME &&
            (
                hasObject(BIOMETRIC_PROMPT_PACKAGE_SELECTOR) ||
                    hasObject(ANDROID_PROMPT_BUTTON_SELECTOR) ||
                    hasObject(BIOMETRIC_PROMPT_TITLE_SELECTOR) ||
                    hasObject(BIOMETRIC_PROMPT_SUBTITLE_SELECTOR) ||
                    hasObject(FINGERPRINT_TEXT_SELECTOR) ||
                    hasObject(CANCEL_BUTTON_TEXT_SELECTOR) ||
                    hasObject(USE_PIN_BUTTON_TEXT_SELECTOR) ||
                    hasObject(USE_PASSWORD_BUTTON_TEXT_SELECTOR)
            )

    private fun MacrobenchmarkScope.waitForPossibleLockUi(timeoutMs: Long) {
        val deadline = SystemClock.uptimeMillis() + timeoutMs

        while (SystemClock.uptimeMillis() < deadline) {
            if (hasAnyLockUiObject()) {
                return
            }

            SystemClock.sleep(LOCK_UI_POLL_INTERVAL_MS)
        }
    }

    private fun MacrobenchmarkScope.hasAnyLockUiObject(): Boolean =
        hasObject(PIN_SETUP_TITLE_SELECTOR) ||
            hasObject(PIN_SETUP_REENTER_SELECTOR) ||
            hasObject(BIOMETRIC_PROMPT_TITLE_SELECTOR) ||
            hasObject(BIOMETRIC_PIN_SUBTITLE_SELECTOR) ||
            hasObject(FINGERPRINT_TEXT_SELECTOR) ||
            hasObject(ANDROID_PROMPT_BUTTON_SELECTOR) ||
            hasObject(CANCEL_BUTTON_TEXT_SELECTOR) ||
            hasObject(BIOMETRIC_PROMPT_CANCEL_TEXT_SELECTOR) ||
            hasObject(USE_PIN_BUTTON_TEXT_SELECTOR) ||
            hasObject(USE_PASSWORD_BUTTON_TEXT_SELECTOR) ||
            hasObject(BIOMETRIC_PROMPT_PACKAGE_SELECTOR)

    private fun MacrobenchmarkScope.hasAppObject(selector: BySelector): Boolean =
        device.findObject(selector) != null

    private fun MacrobenchmarkScope.hasObject(selector: BySelector): Boolean =
        device.findObject(selector) != null

    private fun MacrobenchmarkScope.waitForObject(
        selector: BySelector,
        timeoutMs: Long = UI_TIMEOUT_MS,
    ): UiObject2? {
        if (!device.wait(Until.hasObject(selector), timeoutMs)) {
            return null
        }

        return device.findObject(selector)
    }

    private fun MacrobenchmarkScope.clickIfPresent(
        selector: BySelector,
        timeoutMs: Long = SHORT_TIMEOUT_MS,
    ): Boolean {
        val target = waitForObject(selector, timeoutMs) ?: return false
        target.click()
        device.waitForIdle()
        return true
    }

    private fun MacrobenchmarkScope.waitForAppObject(
        selector: BySelector,
        timeoutMs: Long = UI_TIMEOUT_MS,
    ): UiObject2? {
        if (!device.wait(Until.hasObject(selector), timeoutMs)) {
            return null
        }

        return device.findObject(selector)
    }

    private fun pinFieldSelector(index: Int): BySelector =
        By.res(PACKAGE_NAME, "pin_text_field_$index")

    private fun Char.toKeyCode(): Int =
        when (this) {
            '0' -> KeyEvent.KEYCODE_0
            '1' -> KeyEvent.KEYCODE_1
            '2' -> KeyEvent.KEYCODE_2
            '3' -> KeyEvent.KEYCODE_3
            '4' -> KeyEvent.KEYCODE_4
            '5' -> KeyEvent.KEYCODE_5
            '6' -> KeyEvent.KEYCODE_6
            '7' -> KeyEvent.KEYCODE_7
            '8' -> KeyEvent.KEYCODE_8
            '9' -> KeyEvent.KEYCODE_9
            else -> error("Unsupported pin digit: $this")
        }

    companion object {
        const val PACKAGE_NAME = "com.k689.identid"
        const val PIN_CODE = "123456"
        const val MAX_UNLOCK_STEPS = 8
        const val UI_TIMEOUT_MS = 5_000L
        const val SHORT_TIMEOUT_MS = 1_000L
        const val LOCK_UI_APPEAR_TIMEOUT_MS = 3_000L
        const val LOCK_UI_POLL_INTERVAL_MS = 200L
        const val SYSTEM_UI_PACKAGE = "com.android.systemui"

        val HOME_TAB_SELECTOR = By.res(PACKAGE_NAME, "dashboard_screen_bottom_navigation_item_home")
        val PIN_PRIMARY_BUTTON_SELECTOR = By.res(PACKAGE_NAME, "pin_screen_button")
        val BIOMETRIC_PIN_TEXT_SELECTOR = By.res(PACKAGE_NAME, "biometric_screen_pin_text")
        val SUCCESS_PRIMARY_BUTTON_SELECTOR = By.res(PACKAGE_NAME, "success_screen_primary_button")
        val BIOMETRIC_PROMPT_PACKAGE_SELECTOR = By.pkg(SYSTEM_UI_PACKAGE)
        val ANDROID_PROMPT_BUTTON_SELECTOR = By.res("android", "button2")
        val EDIT_TEXT_SELECTOR = By.clazz("android.widget.EditText")
        val PIN_SETUP_TITLE_SELECTOR = By.text("Welcome to your Wallet")
        val PIN_SETUP_REENTER_SELECTOR = By.text("Re-enter the pin")
        val BIOMETRIC_PIN_SUBTITLE_SELECTOR = By.textContains("Enter your PIN code to continue")
        val BIOMETRIC_PROMPT_TITLE_SELECTOR = By.text("Biometric authentication")
        val BIOMETRIC_PROMPT_SUBTITLE_SELECTOR = By.text("Authenticate using your biometrics")
        val FINGERPRINT_TEXT_SELECTOR = By.textContains("finger")
        val CANCEL_BUTTON_TEXT_SELECTOR = By.text("Cancel")
        val BIOMETRIC_PROMPT_CANCEL_TEXT_SELECTOR = By.textContains("Cancel")
        val USE_PIN_BUTTON_TEXT_SELECTOR = By.textContains("PIN")
        val USE_PASSWORD_BUTTON_TEXT_SELECTOR = By.textContains("password")
        val NEXT_BUTTON_TEXT_SELECTOR = By.text("NEXT")
        val CONFIRM_BUTTON_TEXT_SELECTOR = By.text("CONFIRM")
        val GO_HOME_BUTTON_TEXT_SELECTOR = By.text("GO TO HOME")
    }
}