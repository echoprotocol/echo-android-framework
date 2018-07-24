package com.pixelplex.echoframework.core.crypto.internal

import com.pixelplex.echoframework.core.crypto.SeedProvider
import com.pixelplex.echoframework.model.AuthorityType
import com.pixelplex.echoframework.support.Converter
import com.pixelplex.echoframework.support.checkNotNull

/**
 * Provides seed for key creation with active user role
 *
 * @author Dmitriy Bushuev
 */
class RoleDependentSeedProvider : SeedProvider {

    override fun provide(name: String, password: String, authorityType: AuthorityType): String {
        val roleName = AuthorityTypeToRoleConverter().convert(authorityType)

        return name + roleName + password
    }

    private class AuthorityTypeToRoleConverter : Converter<AuthorityType, String> {

        private val roleNameRegistry = hashMapOf(
            AuthorityType.ACTIVE to "active",
            AuthorityType.OWNER to "owner",
            AuthorityType.KEY to "memo"
        )

        override fun convert(source: AuthorityType): String {
            val roleName = roleNameRegistry[source]

            checkNotNull(roleName, "Unrecognized authority type: $source")

            return roleName!!
        }

    }

}
