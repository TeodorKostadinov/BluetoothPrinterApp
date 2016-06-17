// Generated code from Butter Knife. Do not modify!
package com.inveitix.ticketprint.ui;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class MainActivity$$ViewBinder<T extends com.inveitix.ticketprint.ui.MainActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131492956, "field 'btnSendDraw'");
    target.btnSendDraw = finder.castView(view, 2131492956, "field 'btnSendDraw'");
    view = finder.findRequiredView(source, 2131492957, "field 'btnDisconnect'");
    target.btnDisconnect = finder.castView(view, 2131492957, "field 'btnDisconnect'");
    view = finder.findRequiredView(source, 2131492955, "field 'btnScan'");
    target.btnScan = finder.castView(view, 2131492955, "field 'btnScan'");
    view = finder.findRequiredView(source, 2131492954, "field 'webView'");
    target.webView = finder.castView(view, 2131492954, "field 'webView'");
  }

  @Override public void unbind(T target) {
    target.btnSendDraw = null;
    target.btnDisconnect = null;
    target.btnScan = null;
    target.webView = null;
  }
}
