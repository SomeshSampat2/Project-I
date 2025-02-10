package com.innovatelabs3.projectI2.utils

import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import java.io.File

data class FileSearchResult(
    val name: String,
    val path: String,
    val type: FileType
)

enum class FileType {
    IMAGE, VIDEO, AUDIO, DOCUMENT, PDF, OTHER
}

object FileSearchUtils {
    fun searchFiles(context: Context, query: String): List<FileSearchResult> {
        val results = mutableListOf<FileSearchResult>()

        // Search for Images
        searchImages(context, query, results)

        // Search for Videos
        searchVideos(context, query, results)

        // Search for Audio
        searchAudio(context, query, results)

        // Search for Documents
        searchDocuments(context, query, results)

        return results
    }

    private fun searchImages(
        context: Context,
        query: String,
        results: MutableList<FileSearchResult>
    ) {
        val projection = arrayOf(
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media._ID
        )

        val selection = "${MediaStore.Images.Media.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$query%")

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

            while (cursor.moveToNext()) {
                val name = cursor.getString(nameColumn)
                val path = cursor.getString(pathColumn)
                results.add(FileSearchResult(name, path, FileType.IMAGE))
            }
        }
    }

    private fun searchVideos(
        context: Context,
        query: String,
        results: MutableList<FileSearchResult>
    ) {
        val projection = arrayOf(
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATA
        )

        val selection = "${MediaStore.Video.Media.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$query%")

        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)

            while (cursor.moveToNext()) {
                val name = cursor.getString(nameColumn)
                val path = cursor.getString(pathColumn)
                results.add(FileSearchResult(name, path, FileType.VIDEO))
            }
        }
    }

    private fun searchAudio(
        context: Context,
        query: String,
        results: MutableList<FileSearchResult>
    ) {
        val projection = arrayOf(
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATA
        )

        val selection = "${MediaStore.Audio.Media.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$query%")

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (cursor.moveToNext()) {
                val name = cursor.getString(nameColumn)
                val path = cursor.getString(pathColumn)
                results.add(FileSearchResult(name, path, FileType.AUDIO))
            }
        }
    }

    private fun searchDocuments(
        context: Context,
        query: String,
        results: MutableList<FileSearchResult>
    ) {
        // Define projections
        val projection = arrayOf(
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.MIME_TYPE
        )

        // Separate PDF search from other documents
        searchPdfFiles(context, query, results, projection)
        searchOtherDocuments(context, query, results, projection)
    }

    private fun searchPdfFiles(
        context: Context,
        query: String,
        results: MutableList<FileSearchResult>,
        projection: Array<String>
    ) {
        val selection = """
            ${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ? AND 
            (${MediaStore.Files.FileColumns.MIME_TYPE} = ? OR 
             ${MediaStore.Files.FileColumns.MIME_TYPE} = ? OR
             ${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ?)
        """.trimIndent()

        val selectionArgs = arrayOf(
            "%$query%",
            "application/pdf",
            "application/x-pdf",
            "%.pdf"
        )

        try {
            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                val nameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)

                while (cursor.moveToNext()) {
                    val name = cursor.getString(nameColumn)
                    val path = cursor.getString(pathColumn)

                    // Additional check for PDF extension
                    if (name.lowercase().endsWith(".pdf")) {
                        results.add(FileSearchResult(name, path, FileType.PDF))
                    }
                }
            }

            // Also search in common PDF directories
            val commonPdfDirs = listOf(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                File(Environment.getExternalStorageDirectory(), "PDFs")
            )

            commonPdfDirs.forEach { dir ->
                if (dir.exists() && dir.isDirectory) {
                    searchDirectoryForPdfs(dir, query, results)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun searchDirectoryForPdfs(
        directory: File,
        query: String,
        results: MutableList<FileSearchResult>
    ) {
        directory.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                searchDirectoryForPdfs(file, query, results)
            } else if (file.name.lowercase().endsWith(".pdf") &&
                file.name.lowercase().contains(query.lowercase())
            ) {
                results.add(
                    FileSearchResult(
                        name = file.name,
                        path = file.absolutePath,
                        type = FileType.PDF
                    )
                )
            }
        }
    }

    private fun searchOtherDocuments(
        context: Context,
        query: String,
        results: MutableList<FileSearchResult>,
        projection: Array<String>
    ) {
        val mimeTypes = arrayOf(
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain"
        )

        val mimeTypeSelection = mimeTypes.joinToString(" OR ") {
            "${MediaStore.Files.FileColumns.MIME_TYPE} = ?"
        }

        val selection =
            "(${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ?) AND ($mimeTypeSelection)"
        val selectionArgs = arrayOf("%$query%") + mimeTypes

        context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val mimeTypeColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

            while (cursor.moveToNext()) {
                val name = cursor.getString(nameColumn)
                val path = cursor.getString(pathColumn)
                val mimeType = cursor.getString(mimeTypeColumn)

                val type = when {
                    mimeType.contains("pdf") -> FileType.PDF
                    mimeType.contains("word") || mimeType.contains("text") -> FileType.DOCUMENT
                    else -> FileType.OTHER
                }

                results.add(FileSearchResult(name, path, type))
            }
        }
    }

    fun formatSearchResults(results: List<FileSearchResult>): String {
        if (results.isEmpty()) {
            return "I couldn't find any files matching your search."
        }

        return "Here are the files I found. They are displayed below."
    }
} 