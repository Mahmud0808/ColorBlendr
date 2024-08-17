package com.drdisagree.colorblendr.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.common.Const
import com.drdisagree.colorblendr.common.Const.FIRST_RUN
import com.drdisagree.colorblendr.common.Const.WALLPAPER_COLOR_LIST
import com.drdisagree.colorblendr.common.Const.saveWorkingMethod
import com.drdisagree.colorblendr.config.RPrefs.putBoolean
import com.drdisagree.colorblendr.config.RPrefs.putString
import com.drdisagree.colorblendr.databinding.FragmentOnboardingBinding
import com.drdisagree.colorblendr.extension.MethodInterface
import com.drdisagree.colorblendr.provider.RootConnectionProvider.Companion.builder
import com.drdisagree.colorblendr.provider.ShizukuConnectionProvider
import com.drdisagree.colorblendr.service.ShizukuConnection
import com.drdisagree.colorblendr.ui.activities.MainActivity
import com.drdisagree.colorblendr.ui.adapters.OnboardingAdapter
import com.drdisagree.colorblendr.utils.AppUtil.hasStoragePermission
import com.drdisagree.colorblendr.utils.AppUtil.permissionsGranted
import com.drdisagree.colorblendr.utils.FabricatedUtil.getAndSaveSelectedFabricatedApps
import com.drdisagree.colorblendr.utils.ShizukuUtil.bindUserService
import com.drdisagree.colorblendr.utils.ShizukuUtil.getUserServiceArgs
import com.drdisagree.colorblendr.utils.ShizukuUtil.isShizukuAvailable
import com.drdisagree.colorblendr.utils.ShizukuUtil.requestShizukuPermission
import com.drdisagree.colorblendr.utils.WallpaperColorUtil.getAndSaveWallpaperColors
import com.drdisagree.colorblendr.utils.WallpaperColorUtil.getWallpaperColors

class OnboardingFragment : Fragment() {

    private lateinit var binding: FragmentOnboardingBinding
    private lateinit var adapter: OnboardingAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnboardingBinding.inflate(inflater, container, false)

        adapter = OnboardingAdapter(
            childFragmentManager,
            requireActivity().lifecycle
        )

        adapter.addFragment(OnboardingItem1Fragment())
        adapter.addFragment(OnboardingItem2Fragment())
        adapter.addFragment(OnboardingItem3Fragment())

        binding.viewPager.adapter = adapter

        binding.viewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                animateBackButton(position)
                changeContinueButtonText(position)
            }
        })

        binding.btnNext.setOnClickListener {
            if (binding.viewPager.currentItem == adapter.itemCount - 1) {
                if (!permissionsGranted(requireContext())) {
                    Toast.makeText(
                        requireContext(),
                        R.string.grant_all_permissions,
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setOnClickListener
                }

                when (Const.WORKING_METHOD) {
                    Const.WorkMethod.NULL -> {
                        Toast.makeText(requireContext(), R.string.select_method, Toast.LENGTH_SHORT)
                            .show()
                    }

                    Const.WorkMethod.ROOT -> {
                        checkRootConnection()
                    }

                    Const.WorkMethod.SHIZUKU -> {
                        checkShizukuConnection()
                    }
                }

                return@setOnClickListener
            }

            binding.viewPager.currentItem += 1
        }

        binding.btnPrev.setOnClickListener {
            binding.viewPager.setCurrentItem(
                binding.viewPager.currentItem - 1, true
            )
        }

        registerOnBackInvokedCallback()

        return binding.root
    }

    private fun checkRootConnection() {
        builder(requireContext())
            .runOnSuccess(object : MethodInterface() {
                override fun run() {
                    goToHomeFragment()
                }
            }).run()
    }

    private fun checkShizukuConnection() {
        if (isShizukuAvailable) {
            requestShizukuPermission(requireActivity()) { granted: Boolean ->
                if (granted) {
                    bindUserService(
                        getUserServiceArgs(ShizukuConnection::class.java),
                        ShizukuConnectionProvider.serviceConnection
                    )
                    goToHomeFragment()
                } else {
                    Toast.makeText(
                        appContext,
                        R.string.shizuku_service_not_found,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } else {
            Toast.makeText(
                appContext,
                R.string.shizuku_service_not_found,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun goToHomeFragment() {
        saveWorkingMethod(Const.WORKING_METHOD)
        getAndSaveWallpaperColors(requireContext())
        getAndSaveSelectedFabricatedApps(requireContext())
        putBoolean(FIRST_RUN, false)
        MainActivity.Companion.replaceFragment(HomeFragment(), true)
    }

    private fun animateBackButton(position: Int) {
        val duration = 300

        if (position == 0 && binding.btnPrev.visibility == View.VISIBLE) {
            val fadeOut = getFadeOutAnimation(duration)
            binding.btnPrev.startAnimation(fadeOut)
        } else if (position != 0 && binding.btnPrev.visibility != View.VISIBLE) {
            val fadeIn = getFadeInAnimation(duration)
            binding.btnPrev.startAnimation(fadeIn)
        }
    }

    @Suppress("SameParameterValue")
    private fun getFadeOutAnimation(duration: Int): AlphaAnimation {
        val fadeOut = AlphaAnimation(1.0f, 0.0f)

        fadeOut.duration = duration.toLong()

        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
            }

            override fun onAnimationEnd(animation: Animation) {
                binding.btnPrev.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation) {
            }
        })

        return fadeOut
    }

    @Suppress("SameParameterValue")
    private fun getFadeInAnimation(duration: Int): AlphaAnimation {
        val fadeIn = AlphaAnimation(0.0f, 1.0f)

        fadeIn.duration = duration.toLong()

        fadeIn.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                binding.btnPrev.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animation) {
            }

            override fun onAnimationRepeat(animation: Animation) {
            }
        })

        return fadeIn
    }

    private fun changeContinueButtonText(position: Int) {
        if (position == adapter.itemCount - 1) {
            binding.btnNext.setText(R.string.start)

            Thread {
                try {
                    if (hasStoragePermission()) {
                        val wallpaperColors =
                            getWallpaperColors(requireContext())
                        putString(
                            WALLPAPER_COLOR_LIST,
                            Const.GSON.toJson(wallpaperColors)
                        )
                    }
                } catch (ignored: Exception) {
                }
            }.start()
        } else {
            binding.btnNext.setText(R.string.btn_continue)
        }
    }

    private fun registerOnBackInvokedCallback() {
        requireActivity().onBackPressedDispatcher.addCallback(
            requireActivity(),
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBackPressed()
                }
            })
    }

    private fun onBackPressed() {
        if (binding.viewPager.currentItem == 0) {
            requireActivity().finish()
        } else {
            binding.viewPager.setCurrentItem(binding.viewPager.currentItem - 1, true)
        }
    }
}