package org.echo.mobile.framework.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * Test cases for [GrapheneObject] model
 *
 * @author Dmitriy Bushuev
 */
class GrapheneObjectTest {

    private lateinit var baseGrapheneObject: GrapheneObject
    private lateinit var accountGrapheneObject: GrapheneObject
    private lateinit var forceSettlementGrapheneObject: GrapheneObject
    private lateinit var globalPropertiesGrapheneObject: GrapheneObject
    private lateinit var specialAuthorityGrapheneObject: GrapheneObject
    private lateinit var nonexistentGrapheneObject: GrapheneObject

    @Before
    fun setUp() {
        baseGrapheneObject = GrapheneObject("1.1.0")
        accountGrapheneObject = GrapheneObject("1.2.0")
        forceSettlementGrapheneObject = GrapheneObject("1.4.0")
        globalPropertiesGrapheneObject = GrapheneObject("2.0.0")
        specialAuthorityGrapheneObject = GrapheneObject("2.13.0")
        nonexistentGrapheneObject = GrapheneObject("5.14.0")
    }

    @Test
    fun getObjectIdTest() {
        assertEquals("1.1.0", baseGrapheneObject.getObjectId())
        assertEquals("1.2.0", accountGrapheneObject.getObjectId())
        assertEquals("1.4.0", forceSettlementGrapheneObject.getObjectId())
        assertEquals("2.0.0", globalPropertiesGrapheneObject.getObjectId())
        assertEquals("2.13.0", specialAuthorityGrapheneObject.getObjectId())
    }

    @Test
    fun getObjectTypeTest() {
        assertEquals(ObjectType.BASE_OBJECT, baseGrapheneObject.getObjectType())
        assertEquals(ObjectType.ACCOUNT_OBJECT, accountGrapheneObject.getObjectType())

        assertEquals(
            ObjectType.FORCE_SETTLEMENT_OBJECT,
            forceSettlementGrapheneObject.getObjectType()
        )
        assertEquals(
            ObjectType.GLOBAL_PROPERTY_OBJECT,
            globalPropertiesGrapheneObject.getObjectType()
        )
        assertEquals(
            ObjectType.SPECIAL_AUTHORITY_OBJECT,
            specialAuthorityGrapheneObject.getObjectType()
        )

        assertNull(nonexistentGrapheneObject.getObjectType())
    }

}
