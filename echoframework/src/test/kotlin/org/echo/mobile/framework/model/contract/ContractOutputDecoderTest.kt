package org.echo.mobile.framework.model.contract

import org.echo.mobile.framework.model.contract.output.*
import org.junit.Assert.*
import org.junit.Test
import java.math.BigInteger

/**
 * Test cases for [ContractOutputDecoder]
 *
 * @author Dmitriy Bushuev
 */
class ContractOutputDecoderTest {

    @Test
    fun decodeIntTest() {
        val source =
            "0000000000000000000000000000000000000000000000056bc75e2d63100000".toByteArray()

        val decoder = ContractOutputDecoder()
        val result = decoder.decode(source, listOf(NumberOutputValueType()))

        assertEquals(result.first().value as BigInteger, "100000000000000000000".toBigInteger())
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
    fun decodeAccountAddressTest() {
        val source =
            "0000000000000000000000000000000000000000000000000000000000000016".toByteArray()

        val decoder = ContractOutputDecoder()
        val result = decoder.decode(source, listOf(AccountAddressOutputValueType()))

        val value = result.first().value
        assertNotNull(value)
        assertEquals(value.toString(), "1.2.22")
    }

    @Test
    fun decodeContractAddressTest() {
        val source =
            "01000000000000000000000000000000000041a3".toByteArray()

        val decoder = ContractOutputDecoder()
        val result = decoder.decode(source, listOf(ContractAddressOutputValueType()))

        val value = result.first().value
        assertNotNull(value)
        assertEquals(value.toString(), "1.16.16803")
    }

    @Test
    fun decodeContractFullAddressTest() {
        val source =
            "00000000000000000000000001000000000000000000000000000000000041a3".toByteArray()

        val decoder = ContractOutputDecoder()
        val result = decoder.decode(source, listOf(ContractAddressOutputValueType()))

        val value = result.first().value
        assertNotNull(value)
        assertEquals(value.toString(), "1.16.16803")
    }


    @Test
    fun decodeAddressListTest() {
        val source =
            ("0000000000000000000000000000000000000000000000000000000000000020" +
                    "0000000000000000000000000000000000000000000000000000000000000002" +
                    "0000000000000000000000000000000000000000000000000000000000000016" +
                    "0000000000000000000000000100000000000000000000000000000000004be4" +
                    "0000000000000000000000000000000000000000000000000000000000000001").toByteArray()

        val decoder = ContractOutputDecoder()
        val result = decoder.decode(
            source,
            listOf(ListValueType(AddressOutputValueType()), BooleanOutputValueType())
        )

        val addressesArray = result.first().value as? List<String>
        assertNotNull(addressesArray)
        assert(addressesArray!!.size == 2)
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