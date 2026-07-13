package com.alhaq.amnshield.ui.fragments.features

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.alhaq.amnshield.R
import com.alhaq.amnshield.databinding.FragmentSocialMediaBlockerConfigBinding
import com.alhaq.amnshield.databinding.ItemSocialBlockCardBinding
import com.alhaq.amnshield.services.AmnShieldAccessibilityService
import com.alhaq.amnshield.ui.activity.SelectAppsActivity
import com.alhaq.amnshield.premium.PremiumManager
import java.util.ArrayList
import java.util.Locale

/**
 * Configuration screen for managing blocked social media apps and websites.
 */
class SocialMediaBlockerConfigFragment : BaseFeatureFragment() {

    private var _binding: FragmentSocialMediaBlockerConfigBinding? = null
    private val binding get() = _binding!!

    private val selectAppsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val selectedApps = result.data?.getStringArrayListExtra("SELECTED_APPS")
            selectedApps?.let {
                savedPreferencesLoader.saveBlockedSocialApps(it.toSet())
                sendRefreshRequest(AmnShieldAccessibilityService.INTENT_ACTION_REFRESH_APP_BLOCKER)
                refreshAppsList()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSocialMediaBlockerConfigBinding.inflate(inflater, container, false)

        val premiumManager = PremiumManager.getInstance(requireContext().applicationContext)
        if (!premiumManager.isPremium()) {
            binding.configContainer.visibility = View.GONE
            binding.statusCard.visibility = View.VISIBLE
            binding.statusMessage.text = getString(R.string.premium_required_message)
            binding.btnStatusAction.text = getString(R.string.premium_view_plans)
            binding.btnStatusAction.setOnClickListener {
                val intent = Intent(requireContext(), com.alhaq.amnshield.ui.activity.FragmentActivity::class.java)
                intent.putExtra("feature_type", "premium_features")
                startActivity(intent)
            }
            return binding.root
        }

        if (!isAccessibilityServiceEnabled(AmnShieldAccessibilityService::class.java)) {
            binding.configContainer.visibility = View.GONE
            binding.statusCard.visibility = View.VISIBLE
            binding.statusMessage.text = "Please enable the main AmnShield Accessibility Service"
            binding.btnStatusAction.text = "Enable Service"
            binding.btnStatusAction.setOnClickListener {
                showAccessibilityInfoDialog("AmnShield Accessibility Service", AmnShieldAccessibilityService::class.java)
            }
            return binding.root
        }

        binding.statusCard.visibility = View.GONE
        binding.configContainer.visibility = View.VISIBLE

        setupUI()

        return binding.root
    }

    private fun setupUI() {
        // Back arrow
        binding.btnBackArrow.setOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }

        // Enable switch
        val isEnabled = savedPreferencesLoader.isSocialMediaBlockerEnabled()
        binding.switchSocialMediaBlocker.isChecked = isEnabled
        binding.switchSocialMediaBlocker.setOnCheckedChangeListener { _, isChecked ->
            savedPreferencesLoader.setSocialMediaBlockerEnabled(isChecked)
            sendRefreshRequest(AmnShieldAccessibilityService.INTENT_ACTION_REFRESH_APP_BLOCKER)
        }

        // Manage apps button
        binding.btnManageApps.setOnClickListener {
            val intent = Intent(requireContext(), SelectAppsActivity::class.java)
            intent.putStringArrayListExtra(
                "PRE_SELECTED_APPS",
                ArrayList(savedPreferencesLoader.loadBlockedSocialApps())
            )
            selectAppsLauncher.launch(intent, activityOptions)
        }

        // Add website button
        binding.btnAddWebsite.setOnClickListener {
            val rawUrl = binding.inputWebsite.text?.toString()?.trim()
            if (rawUrl.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Please enter a domain or URL", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var cleanUrl = rawUrl.lowercase(Locale.ROOT)
            // Strip protocols and www
            cleanUrl = cleanUrl.replace("https://", "")
                .replace("http://", "")
                .replace("www.", "")

            val currentWebsites = savedPreferencesLoader.loadBlockedSocialWebsites().toMutableSet()
            if (currentWebsites.contains(cleanUrl)) {
                Toast.makeText(requireContext(), "Website already blocked", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            currentWebsites.add(cleanUrl)
            savedPreferencesLoader.saveBlockedSocialWebsites(currentWebsites)
            sendRefreshRequest(AmnShieldAccessibilityService.INTENT_ACTION_REFRESH_APP_BLOCKER)
            binding.inputWebsite.text?.clear()
            refreshWebsitesList()
        }

        refreshAppsList()
        refreshWebsitesList()
    }

    private fun refreshAppsList() {
        binding.layoutBlockedAppsList.removeAllViews()
        val blockedApps = savedPreferencesLoader.loadBlockedSocialApps()
        val pm = requireContext().packageManager

        for (packageName in blockedApps) {
            val itemBinding = ItemSocialBlockCardBinding.inflate(layoutInflater, binding.layoutBlockedAppsList, false)
            
            // Get user-friendly app label
            val appLabel = try {
                val appInfo = pm.getApplicationInfo(packageName, 0)
                pm.getApplicationLabel(appInfo).toString()
            } catch (e: PackageManager.NameNotFoundException) {
                packageName
            }

            itemBinding.txtTitle.text = appLabel
            itemBinding.btnDelete.setOnClickListener {
                val updatedApps = savedPreferencesLoader.loadBlockedSocialApps().toMutableSet()
                updatedApps.remove(packageName)
                savedPreferencesLoader.saveBlockedSocialApps(updatedApps)
                sendRefreshRequest(AmnShieldAccessibilityService.INTENT_ACTION_REFRESH_APP_BLOCKER)
                refreshAppsList()
            }

            binding.layoutBlockedAppsList.addView(itemBinding.root)
        }
    }

    private fun refreshWebsitesList() {
        binding.layoutBlockedWebsitesList.removeAllViews()
        val blockedWebsites = savedPreferencesLoader.loadBlockedSocialWebsites()

        for (website in blockedWebsites) {
            val itemBinding = ItemSocialBlockCardBinding.inflate(layoutInflater, binding.layoutBlockedWebsitesList, false)
            itemBinding.txtTitle.text = website
            itemBinding.btnDelete.setOnClickListener {
                val updatedWebsites = savedPreferencesLoader.loadBlockedSocialWebsites().toMutableSet()
                updatedWebsites.remove(website)
                savedPreferencesLoader.saveBlockedSocialWebsites(updatedWebsites)
                sendRefreshRequest(AmnShieldAccessibilityService.INTENT_ACTION_REFRESH_APP_BLOCKER)
                refreshWebsitesList()
            }

            binding.layoutBlockedWebsitesList.addView(itemBinding.root)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val FRAGMENT_ID = "social_media_blocker"
    }
}
