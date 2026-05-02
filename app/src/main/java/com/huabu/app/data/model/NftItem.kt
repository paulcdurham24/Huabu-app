package com.huabu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class NftChain { ETHEREUM, POLYGON, SOLANA, BASE, ARBITRUM }

@Entity(tableName = "nft_items")
data class NftItem(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val collection: String,
    val chain: NftChain = NftChain.ETHEREUM,
    val priceEth: Float = 0f,
    val lastSaleEth: Float = 0f,
    val imageUrl: String = "",
    val openseaUrl: String = "",
    val acquiredAt: Long = System.currentTimeMillis()
)
