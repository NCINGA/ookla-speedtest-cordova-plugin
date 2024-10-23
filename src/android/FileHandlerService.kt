package com.ncinga.speedtest

import java.io.File

interface FileHandlerService {
    fun writeFile(data: String, path: File)
}