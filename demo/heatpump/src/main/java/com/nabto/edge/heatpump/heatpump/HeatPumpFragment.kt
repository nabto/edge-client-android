package com.nabto.edge.heatpump.heatpump

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.INVALID_POSITION
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Spinner
import android.widget.Switch
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.navigation.fragment.navArgs
import com.nabto.edge.heatpump.R
import com.nabto.edge.heatpump.databinding.HeatpumpFragmentBinding
import com.nabto.edge.heatpump.databinding.OverviewFragmentBinding
import com.nabto.edge.heatpump.overview.OverviewViewModel
import javax.inject.Inject

class HeatPumpFragment @Inject constructor(private val factory : ViewModelProvider.Factory): Fragment() {

    val args : HeatPumpFragmentArgs by navArgs<HeatPumpFragmentArgs>()

    lateinit private var binding : HeatpumpFragmentBinding

    lateinit private var viewModel : HeatPumpViewModel

    lateinit private var seekBar : SeekBar


    private var tracking = false
    private var sliderValue : Double = 0.0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProviders.of(this, factory)[HeatPumpViewModel::class.java];
        } ?: throw Exception("Invalid activity")

        viewModel.setDevice(args.productId, args.deviceId)
    }
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ) : View? {
        binding = HeatpumpFragmentBinding.inflate(layoutInflater, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner


        val rootView  = binding.root;

        seekBar = rootView.findViewById<SeekBar>(R.id.target_temperature_slider)

        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                sliderValue = progress/10.0
                viewModel.shownTargetTemperature.value = sliderValue
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                tracking = true;
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                tracking = false;
                viewModel.setTarget(sliderValue);
            }

        })

        viewModel.target.observe(this, object : Observer<Double> {
            override fun onChanged(d : Double) {
                seekBar.progress = (d * 10).toInt()
            }
        })

        val powerButton = rootView.findViewById<Switch>(R.id.heatpump_active);
        powerButton.setOnCheckedChangeListener { powerButton, isChecked -> viewModel.setPower(isChecked) }

        val modeSpinner = rootView.findViewById<Spinner>(R.id.mode_spinner);
        modeSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.setMode(modeSpinner.selectedItem.toString())
            }

        }

        viewModel.mode.observe(viewLifecycleOwner) {
            mode -> modeSpinner.setSelection(selectedMode(mode));
        }

        return rootView;
    }

    fun selectedMode(mode : String): Int {
        for (i in 0..(viewModel.modesArray.size - 1 )) {
            if (viewModel.modesArray[i] == mode) {
                return i;
            }
        }
        return INVALID_POSITION;
    }
}