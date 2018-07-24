package com.pixelplex.echoframework.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Test cases for [ObjectType] model
 *
 * @author Dmitriy Bushuev
 */
class ObjectTypeTest {

    @Test
    fun objectIdTest() {
        val baseObject = ObjectType.BASE_OBJECT
        val accountObject = ObjectType.ACCOUNT_OBJECT
        val forceSettlementObject = ObjectType.FORCE_SETTLEMENT_OBJECT
        val globalPropertiesObject = ObjectType.GLOBAL_PROPERTY_OBJECT
        val specialAuthorityObject = ObjectType.SPECIAL_AUTHORITY_OBJECT

        assertEquals("1.1.0", baseObject.genericObjectId)
        assertEquals("1.2.0", accountObject.genericObjectId)
        assertEquals("1.4.0", forceSettlementObject.genericObjectId)
        assertEquals("2.0.0", globalPropertiesObject.genericObjectId)
        assertEquals("2.14.0", specialAuthorityObject.genericObjectId)
    }

    @Test
    fun findBySpaceAndTypeTest() {
        val baseObject = Pair(1, 1)
        val globalPropertiesObject = Pair(2, 0)
        val accountObject = Pair(1, 2)
        val chainPropertiesObject = Pair(2, 11)
        val nonExistentObject = Pair(4, 1)

        assertEquals(
            ObjectType.BASE_OBJECT,
            ObjectType.get(baseObject.first, baseObject.second)
        )
        assertEquals(
            ObjectType.GLOBAL_PROPERTY_OBJECT,
            ObjectType.get(globalPropertiesObject.first, globalPropertiesObject.second)
        )
        assertEquals(
            ObjectType.ACCOUNT_OBJECT,
            ObjectType.get(accountObject.first, accountObject.second)
        )
        assertEquals(
            ObjectType.CHAIN_PROPERTY_OBJECT,
            ObjectType.get(chainPropertiesObject.first, chainPropertiesObject.second)
        )

        assertNull(ObjectType.get(nonExistentObject.first, nonExistentObject.second))
    }

}
