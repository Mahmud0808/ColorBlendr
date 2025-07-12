package com.drdisagree.colorblendr.ui.fragments

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.databinding.FragmentPairingBinding
import com.drdisagree.colorblendr.ui.activities.MainActivity
import com.drdisagree.colorblendr.utils.app.AppUtil
import com.drdisagree.colorblendr.utils.app.MiscUtil.setToolbarTitle
import com.drdisagree.colorblendr.utils.app.SystemUtil
import com.drdisagree.colorblendr.utils.wifiadb.WifiAdbConnectedDevices

class PairingFragment : Fragment() {

    private lateinit var binding: FragmentPairingBinding
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPairingBinding.inflate(inflater, container, false)

        setToolbarTitle(requireContext(), R.string.pairing, true, binding.header.toolbar)

        updateNotificationPermissionState()
        updateWifiState(SystemUtil.isConnectedToWifi(requireContext()))

        binding.notificationButton.setOnClickListener {
            AppUtil.openAppNotificationSettings(requireContext())
        }

        binding.wifiPromptButton.setOnClickListener {
            SystemUtil.requestEnableWifi(requireContext())
        }

        binding.developerOptionsButton.setOnClickListener {
            SystemUtil.openDeveloperOptions(requireContext())
        }

        (requireActivity() as MainActivity).pairThisDevice()

        return binding.root
    }

    private fun updateNotificationPermissionState() {
        if (AppUtil.hasNotificationPermission(requireContext())) {
            binding.notificationHint.visibility = View.VISIBLE
            binding.notificationAccess.visibility = View.GONE
        } else {
            binding.notificationHint.visibility = View.GONE
            binding.notificationAccess.visibility = View.VISIBLE
        }
    }

    private fun updateWifiState(isConnected: Boolean) {
        binding.wifiConnectionRequired.visibility = if (isConnected) View.GONE else View.VISIBLE
    }

    override fun onStart() {
        super.onStart()

        connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                val isConnected = connectivityManager.getNetworkCapabilities(network)
                    ?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

                requireActivity().runOnUiThread { updateWifiState(isConnected) }
            }

            override fun onLost(network: Network) {
                requireActivity().runOnUiThread {
                    updateWifiState(false)
                }
            }
        }

        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    override fun onResume() {
        super.onResume()

        updateNotificationPermissionState()

        if (WifiAdbConnectedDevices.isMyDeviceConnected()) {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    override fun onStop() {
        super.onStop()

        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}