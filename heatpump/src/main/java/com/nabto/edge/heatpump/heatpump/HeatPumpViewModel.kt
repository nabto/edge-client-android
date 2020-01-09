package com.nabto.edge.heatpump.heatpump

import android.view.View
import androidx.lifecycle.*
import com.nabto.edge.heatpump.data.source.overview.PairedDevice
import com.nabto.edge.heatpump.data.source.overview.PairedDevicesDao
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject


class HeatPumpViewModel : ViewModel {

    public enum class State {
        CONNECTING,
        CONNECTED,
        CLOSED
    }

    @Inject
    lateinit var pairedDevicesDao: PairedDevicesDao;

    lateinit private var pairedDevice: PairedDevice

    @Inject
    lateinit var heatPumpConnectionFactory: HeatPumpConnectionFactory
    lateinit var heatPumpConnection: HeatPumpConnection;

    var connectionState = MediatorLiveData<State>()

    var heatPumpState = MutableLiveData<HeatPumpState>()

    val shownTargetTemperature = MutableLiveData<Double>(0.0)

    val errorText = MutableLiveData<String>()

    val temperature: LiveData<Double> =
            Transformations.map(heatPumpState) { value ->
                value.temperature
            }

    val mode: LiveData<String> =
            Transformations.map(heatPumpState) { value ->
                value.mode
            }

    val power: LiveData<Boolean> =
            Transformations.map(heatPumpState) { value ->
                value.power
            }

    val target: LiveData<Double> =
            Transformations.map(heatPumpState) { value ->
                value.target
            }

    val connectingVisible: LiveData<Int> by lazy {
        Transformations.map(connectionState) { value ->
            if (value == State.CONNECTING) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    val heatPumpVisible: LiveData<Int> by lazy {
        Transformations.map(connectionState)
        {
            value ->
            if (value == State.CONNECTED) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    val reconnectVisible : LiveData<Int> by lazy {
        Transformations.map(connectionState) { value ->
            if (value == State.CLOSED) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    @Inject constructor() {
        connectionState.value = State.CONNECTING
    }

    fun setError(message: String) {
        errorText.postValue(message)
    }


    fun setDevice(productId : String, deviceId : String) {

        viewModelScope.launch {
            try {
                pairedDevice = pairedDevicesDao.getPairedDevice(productId, deviceId)
                heatPumpConnection = heatPumpConnectionFactory.createHeatPumpConnection(pairedDevice)

                connectionState.addSource(heatPumpConnection.getConnectionState()) { value ->
                    if (value == HeatPumpConnection.ConnectionState.CLOSED) {
                        connectionState.value = State.CLOSED;
                    } else if (value == HeatPumpConnection.ConnectionState.CONNECTED) {
                        connectionState.value = State.CONNECTED
                    } else if (value == HeatPumpConnection.ConnectionState.CONNECTING) {
                        connectionState.value = State.CONNECTING
                    }
                }
                connect();
            } catch (e: Exception) {
                errorText.postValue(e.message)
            }
        }
    }

    fun connect() {
        viewModelScope.launch {
            try {
                setError("");
                heatPumpConnection.connect()
                heatPumpState.postValue(heatPumpConnection.getState());
            } catch (e: Exception) {
                setError(e.message!!);
            }
        }
    }

    fun setTarget(d : Double) {
        viewModelScope.launch {
            try {
                heatPumpConnection.setTarget(d);
                updateState();
            } catch (e : Exception) {
                errorText.postValue(e.message)
            }
        }
    }

    fun setMode(mode : String) {
        viewModelScope.launch {
            try {
                heatPumpConnection.setMode(mode);
                updateState()
            } catch (e : Exception) {
                errorText.postValue(e.message)
            }
        }
    }

    fun setPower(power : Boolean) {
        viewModelScope.launch {
            try {
                heatPumpConnection.setPower(power);
                updateState();
            } catch (e : Exception) {
                errorText.postValue(e.message)
            }
        }
    }

    fun updateState() {
        viewModelScope.launch {
            try {
                val state = heatPumpConnection.getState()
                heatPumpState.postValue(state)
            } catch (e : Exception) {
                errorText.postValue(e.message)
            }
        }
    }

    val modesArray = listOf("COOL", "HEAT", "FAN", "DRY")

    val modes = MutableLiveData(modesArray);

    fun clickReconnect(view : View) {
        connect();
    }
}