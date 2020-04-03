package org.echo.mobile.framework.model

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

        assertEquals("1.1.0", baseObject.genericObjectId)
        assertEquals("1.2.0", accountObject.genericObjectId)
    }

}
