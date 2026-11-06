package com.nexussync.datausage.domain.model

enum class NetworkType(val displayLabel: String) {
    WIFI("Wi-Fi"),
    CELLULAR("Mobile"),
    ETHERNET("Ethernet"),
    OTHER("Network"),
    OFFLINE("Offline")
}

enum class DataSaverState(val displayLabel: String) {
    DISABLED("Inactive"),
    WHITELISTED("Active (Unrestricted)"),
    ENABLED("Active"),
    UNKNOWN("System Managed")
}

data class NetworkConnectionInfo(
    val type: NetworkType = NetworkType.OFFLINE,
    val isConnected: Boolean = false,
    val isValidated: Boolean = false,
    val isMetered: Boolean = false,
    val isVpnActive: Boolean = false,
    val downstreamLinkKbps: Int = 0,
    val upstreamLinkKbps: Int = 0
) {
    val statusBadgeText: String
        get() = when {
            !isConnected -> "OFFLINE"
            isVpnActive -> "${type.displayLabel.uppercase()} • VPN"
            isMetered && type == NetworkType.WIFI -> "WI-FI • METERED"
            else -> type.displayLabel.uppercase()
        }
}
