package com.drdisagree.colorblendr.ui.fragments.onboarding.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.drdisagree.colorblendr.data.common.Constant.WORKING_METHOD
import com.drdisagree.colorblendr.data.enums.WorkMethod
import com.drdisagree.colorblendr.databinding.FragmentOnboardingItem4Binding
import com.drdisagree.colorblendr.ui.activities.MainActivity.Companion.replaceFragment
import com.drdisagree.colorblendr.ui.fragments.PairingFragment
import com.drdisagree.colorblendr.utils.wifiadb.WifiAdbConnectedDevices
import com.drdisagree.colorblendr.utils.wifiadb.WifiAdbShell

class OnboardingItem4Fragment : Fragment() {

    private lateinit var binding: FragmentOnboardingItem4Binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnboardingItem4Binding.inflate(inflater, container, false)

        binding.root.setOnClickListener {
            WORKING_METHOD = WorkMethod.ROOT
            binding.shizuku.isSelected = false
            binding.wirelessAdb.isSelected = false
            disconnectFromWirelessAdb()
        }

        binding.shizuku.setOnClickListener {
            WORKING_METHOD = WorkMethod.SHIZUKU
            binding.root.isSelected = false
            binding.wirelessAdb.isSelected = false
            disconnectFromWirelessAdb()
        }

        binding.wirelessAdb.setOnClickListener {
            WORKING_METHOD = WorkMethod.WIRELESS_ADB
            binding.root.isSelected = false
            binding.shizuku.isSelected = false

            if (!WifiAdbConnectedDevices.isMyDeviceConnected()) {
                binding.wirelessAdb.isSelected = false
                replaceFragment(PairingFragment(), true)
            } else {
                binding.wirelessAdb.isSelected = true
            }
        }

        return binding.getRoot()
    }

    private fun disconnectFromWirelessAdb() {
        if (WifiAdbConnectedDevices.isMyDeviceConnected()) {
            WifiAdbShell.exec("disconnect")
        }
    }

    override fun onResume() {
        super.onResume()

        if (WifiAdbConnectedDevices.isMyDeviceConnected()) {
            binding.root.isSelected = false
            binding.shizuku.isSelected = false
            binding.wirelessAdb.isSelected = true
            WORKING_METHOD = WorkMethod.WIRELESS_ADB
        } else {
            binding.wirelessAdb.isSelected = false
            if (WORKING_METHOD == WorkMethod.WIRELESS_ADB) {
                WORKING_METHOD = WorkMethod.NULL
            }
        }
    }
}