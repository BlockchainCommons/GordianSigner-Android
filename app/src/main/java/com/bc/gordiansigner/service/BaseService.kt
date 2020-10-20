package com.bc.gordiansigner.service

import com.bc.gordiansigner.service.storage.file.FileStorageApi
import com.bc.gordiansigner.service.storage.sharedpref.SharedPrefApi


abstract class BaseService(
    protected val sharedPrefApi: SharedPrefApi,
    protected val fileStorageApi: FileStorageApi
)