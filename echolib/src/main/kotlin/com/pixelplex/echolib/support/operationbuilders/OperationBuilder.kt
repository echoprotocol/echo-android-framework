package com.pixelplex.echolib.support.operationbuilders

import com.pixelplex.echolib.model.BaseOperation

/**
 * Base template for all operation-specific factory classes.
 *
 * @author Daria Pechkovskaya
 */
abstract class OperationBuilder<T : BaseOperation> {

    /**
     * Must be implemented and return the specific operation the
     * factory is supposed to build.
     *
     * @return: A usable instance of a given operation.
     */
    abstract fun build(): T
}
