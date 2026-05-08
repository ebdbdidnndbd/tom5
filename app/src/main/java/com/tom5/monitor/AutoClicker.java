package com.tom5.monitor;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;

public class AutoClicker extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // البحث عن أزرار النظام باللغتين العربية والإنجليزية
        findAndClick(getRootInActiveWindow(), "البدء الآن");
        findAndClick(getRootInActiveWindow(), "Start now");
        findAndClick(getRootInActiveWindow(), "بدء الآن");
        findAndClick(getRootInActiveWindow(), "START NOW");
    }

    private void findAndClick(AccessibilityNodeInfo node, String text) {
        if (node == null) return;
        List<AccessibilityNodeInfo> list = node.findAccessibilityNodeInfosByText(text);
        for (AccessibilityNodeInfo n : list) {
            n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            n.recycle();
        }
    }

    @Override public void onInterrupt() {}
}
