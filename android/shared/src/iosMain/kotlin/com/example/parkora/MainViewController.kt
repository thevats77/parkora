package com.example.parkora

import androidx.compose.ui.window.ComposeUIViewController
import com.example.parkora.data.ParkoraRepository
import platform.UIKit.UIViewController

/*
 * iPhone entry point. The Swift app (iosApp/iosApp/ContentView.swift) calls
 * MainViewController() and shows the returned screen – the same ParkoraApp()
 * that Android shows from MainActivity.
 */
fun MainViewController(): UIViewController {
    ParkoraRepository.init()
    return ComposeUIViewController { ParkoraApp() }
}
