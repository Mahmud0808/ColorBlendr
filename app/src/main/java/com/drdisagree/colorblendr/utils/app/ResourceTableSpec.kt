package com.drdisagree.colorblendr.utils.app

import java.io.EOFException
import java.io.InputStream
import java.util.zip.ZipFile

object ResourceTableSpec {

    private const val RESOURCE_TABLE = "resources.arsc"
    private const val RES_TABLE_TYPE = 0x0002
    private const val RES_TABLE_PACKAGE_TYPE = 0x0200
    private const val RES_TABLE_TYPE_SPEC_TYPE = 0x0202
    private const val CHUNK_HEADER_SIZE = 8

    fun entryCounts(apkPath: String): Map<Int, Int> = runCatching {
        ZipFile(apkPath).use { zip ->
            val entry = zip.getEntry(RESOURCE_TABLE) ?: return@use emptyMap()
            zip.getInputStream(entry).buffered().use { input ->
                buildMap { readTable(TableReader(input), this) }
            }
        }
    }.getOrDefault(emptyMap())

    private fun readTable(reader: TableReader, counts: MutableMap<Int, Int>) {
        val table = reader.chunk() ?: return
        if (table.type != RES_TABLE_TYPE) return
        reader.forEachChild(table) { child ->
            if (child.type == RES_TABLE_PACKAGE_TYPE) readPackage(reader, child, counts)
        }
    }

    private fun readPackage(reader: TableReader, pkg: Chunk, counts: MutableMap<Int, Int>) {
        val packageId = reader.u32()
        reader.forEachChild(pkg) { child ->
            if (child.type != RES_TABLE_TYPE_SPEC_TYPE) return@forEachChild
            val typeId = reader.u8()
            reader.skip(3)
            counts[(packageId shl 24) or (typeId shl 16)] = reader.u32()
        }
    }

    private class Chunk(val type: Int, val start: Long, val headerSize: Int, val size: Long)

    private class TableReader(private val input: InputStream) {

        private var position = 0L

        fun chunk(): Chunk? {
            val start = position
            val type = u16()
            val headerSize = u16()
            val size = u32().toLong() and 0xffffffffL
            if (headerSize < CHUNK_HEADER_SIZE || size < headerSize) return null
            return Chunk(type, start, headerSize, size)
        }

        inline fun forEachChild(parent: Chunk, action: (Chunk) -> Unit) {
            val end = parent.start + parent.size
            skipTo(parent.start + parent.headerSize)
            while (position + CHUNK_HEADER_SIZE <= end) {
                val child = chunk() ?: return
                if (child.start + child.size > end) return
                action(child)
                skipTo(child.start + child.size)
            }
        }

        fun u8(): Int = input.read().also {
            if (it < 0) throw EOFException()
            position++
        }

        fun u16(): Int = u8() or (u8() shl 8)

        fun u32(): Int = u16() or (u16() shl 16)

        fun skip(count: Int) = skipTo(position + count)

        private fun skipTo(target: Long) {
            while (position < target) {
                val skipped = input.skip(target - position)
                if (skipped > 0) position += skipped else u8()
            }
        }
    }
}
