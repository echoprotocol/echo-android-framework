package com.pixelplex.echolib.model

/**
 * This class encapsulates batch of history
 *
 * @author Dmitriy Bushuev
 */
data class HistoryResponse(val transactions: List<History>, val hasMore: Boolean)
