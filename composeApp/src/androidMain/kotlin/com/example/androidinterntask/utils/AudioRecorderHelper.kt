package com.example.androidinterntask.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

class AudioRecorderHelper(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var isRecording = false

    companion object {
        private const val TAG = "AudioRecorderHelper"
    }

    fun startRecording(fileName: String): Boolean {
        // Clean up any existing recorder first
        if (isRecording) {
            Log.w(TAG, "Already recording, stopping previous recording")
            stopRecording()
        }

        return try {
            // Create output directory
            val directory = File(context.filesDir, "recordings")
            if (!directory.exists()) {
                val created = directory.mkdirs()
                Log.d(TAG, "Directory created: $created, path: ${directory.absolutePath}")
            }

            // Create output file
            outputFile = File(directory, fileName)
            Log.d(TAG, "Output file: ${outputFile?.absolutePath}")

            // Initialize MediaRecorder
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mediaRecorder = recorder

            try {
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                recorder.setAudioEncodingBitRate(128000)
                recorder.setAudioSamplingRate(44100)
                recorder.setOutputFile(outputFile?.absolutePath)

                recorder.prepare()
                recorder.start()
                isRecording = true
                Log.d(TAG, "Recording started successfully")
                true
            } catch (e: IOException) {
                Log.e(TAG, "IOException during recording setup", e)
                cleanupRecorder()
                false
            } catch (e: IllegalStateException) {
                Log.e(TAG, "IllegalStateException during recording setup", e)
                cleanupRecorder()
                false
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected exception during recording setup", e)
                cleanupRecorder()
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in startRecording", e)
            cleanupRecorder()
            false
        }
    }

    fun stopRecording(): String? {
        if (!isRecording) {
            Log.w(TAG, "Not recording, nothing to stop")
            return null
        }

        return try {
            mediaRecorder?.apply {
                try {
                    stop()
                    Log.d(TAG, "Recording stopped successfully")
                } catch (e: IllegalStateException) {
                    Log.e(TAG, "IllegalStateException when stopping", e)
                } catch (e: RuntimeException) {
                    Log.e(TAG, "RuntimeException when stopping", e)
                }

                release()
                Log.d(TAG, "MediaRecorder released")
            }

            mediaRecorder = null
            isRecording = false

            val filePath = outputFile?.absolutePath
            Log.d(TAG, "Recording saved to: $filePath")

            // Verify file exists and has size
            outputFile?.let { file ->
                if (file.exists() && file.length() > 0) {
                    Log.d(TAG, "File verified: ${file.length()} bytes")
                    filePath
                } else {
                    Log.e(TAG, "File not found or empty")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in stopRecording", e)
            cleanupRecorder()
            null
        }
    }

    fun release() {
        try {
            if (isRecording) {
                Log.d(TAG, "Stopping recording in release()")
                mediaRecorder?.stop()
            }
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
            Log.d(TAG, "Released successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Exception in release", e)
            mediaRecorder = null
            isRecording = false
        }
    }

    private fun cleanupRecorder() {
        try {
            mediaRecorder?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Exception during cleanup", e)
        }
        mediaRecorder = null
        isRecording = false
    }

    fun isRecording() = isRecording

    fun getOutputFilePath() = outputFile?.absolutePath
}