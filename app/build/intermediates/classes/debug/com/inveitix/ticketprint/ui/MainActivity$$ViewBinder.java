// Generated code from Butter Knife. Do not modify!
package com.inveitix.ticketprint.ui;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class MainActivity$$ViewBinder<T extends com.inveitix.ticketprint.ui.MainActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131427419, "field 'btnSendDraw'");
    target.btnSendDraw = finder.castView(view, 2131427419, "field 'btnSendDraw'");
    view = finder.findRequiredView(source, 2131427418, "field 'btnOpen'");
    target.btnOpen = finder.castView(view, 2131427418, "field 'btnOpen'");
    view = finder.findRequiredView(source, 2131427420, "field 'btnDisconnect'");
    target.btnDisconnect = finder.castView(view, 2131427420, "field 'btnDisconnect'");
    view = finder.findRequiredView(source, 2131427416, "field 'checkBox'");
    target.checkBox = finder.castView(view, 2131427416, "field 'checkBox'");
    view = finder.findRequiredView(source, 2131427415, "field 'edtContext'");
    target.edtContext = finder.castView(view, 2131427415, "field 'edtContext'");
    view = finder.findRequiredView(source, 2131427417, "field 'webView'");
    target.webView = finder.castView(view, 2131427417, "field 'webView'");
  }

  @Override public void unbind(T target) {
    target.btnSendDraw = null;
    target.btnOpen = null;
    target.btnDisconnect = null;
    target.checkBox = null;
    target.edtContext = null;
    target.webView = null;
  }
}
