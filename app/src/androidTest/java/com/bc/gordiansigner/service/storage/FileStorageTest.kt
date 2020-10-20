package com.bc.gordiansigner.service.storage

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.bc.gordiansigner.RxTest
import com.bc.gordiansigner.service.storage.file.FileStorageApi
import com.bc.gordiansigner.service.storage.file.rxCompletable
import com.bc.gordiansigner.service.storage.file.rxSingle
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FileStorageTest : RxTest() {

    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var filePrefix: String

    @Before
    fun beforeEach() {
        filePrefix = "${System.currentTimeMillis()}"
    }

    @After
    fun afterEach() {
        appContext.filesDir.listFiles()?.let { files -> files.forEach { it.deleteRecursively() } }
    }

    @Test
    fun testReadWriteStandardFileGateway() {
        val dataSet =
            mapOf(
                "$filePrefix-test.txt" to "test_content_1",
                "$filePrefix-test.key" to "",
                "$filePrefix-test" to "valvalvalvalvalvalvalvalvalvalvalval"
            )

        for (d in dataSet.entries) {
            FileStorageApi(appContext).STANDARD.rxCompletable { gw ->
                gw.writeOnFilesDir(
                    d.key,
                    d.value.toByteArray()
                )
            }
                .test()
                .assertComplete()
                .assertNoErrors()

            FileStorageApi(appContext).STANDARD.rxSingle { gw -> String(gw.readOnFilesDir(d.key)) }
                .test()
                .assertComplete()
                .assertNoErrors()
                .assertValue(d.value)
        }
    }

    @Test
    fun testReadWriteSecureFileGateway() {
        val dataSet =
            mapOf(
                "$filePrefix-test_secure.txt" to "test_content_1",
                "$filePrefix-test_secure.key" to "",
                "$filePrefix-test_secure" to "valvalvalvalvalvalvalvalvalvalvalval"
            )

        for (d in dataSet.entries) {
            FileStorageApi(appContext).SECURE.rxCompletable { gw ->
                gw.writeOnFilesDir(
                    d.key,
                    d.value.toByteArray()
                )
            }
                .test()
                .assertComplete()
                .assertNoErrors()

            FileStorageApi(appContext).SECURE.rxSingle { gw -> String(gw.readOnFilesDir(d.key)) }
                .test()
                .assertComplete()
                .assertNoErrors()
                .assertValue(d.value)
        }
    }

}