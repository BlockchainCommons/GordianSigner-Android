package com.bc.gordiansigner

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bc.gordiansigner.util.RxImmediateSchedulerRule
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
abstract class RxTest {

    @JvmField
    @Rule
    val rxImmediateSchedulerRule = RxImmediateSchedulerRule()
}