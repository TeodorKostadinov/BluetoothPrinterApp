// Generated code from Butter Knife. Do not modify!
package com.inveitix.ticketprint.ui;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class MainActivity$$ViewBinder<T extends com.inveitix.ticketprint.ui.MainActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131492957, "field 'btnSendDraw'");
    target.btnSendDraw = finder.castView(view, 2131492957, "field 'btnSendDraw'");
    view = finder.findRequiredView(source, 2131492956, "field 'btnOpen'");
    target.btnOpen = finder.castView(view, 2131492956, "field 'btnOpen'");
    view = finder.findRequiredView(source, 2131492958, "field 'btnDisconnect'");
    target.btnDisconnect = finder.castView(view, 2131492958, "field 'btnDisconnect'");
    view = finder.findRequiredView(source, 2131492954, "field 'checkBox'");
    target.checkBox = finder.castView(view, 2131492954, "field 'checkBox'");
    view = finder.findRequiredView(source, 2131492953, "field 'edtContext'");
    target.edtContext = finder.castView(view, 2131492953, "field 'edtContext'");
    view = finder.findRequiredView(source, 2131492955, "field 'webView'");
    target.webView = finder.castView(view, 2131492955, "field 'webView'");
    view = finder.findRequiredView(source, 2131492947, "field 'btnRefresh' and method 'refresh'");
    target.btnRefresh = finder.castView(view, 2131492947, "field 'btnRefresh'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.refresh();
        }
      });
  }

  @Override public void unbind(T target) {
    target.btnSendDraw = null;
    target.btnOpen = null;
    target.btnDisconnect = null;
    target.checkBox = null;
    target.edtContext = null;
    target.webView = null;
    target.btnRefresh = null;
  }
}
