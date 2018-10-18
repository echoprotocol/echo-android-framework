package org.echo.mobile.framework.model.contract

import org.echo.mobile.framework.model.contract.output.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Test cases for [ContractOutputDecoder]
 *
 * @author Dmitriy Bushuev
 */
class ContractOutputDecoderTest {

    @Test
    fun decodeIntTest() {
        val source =
            "000000000000000000000000000000000000000000000000000000000000000a".toByteArray()

        val decoder = ContractOutputDecoder()
        val result = decoder.decode(source, listOf(NumberOutputValueType()))

        assertEquals(result.first().value as Long, 10)
    }

    @Test
    fun decodeBoolTest() {
        val trueSource =
            "0000000000000000000000000000000000000000000000000000000000000001".toByteArray()
        val falseSource =
            "0000000000000000000000000000000000000000000000000000000000000000".toByteArray()

        val decoder = ContractOutputDecoder()
        val resultTrue = decoder.decode(trueSource, listOf(BooleanOutputValueType()))
        val resultFalse = decoder.decode(falseSource, listOf(BooleanOutputValueType()))

        assertTrue(resultTrue.first().value as Boolean)
        assertFalse(resultFalse.first().value as Boolean)
    }

    @Test
    fun decodeStringTest() {
        val trueSource =
            "000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000000000000000000000035452580000000000000000000000000000000000000000000000000000000000"

        val decoder = ContractOutputDecoder()
        val result = decoder.decode(trueSource.toByteArray(), listOf(StringOutputValueType()))

        assertEquals(result.first().value.toString(), "TRX")
    }

    @Test
    fun decodeIntWithBooleanTest() {
        val source =
            ("0000000000000000000000000000000000000000000000000000000000" +
                    "00000a0000000000000000000000000000000" +
                    "000000000000000000000000000000001").toByteArray()

        val decoder = ContractOutputDecoder()
        val result =
            decoder.decode(source, listOf(NumberOutputValueType(), BooleanOutputValueType()))

        assertEquals(result.first().value as Long, 10)
        assertTrue(result.last().value as Boolean)
    }

    @Test
    fun decodeIntWithBooleanWithStringTest() {
        val source = ("000000000000000000000000000000000000000000000000000000000000000a" +
                "0000000000000000000000000000000000000000000000000000000000000001" +
                "000000000000000000000000000000000000000000000000000000000000002" +
                "00000000000000000000000000000000000000000000000000000000000000003545258" +
                "0000000000000000000000000000000000000000000000000000000000").toByteArray()

        val decoder = ContractOutputDecoder()
        val result =
            decoder.decode(
                source,
                listOf(NumberOutputValueType(), BooleanOutputValueType(), StringOutputValueType())
            )

        assertEquals(result.first().value as Long, 10)
        assertTrue(result[1].value as Boolean)
        assertEquals(result.last().value.toString(), "TRX")
    }

    @Test
    fun decodeAddressTest() {
        val source =
            "0000000000000000000000000000000000000000000000000000000000000016".toByteArray()

        val decoder = ContractOutputDecoder()
        val result = decoder.decode(source, listOf(AddressOutputValueType()))

        assertEquals(result.first().value.toString(), "22")
    }

    @Test
    fun decodeAddressListTest() {
        val source =
            ("00000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000000002" +
                    "000000000000000000000000000000000000000000000000000000000000028d0000000000000000000000000000000000000000000000000000000000000040" +
                    "0000000000000000000000000000000000000000000000000000000000000001").toByteArray()

        val decoder = ContractOutputDecoder()
        val result = decoder.decode(
            source,
            listOf(ListValueType(AddressOutputValueType()), BooleanOutputValueType())
        )

        assertNotNull(result.first().value)
        assertTrue(result[1].value as Boolean)
    }

    @Test
    fun decodeFixedBytesTest() {
        val source =
            ("64696d6100000000000000000000000000000000000000000000000000000000").toByteArray()

        val decoder = ContractOutputDecoder()
        val result = decoder.decode(
            source,
            listOf(FixedBytesOutputValueType())
        )

        assertNotNull(result.first().value)
    }

    @Test
    fun decodeFixedUInt32ArrayTest() {
        val source =
            ("0000000000000000000000000000000000000000000000000000000000000001" +
                    "0000000000000000000000000000000000000000000000000000000000000002" +
                    "0000000000000000000000000000000000000000000000000000000000000003").toByteArray()

        val decoder = ContractOutputDecoder()
        val result = decoder.decode(
            source,
            listOf(FixedArrayOutputValueType(3, NumberOutputValueType()))
        )

        assertTrue((result.first().value as List<Any>).isNotEmpty())
        assertEquals((result.first().value as List<Long>)[0], 1L)
    }

}