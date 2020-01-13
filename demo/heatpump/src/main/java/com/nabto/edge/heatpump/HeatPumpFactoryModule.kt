package com.nabto.edge.heatpump

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import dagger.Module
import dagger.Provides

@Module
class HeatPumpFactoryModule {

    @Provides
    fun providesViewModelFactory(modelFactory: ScanViewModelFactory): ViewModelProvider.Factory = modelFactory

    @Provides
    fun providesFragmentFactory(fragmentFactory: HeatPumpFragmentFactory) : FragmentFactory = fragmentFactory
}