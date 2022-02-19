package de.micromata.borgbutler.server.user

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class UserFilterTest {
    @Test
    fun checkRemoteAddressTest() {
        check(null, null, false)
        check("127.0.0.1", null, true)
        check("127.0.0.1", "172.0.", true)
        check("127.0.0.1", "192.168.", true)
        check("192.168.1.1", "192.168.", true)
        check("192.168.1.1", "192.168. 192.178.5", true)
        check("192.178.5.1", "192.168. 192.178.5", true)
        check("192.178.6.1", "192.168. 192.178.5", false)
    }

    fun check(remoteAddress: String?, allowedClientIps: String?, expected: Boolean) {
        if (allowedClientIps != null) {
            System.setProperty(UserFilter.SYSTEM_PROPERTY_ALLOWED_CLIENT_IPS, allowedClientIps)
        } else {
            System.clearProperty(UserFilter.SYSTEM_PROPERTY_ALLOWED_CLIENT_IPS)
        }
        Assertions.assertEquals(expected, UserFilter().check(remoteAddress))
    }
}
