/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bc.gordiansigner.service.storage.file

import android.content.Context
import javax.inject.Inject

class FileStorageApi @Inject constructor(context: Context) {

    val STANDARD = lazy { StandardFileGateway(context) }.value

    val SECURE = lazy { SecureFileGateway(context) }.value

    val SUPER_SECURE = lazy { SuperSecureFileGateway(context) }.value

}