package org.echo.mobile.framework.support

/**
 * Simple value converter interface
 *
 * @author Dmitriy Bushuev.
 */
interface Converter<I, O> {

    /**
     * Converts input value of type [I] to value with type [O]
     */
    fun convert(source: I): O

}
