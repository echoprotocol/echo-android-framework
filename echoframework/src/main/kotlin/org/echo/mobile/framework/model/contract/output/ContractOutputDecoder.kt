package org.echo.mobile.framework.model.contract.output

/**
 * Decodes output contract data according to required output types
 *
 * @author Dmitriy Bushuev
 */
class ContractOutputDecoder {

    /**
     * Decodes [source] into convenient format according to required [types]
     */
    fun decode(source: ByteArray, types: List<OutputValueType>): List<OutputValue> {
        if (source.isEmpty()) return emptyList()

        return processOutputs(source, types)
    }

    private fun processOutputs(source: ByteArray, types: List<OutputValueType>): List<OutputValue> =
        mutableListOf<OutputValue>().apply {
            var data = source

            types.forEach { type ->
                val value = type.decode(data)
                val outputValue = OutputValue(type, value.first)

                data = value.second
                add(outputValue)
            }
        }

}
