package org.echo.mobile.framework.model.contract

import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.model.contract.input.AccountAddressInputValueType
import org.echo.mobile.framework.model.contract.input.AddressInputValueType
import org.echo.mobile.framework.model.contract.input.BooleanInputValueType
import org.echo.mobile.framework.model.contract.input.BytesInputValueType
import org.echo.mobile.framework.model.contract.input.ContractAddressInputValueType
import org.echo.mobile.framework.model.contract.input.ContractInputEncoder
import org.echo.mobile.framework.model.contract.input.DynamicArrayInputValueType
import org.echo.mobile.framework.model.contract.input.DynamicStringArrayValueType
import org.echo.mobile.framework.model.contract.input.EthContractAddressInputValueType
import org.echo.mobile.framework.model.contract.input.FixedArrayInputValueType
import org.echo.mobile.framework.model.contract.input.FixedBytesInputValueType
import org.echo.mobile.framework.model.contract.input.InputValue
import org.echo.mobile.framework.model.contract.input.NumberInputValueType
import org.echo.mobile.framework.model.contract.input.StringInputValueType
import org.junit.Assert
import org.junit.Test

/**
 * Test cases for [ContractInputEncoder]
 *
 * @author Dmitriy Bushuev
 */
class ContractInputEncoderTest {

    private val contractInputEncoder by lazy {
        ContractInputEncoder()
    }

    @Test
    fun encodeEmptyFunctionTest() {
        val function = "totalSupply"
        val result = "18160ddd"

        val encoded = contractInputEncoder.encode(function)
        Assert.assertEquals(result, encoded)
    }

    @Test
    fun encodeFunctionWithBooleanTest() {
        val function = "totalSupply"
        val result = "899426490000000000000000000000000000000000000000000000000000000000000001"

        val encoded = contractInputEncoder.encode(
            function,
            listOf(InputValue(BooleanInputValueType(), "true"))
        )

        Assert.assertEquals(result, encoded)
    }

    @Test
    fun encodeFunctionWithIntTest() {
        val function = "totalSupply"
        val result = "bd85b0390000000000000000000000000000000000000000000000000000000000000005"

        val encoded = contractInputEncoder.encode(
            function,
            listOf(InputValue(NumberInputValueType("uint256"), "5"))
        )

        Assert.assertEquals(result, encoded)
    }

    @Test
    fun encodeFunctionWithBytesTest() {
        val result = "000000000000000000000000000000000000000000000000000000000000002" +
                "0000000000000000000000000000000000000000000000000000000000000000133" +
                "00000000000000000000000000000000000000000000000000000000000000"

        val encoded = contractInputEncoder.encodeArguments(
            listOf(InputValue(BytesInputValueType(), "3"))
        )

        Assert.assertEquals(result, encoded)
    }

    @Test
    fun encodeFunctionWithAddressTest() {
        val function = "totalSupply"
        val result = "e4dc2aa40000000000000000000000000000000000000000000000000000000000000012"

        val encoded =
            contractInputEncoder.encode(
                function,
                listOf(InputValue(AddressInputValueType(), "1.2.18"))
            )

        Assert.assertEquals(result, encoded)
    }

    @Test(expected = LocalException::class)
    fun encodeFunctionWithAddressFailureTest() {
        val function = "totalSupply"
        val result = "e4dc2aa40000000000000000000000000000000000000000000000000000000000000012"

        val encoded =
            contractInputEncoder.encode(function, listOf(InputValue(AddressInputValueType(), "18")))

        Assert.assertEquals(result, encoded)
    }

    @Test
    fun encodeFunctionWithAccountAddressTest() {
        val function = "totalSupply"
        val result = "e4dc2aa40000000000000000000000000000000000000000000000000000000000000012"

        val encoded =
            contractInputEncoder.encode(
                function,
                listOf(InputValue(AccountAddressInputValueType(), "1.2.18"))
            )

        Assert.assertEquals(result, encoded)
    }

    @Test
    fun encodeFunctionWithContractAddressTest() {
        val function = "totalSupply"
        val result = "e4dc2aa40000000000000000000000000100000000000000000000000000000000000012"

        val encoded =
            contractInputEncoder.encode(
                function,
                listOf(InputValue(ContractAddressInputValueType(), "1.16.18"))
            )

        Assert.assertEquals(result, encoded)
    }

    @Test
    fun encodeFunctionWithStringTest() {
        val function = "totalSupply"
        val result = "c415db130000000000000000000000000000000000000000000000000000000000000020" +
                "00000000000000000000000000000000000000000000000000000000000000046e616d6500000000000000000000000000000000000000000000000000000000"

        val encoded =
            contractInputEncoder.encode(
                function,
                listOf(InputValue(StringInputValueType(), "name"))
            )

        Assert.assertEquals(result, encoded)
    }

    @Test
    fun encodeUint256AndStringTest() {
        val function = "totalSupply"
        val result = "d2233d77000000000000000000000000000000000000000000000000000000000000007b" +
                "0000000000000000000000000000000000000000000000000000000000000040000000000000000000000" +
                "000000000000000000000000000000000000000000d48656c6c6f2c20576f726c642100000000000000000000000000000000000000"

        val params = listOf(
            InputValue(NumberInputValueType("uint256"), "123"),
            InputValue(StringInputValueType(), "Hello, World!")
        )

        val encoded = contractInputEncoder.encode(function, params)
        Assert.assertEquals(result, encoded)
    }


    @Test
    fun encodeEthContractAddressTest() {
        val decoded = "000000000000000000000000ca35b7d915458ef540ade6068dfe2f44e8fa733c"
        val target = "0xca35b7d915458ef540ade6068dfe2f44e8fa733c"

        val params = listOf(
            InputValue(EthContractAddressInputValueType("address"), target)
        )

        val encoded = contractInputEncoder.encodeArguments(params)
        Assert.assertEquals(encoded, decoded)
    }

    @Test
    fun encodeEthContractAddressWithoutPrefixTest() {
        val decoded = "000000000000000000000000ca35b7d915458ef540ade6068dfe2f44e8fa733c"
        val target = "ca35b7d915458ef540ade6068dfe2f44e8fa733c"

        val params = listOf(
            InputValue(EthContractAddressInputValueType("address"), target)
        )

        val encoded = contractInputEncoder.encodeArguments(params)
        Assert.assertEquals(encoded, decoded)
    }

    @Test
    fun encodeUIntAndStringAndAddressTest() {
        val function = "totalSupply"
        val result = "4b0182a5000000000000000000000000000000000000000000000000000000000000007b" +
                "0000000000000000000000000000000000000000000000000000000000000060000000000000000000000" +
                "00000000000000000000000000000000000000000120000000000000000000000000000000000000000000" +
                "00000000000000000000d48656c6c6f2c20576f726c642100000000000000000000000000000000000000"

        val params = listOf(
            InputValue(NumberInputValueType("uint"), "123"),
            InputValue(StringInputValueType(), "Hello, World!"),
            InputValue(AddressInputValueType(), "1.2.18")
        )

        val encoded = contractInputEncoder.encode(function, params)
        Assert.assertEquals(result, encoded)
    }

    @Test
    fun encodeBoolAndStringAndStringAndUint8AndBoolTest() {
        val result = "0000000000000000000000000000000000000000000000000000000000000001" +
                "00000000000000000000000000000000000000000000000000000000000000a" +
                "000000000000000000000000000000000000000000000000000000000000000e" +
                "000000000000000000000000000000000000000000000000000000000000000ff" +
                "0000000000000000000000000000000000000000000000000000000000000000" +
                "00000000000000000000000000000000000000000000000000000000000000027364" +
                "000000000000000000000000000000000000000000000000000000000000000000000" +
                "000000000000000000000000000000000000000000000000000000361736400000000" +
                "00000000000000000000000000000000000000000000000000"

        val params = listOf(
            InputValue(BooleanInputValueType(), "true"),
            InputValue(StringInputValueType(), "sd"),
            InputValue(StringInputValueType(), "asd"),
            InputValue(NumberInputValueType("uint8"), "255"),
            InputValue(BooleanInputValueType(), "false")
        )

        val encoded = contractInputEncoder.encodeArguments(params)
        Assert.assertEquals(result, encoded)
    }

    @Test
    fun dynamicIntArrayTest() {
        val result = "000000000000000000000000000000000000000000000000000000000000006" +
                "000000000000000000000000000000000000000000000000000000000000000010000000000" +
                "0000000000000000000000000000000000000000000000000000a0000000000000000000000" +
                "000000000000000000000000000000000000000000464617665000000000000000000000000" +
                "000000000000000000000000000000000000000000000000000000000000000000000000000" +
                "000000000000000000003000000000000000000000000000000000000000000000000000000" +
                "000000000100000000000000000000000000000000000000000000000000000000000000020" +
                "000000000000000000000000000000000000000000000000000000000000003"

        val params = listOf(
            InputValue(
                StringInputValueType(),
                "dave"
            ),
            InputValue(
                BooleanInputValueType(),
                "true"
            ),
            InputValue(
                DynamicArrayInputValueType(NumberInputValueType("uint256")),
                "[1,2,3]"
            )
        )

        val encoded = contractInputEncoder.encodeArguments(params)
        Assert.assertEquals(result, encoded)
    }

    @Test
    fun encodeEtherExampleUintAndDynamicArrayOfUintsAndBytes10AndBytesTest() {
        val result = "0000000000000000000000000000000000000000000000000000000000000001" +
                "0000000000000000000000000000000000000000000000000000000000000080313233343536" +
                "3738393000000000000000000000000000000000000000000000000000000000000000000000" +
                "00000000000000000000000000000000000000e0000000000000000000000000000000000000" +
                "0000000000000000000000000002000000000000000000000000000000000000000000000000" +
                "0000000000000002000000000000000000000000000000000000000000000000000000000000" +
                "0003000000000000000000000000000000000000000000000000000000000000000d48656c6c" +
                "6f2c20776f726c642100000000000000000000000000000000000000"

        val params = listOf(
            InputValue(
                NumberInputValueType("uint256"),
                "1"
            ),
            InputValue(
                DynamicArrayInputValueType(NumberInputValueType("uint256")),
                "[2,3]"
            ),
            InputValue(
                FixedBytesInputValueType(10),
                "1234567890"
            ),
            InputValue(
                BytesInputValueType(),
                "Hello, world!"
            )
        )

        val encoded = contractInputEncoder.encodeArguments(params)
        Assert.assertEquals(result, encoded)
    }

    @Test
    fun encodeUintAndDynamicArrayOfBytes32AndBytes10AndBytesTest() {
        val result = "0000000000000000000000000000000000000000000000000000000000000001" +
                "0000000000000000000000000000000000000000000000000000000000000080313233343536" +
                "3738393000000000000000000000000000000000000000000000000000000000000000000000" +
                "00000000000000000000000000000000000000e0000000000000000000000000000000000000" +
                "0000000000000000000000000002313233343536373839300000000000000000000000000000" +
                "0000000000000000313233343536373839300000000000000000000000000000000000000000" +
                "0000000000000000000000000000000000000000000000000000000000000000000d48656c6c" +
                "6f2c20776f726c642100000000000000000000000000000000000000"

        val params = listOf(
            InputValue(
                NumberInputValueType("uint256"),
                "1"
            ),
            InputValue(
                DynamicArrayInputValueType(FixedBytesInputValueType(32)),
                "[1234567890,1234567890]"
            ),
            InputValue(
                FixedBytesInputValueType(10),
                "1234567890"
            ),
            InputValue(
                BytesInputValueType(),
                "Hello, world!"
            )
        )

        val encoded = contractInputEncoder.encodeArguments(params)
        Assert.assertEquals(result, encoded)
    }

    @Test
    fun dynamicStringArrayTest() {
        val result = "000000000000000000000000000000000000000000000000000000000000002" +
                "000000000000000000000000000000000000000000000000000000000000000030000000000" +
                "00000000000000000000000000000000000000000000000000000d48656c6c6f2c20776f726" +
                "c64210000000000000000000000000000000000000000000000000000000000000000000000" +
                "0000000000000000000000000000000d48656c6c6f2c20776f726c642100000000000000000" +
                "000000000000000000000000000000000000000000000000000000000000000000000000000" +
                "000000000d48656c6c6f2c20776f726c642100000000000000000000000000000000000000"

        val params = listOf(
            InputValue(
                DynamicStringArrayValueType(),
                """["Hello, world!","Hello, world!","Hello, world!"]"""
            )
        )

        val encoded = contractInputEncoder.encodeArguments(params)
        Assert.assertEquals(result, encoded)
    }

    @Test
    fun encodeUintAndFixedArrayOfBytes32AndBytes10AndBytesTest() {
        val result = "0000000000000000000000000000000000000000000000000000000000000001" +
                "0000000000000000000000000000000000000000000000000000000000000080313233343536" +
                "3738393000000000000000000000000000000000000000000000000000000000000000000000" +
                "00000000000000000000000000000000000000e0000000000000000000000000000000000000" +
                "0000000000000000000000000002313233343536373839300000000000000000000000000000" +
                "0000000000000000313233343536373839300000000000000000000000000000000000000000" +
                "0000000000000000000000000000000000000000000000000000000000000000000d48656c6c" +
                "6f2c20776f726c642100000000000000000000000000000000000000"

        val params = listOf(
            InputValue(
                NumberInputValueType("uint256"),
                "1"
            ),
            InputValue(
                FixedArrayInputValueType(2, FixedBytesInputValueType(32)),
                "[1234567890,1234567890]"
            ),
            InputValue(
                FixedBytesInputValueType(10),
                "1234567890"
            ),
            InputValue(
                BytesInputValueType(),
                "Hello, world!"
            )
        )

        val encoded = contractInputEncoder.encodeArguments(params)
        Assert.assertEquals(result, encoded)
    }

}
