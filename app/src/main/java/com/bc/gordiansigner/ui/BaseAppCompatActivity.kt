package com.bc.gordiansigner.ui

import android.content.Intent
import android.os.Bundle
import androidx.annotation.LayoutRes
import com.bc.gordiansigner.di.DaggerAppCompatActivity

abstract class BaseAppCompatActivity : DaggerAppCompatActivity() {
    private val lifecycleObserves = mutableListOf<ComponentLifecycleObserver>()

    protected fun addLifecycleObserver(observer: ComponentLifecycleObserver) {
        if (lifecycleObserves.contains(observer)) return
        lifecycleObserves.add(observer)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleObserves.forEach { o -> o.onCreate() }
        if (viewModel() != null) {
            lifecycle.addObserver(viewModel()!!)
        }
        requestFeatures()
        setContentView(layoutRes())
        initComponents()
        observe()
    }

    override fun onStart() {
        super.onStart()
        lifecycleObserves.forEach { o -> o.onStart() }
    }

    override fun onResume() {
        super.onResume()
        lifecycleObserves.forEach { o -> o.onResume() }
    }

    override fun onPause() {
        lifecycleObserves.forEach { o -> o.onPause() }
        super.onPause()
    }

    override fun onStop() {
        lifecycleObserves.forEach { o -> o.onStop() }
        super.onStop()
    }

    override fun onDestroy() {
        unobserve()
        deinitComponents()
        if (viewModel() != null) {
            lifecycle.removeObserver(viewModel()!!)
        }
        lifecycleObserves.forEach { o -> o.onDestroy() }
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        lifecycleObserves.forEach { o ->
            o.onActivityResult(
                requestCode,
                resultCode,
                data
            )
        }
    }

    // Layout resource ID for the activity
    @LayoutRes
    protected abstract fun layoutRes(): Int

    // ViewModel instance
    protected abstract fun viewModel(): BaseViewModel?

    // Callback to init components like view
    protected open fun initComponents() {}

    // Callback to de-init the components
    protected open fun deinitComponents() {}

    // Callback for observe data from ViewModel
    protected open fun observe() {}

    // Callback for unobserve data ViewModel
    protected open fun unobserve() {}

    // Callback to request Android features
    protected open fun requestFeatures() {}
}