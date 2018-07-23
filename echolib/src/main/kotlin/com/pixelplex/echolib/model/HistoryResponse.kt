package com.pixelplex.echolib.model

import com.google.gson.annotations.SerializedName

/**
 * This class encapsulates batch of history
 *
 * @author Dmitriy Bushuev
 */
data class HistoryResponse(@SerializedName("result") val transactions: List<HistoricalTransfer>, val hasMore: Boolean)
