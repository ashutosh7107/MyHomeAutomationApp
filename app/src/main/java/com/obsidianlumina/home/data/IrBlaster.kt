package com.obsidianlumina.home.data

import android.content.Context
import android.hardware.ConsumerIrManager

class IrBlaster(context: Context) {
    private val irManager: ConsumerIrManager? =
        context.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager

    fun hasEmitter(): Boolean = try {
        irManager?.hasIrEmitter() == true
    } catch (_: Exception) {
        false
    }

    /**
     * Transmits either:
     * - A 32-bit NEC hex code such as "0x00FF01FE" or "00FF01FE" at 38 kHz
     * - Or comma-separated microsecond pulse/space durations
     */
    fun transmit(codeInput: String): Boolean {
        val manager = irManager ?: return false
        if (!hasEmitter()) return false

        val clean = codeInput.trim()
        if (clean.isEmpty()) return false

        val pattern = if (clean.contains(",")) {
            clean.split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .filter { it > 0 }
                .toIntArray()
        } else {
            buildNecPattern(clean)
        }

        if (pattern.size < 3) return false

        return try {
            manager.transmit(38000, pattern)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun buildNecPattern(rawHex: String): IntArray {
        val normalized = rawHex
            .removePrefix("0x")
            .removePrefix("0X")
            .replace(" ", "")
        if (normalized.isEmpty() || normalized.length > 8) return IntArray(0)

        val value = normalized.toLongOrNull(16) ?: return IntArray(0)

        // NEC 38 kHz: 9000us mark, 4500us space, 32 bits (MSB first), 560us stop mark
        val pattern = IntArray(2 + 32 * 2 + 1)
        pattern[0] = 9000
        pattern[1] = 4500

        var idx = 2
        for (bit in 31 downTo 0) {
            val isOne = ((value ushr bit) and 1L) == 1L
            pattern[idx++] = 560
            pattern[idx++] = if (isOne) 1690 else 560
        }
        pattern[idx] = 560
        return pattern
    }
}
