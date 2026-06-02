package com.alhaq.deenshield.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alhaq.deenshield.R
import com.alhaq.deenshield.permissions.PermissionItemView
import com.alhaq.deenshield.permissions.PermissionsManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PermissionsBottomSheetDialog : BottomSheetDialogFragment() {

    private lateinit var permissionsManager: PermissionsManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_permissions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        permissionsManager = PermissionsManager(requireContext())

        updatePermissionStates(view)
        setupClickListeners(view)
    }

    override fun onResume() {
        super.onResume()
        view?.let { updatePermissionStates(it) }
    }

    private fun updatePermissionStates(view: View) {
        view.findViewById<PermissionItemView>(R.id.accessibility_permission)?.isGranted =
            permissionsManager.isAccessibilityServiceEnabled()
            
        view.findViewById<PermissionItemView>(R.id.device_admin_permission)?.isGranted =
            permissionsManager.isDeviceAdminEnabled()
            
        view.findViewById<PermissionItemView>(R.id.overlay_permission)?.isGranted =
            permissionsManager.isDrawOverOtherAppsEnabled()
            
        view.findViewById<PermissionItemView>(R.id.usage_stats_permission)?.isGranted =
            permissionsManager.isUsageStatsPermissionGranted()
            
        view.findViewById<PermissionItemView>(R.id.notification_permission)?.isGranted =
            permissionsManager.areNotificationsEnabled()
    }

    private fun setupClickListeners(view: View) {
        view.findViewById<PermissionItemView>(R.id.accessibility_permission)?.setOnClickListener {
            if (!permissionsManager.isAccessibilityServiceEnabled()) {
                startActivity(permissionsManager.getAccessibilityServiceIntent())
            }
        }
        view.findViewById<PermissionItemView>(R.id.device_admin_permission)?.setOnClickListener {
            if (!permissionsManager.isDeviceAdminEnabled()) {
                startActivity(permissionsManager.getDeviceAdminIntent())
            }
        }
        view.findViewById<PermissionItemView>(R.id.overlay_permission)?.setOnClickListener {
            if (!permissionsManager.isDrawOverOtherAppsEnabled()) {
                startActivity(permissionsManager.getDrawOverOtherAppsIntent())
            }
        }
        view.findViewById<PermissionItemView>(R.id.usage_stats_permission)?.setOnClickListener {
            if (!permissionsManager.isUsageStatsPermissionGranted()) {
                startActivity(permissionsManager.getUsageStatsIntent())
            }
        }
        view.findViewById<PermissionItemView>(R.id.notification_permission)?.setOnClickListener {
            if (!permissionsManager.areNotificationsEnabled()) {
                startActivity(permissionsManager.getNotificationPermissionIntent())
            }
        }
    }
}
