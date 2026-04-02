/*
 * Copyright (c) 2026 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work
 * except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the Licence for the specific language
 * governing permissions and limitations under the Licence.
 */

package com.k689.identid.ui.dashboard.home.components

import com.k689.identid.navigation.DashboardScreens
import com.k689.identid.navigation.TransferScreens

sealed class DrawerMenuItem(
    val title: String,
    val route: String? = null,
) {
    data object ChangePin : DrawerMenuItem(title = "Change PIN")

    data object MoveWallet : DrawerMenuItem(title = "Move wallet", route = TransferScreens.MoveWallet.screenRoute)

    data object ReceiveWallet : DrawerMenuItem(title = "Receive wallet", route = TransferScreens.ReceiveWallet.screenRoute)

    data object Preferences : DrawerMenuItem(title = "Preferences", route = DashboardScreens.Preferences.screenRoute)

    companion object {
        val all: List<DrawerMenuItem> =
            listOf(
                ChangePin,
                MoveWallet,
                ReceiveWallet,
                Preferences,
            )
    }
}
