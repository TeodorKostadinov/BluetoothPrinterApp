// Generated code from Butter Knife. Do not modify!
package com.inveitix.ticketprint.ui;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class MainActivity$$ViewBinder<T extends com.inveitix.ticketprint.ui.MainActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131492990, "field 'btnSendDraw'");
    target.btnSendDraw = finder.castView(view, 2131492990, "field 'btnSendDraw'");
    view = finder.findRequiredView(source, 2131492991, "field 'btnDisconnect'");
    target.btnDisconnect = finder.castView(view, 2131492991, "field 'btnDisconnect'");
    view = finder.findRequiredView(source, 2131492993, "field 'btnScan'");
    target.btnScan = finder.castView(view, 2131492993, "field 'btnScan'");
    view = finder.findRequiredView(source, 2131492989, "field 'webView'");
    target.webView = finder.castView(view, 2131492989, "field 'webView'");
    view = finder.findRequiredView(source, 2131492992, "field 'bottomSheet'");
    target.bottomSheet = view;
  }

  @Override public void unbind(T target) {
    target.btnSendDraw = null;
    target.btnDisconnect = null;
    target.btnScan = null;
    target.webView = null;
    target.bottomSheet = null;
  }
}
