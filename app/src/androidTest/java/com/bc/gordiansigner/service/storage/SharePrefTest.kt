package com.bc.gordiansigner.service.storage

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.bc.gordiansigner.RxTest
import com.bc.gordiansigner.service.storage.sharedpref.SharedPrefApi
import com.bc.gordiansigner.service.storage.sharedpref.rxCompletable
import com.bc.gordiansigner.service.storage.sharedpref.rxSingle
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.reflect.KClass

@RunWith(AndroidJUnit4::class)
class SharePrefTest : RxTest() {

    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun testReadWriteStandardSharedPref() {
        val dataSet =
            mapOf<KClass<*>, Map<String, *>>(
                String::class to mapOf(
                    "key1" to "val",
                    "key2" to " ",
                    "key3" to "valvalvalvalvalvalvalvalvalvalvalval"
                ),
                Set::class to mapOf(
                    "key1" to setOf("1", "2", "3"),
                    "key2" to setOf("", "    ", "valvalvalvalvalvalvalvalvalvalvalval")
                )
            )

        for (d in dataSet.entries) {
            val type = d.key
            val value = d.value

            for (v in value) {
                SharedPrefApi(appContext).STANDARD.rxCompletable { pref ->
                    pref.put(
                        v.key,
                        v.value
                    )
                }
                    .test()
                    .assertComplete()
                    .assertNoErrors()
                v
                SharedPrefApi(appContext).STANDARD.rxSingle { pref ->
                    pref.get(
                        v.key,
                        type
                    )
                }
                    .test()
                    .assertComplete()
                    .assertNoErrors()
                    .assertValue(v.value)
            }


        }
    }

    @Test
    fun testReadWriteSecureSharedPref() {
        val dataSet =
            mapOf("key1" to "val", "key2" to " ", "key3" to "valvalvalvalvalvalvalvalvalvalvalval")

        for (d in dataSet.entries) {
            SharedPrefApi(appContext).SECURE.rxCompletable { pref ->
                pref.put(d.key, d.value)
            }.test().assertComplete().assertNoErrors()

            SharedPrefApi(appContext).SECURE.rxSingle { pref ->
                pref.get(d.key, String::class)
            }
                .test()
                .assertComplete()
                .assertNoErrors()
                .assertValue(d.value)

        }
    }

    @Test
    fun testDeleteStandardSharedPref() {
        val dataSet =
            mapOf("key1" to "val", "key2" to " ", "key3" to "valvalvalvalvalvalvalvalvalvalvalval")

        for (d in dataSet.entries) {
            SharedPrefApi(appContext).STANDARD.rxCompletable { pref -> pref.put(d.key, d.value) }
                .test()
                .assertComplete()
                .assertNoErrors()

            SharedPrefApi(appContext).STANDARD.rxCompletable { pref -> pref.clear(d.key) }
                .test()
                .assertComplete()
                .assertNoErrors()

            SharedPrefApi(appContext).STANDARD.rxSingle { pref -> pref.get(d.key, String::class) }
                .test()
                .assertComplete()
                .assertNoErrors()
                .assertValue("")

        }
    }

    @Test
    fun testDeleteSecureSharedPref() {
        val dataSet =
            mapOf("key1" to "val", "key2" to " ", "key3" to "valvalvalvalvalvalvalvalvalvalvalval")

        for (d in dataSet.entries) {
            SharedPrefApi(appContext).SECURE.rxCompletable { pref -> pref.put(d.key, d.value) }
                .test()
                .assertComplete()
                .assertNoErrors()


            SharedPrefApi(appContext).SECURE.rxCompletable { pref -> pref.clear(d.key) }
                .test()
                .assertComplete()
                .assertNoErrors()

            SharedPrefApi(appContext).SECURE.rxSingle { pref -> pref.get(d.key, String::class) }
                .test()
                .assertComplete()
                .assertNoErrors()
                .assertValue("")

        }
    }

}