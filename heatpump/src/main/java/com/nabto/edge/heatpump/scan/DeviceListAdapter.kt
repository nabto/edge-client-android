package com.nabto.edge.heatpump.scan;
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.nabto.edge.heatpump.DeviceListItem
import com.nabto.edge.heatpump.R

class DeviceListAdapter(private val context: Context, private val fragment: Fragment) : BaseAdapter()
{

    var dataSource: List<DeviceListItem> = ArrayList<DeviceListItem>()

    override fun getCount(): Int {
        return dataSource.size;
    }

    override fun getItem(position: Int): DeviceListItem {
        return dataSource[position];
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun getLayoutInflater(): LayoutInflater {
        return context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater;
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rowView = getLayoutInflater().inflate(R.layout.device_item, parent, false)

        val idView = rowView.findViewById(R.id.device_item_id) as TextView
        val descriptionView = rowView.findViewById(R.id.device_item_description) as TextView

        val item = getItem(position);
        idView.text = "" + item.productId + "." + item.deviceId;
        descriptionView.text = item.friendlyName

        rowView.setOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View) {
                val navController = v.findNavController();

                var args = Bundle();
                args.putString("device_id", item.deviceId)
                args.putString("product_id", item.productId);

                navController.navigate(R.id.pair_device_dest, args)
            }
        })

        return rowView
    }

    fun updateData(devices: List<DeviceListItem>) {
        dataSource = devices.toList();
        notifyDataSetChanged()
    }
}
