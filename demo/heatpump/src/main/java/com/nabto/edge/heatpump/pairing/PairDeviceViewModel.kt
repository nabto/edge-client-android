package com.nabto.edge.heatpump.pairing

import android.content.SharedPreferences
import android.util.Log
import android.view.View
import androidx.lifecycle.*
import androidx.navigation.Navigation
import com.nabto.edge.heatpump.data.source.overview.PairedDevice
import com.nabto.edge.heatpump.data.source.overview.PairedDevicesDao
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.nabto.edge.heatpump.R
import com.nabto.edge.heatpump.iam.IAM

class PairDeviceViewModel @Inject constructor(
        val pairedDevicesDao : PairedDevicesDao,
        val unpairedDeviceFactory : UnpairedDeviceFactory
) : ViewModel() {


    enum class State {
        IDLE,
        CONNECTING,
        BUTTON_PAIRING,
        ERROR,
        DONE
    }

    val state = MutableLiveData<State>()

    @Inject
    lateinit var sharedPreferences : SharedPreferences

    lateinit var unpairedDevice : UnpairedDevice

    val productId = MutableLiveData<String>(null)
    var deviceId = MutableLiveData<String>( null)
    var serverKey = MutableLiveData<String>(null)
    var serverUrl = MutableLiveData<String>(null)
    var errorMessage = MutableLiveData<String>(null)

    fun localConnect(productId : String, deviceId : String){
        this.productId.value = productId
        this.deviceId.value = deviceId
        unpairedDevice = unpairedDeviceFactory.createUnpairedDevice(productId,deviceId)
        connect()

    }

    fun onClickConnect(view : View) {
        val navController = Navigation.findNavController(view)
        navController.navigate(R.id.pair_device_dest)
        unpairedDevice = unpairedDeviceFactory.createUnPairedDevice(productId.value.orEmpty(), deviceId.value.orEmpty(), serverUrl.value.orEmpty(), serverKey.value.orEmpty())
        connect()
    }

    fun setError(message : String) {
        errorMessage.postValue(message);
        state.postValue(State.ERROR);
    }

    fun setState(state : State) {
        this.state.value = state;
        errorMessage.postValue("")
    }

    fun connect() {
        setState(State.CONNECTING)
        viewModelScope.launch {
            try {
                unpairedDevice.connect()

                val serverKey = "sk-d8254c6f790001003d0c842d1b63b134";

                val iam = IAM(unpairedDevice.getConnection());

                val pairing = iam.getPairing();
                try {
                    val user = iam.getMe();
                    // already paired
                } catch (e : java.lang.Exception) {
                    // Not paired
                    if (pairing.modes.contains("LocalInitial")) {
                        iam.localInitialPairing();
                    } else {
                        throw Exception("No supported pairing modes");
                    }
                }
                val user = iam.getMe();
                pairedDevicesDao.insert(PairedDevice(pairing.productId, pairing.deviceId, "", serverKey, unpairedDevice.getDeviceFingerprint(), "heat pump", user.sct));
                setState(State.DONE);
            } catch (e: Exception) {
                setError(e.message!!)
            }
        }
    }

    val errorVisible : LiveData<Int> =
         Transformations.map(state) { value ->
             when( value) {
                 State.ERROR -> View.VISIBLE
                 else -> View.GONE
             }
         }

    val progressBarVisible : LiveData<Int> =
            Transformations.map(state) { value ->
                Log.d("pair device view model", value.toString());
                when (value ) {
                    State.CONNECTING, State.BUTTON_PAIRING -> View.VISIBLE
                    else -> View.GONE
                }
            }

    val stateDescription : LiveData<String> =
            Transformations.map(state) { value ->
                value.toString();
            }


}