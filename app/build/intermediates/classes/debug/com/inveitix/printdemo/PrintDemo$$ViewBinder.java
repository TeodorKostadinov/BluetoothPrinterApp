// Generated code from Butter Knife. Do not modify!
package com.inveitix.printdemo;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class PrintDemo$$ViewBinder<T extends com.inveitix.printdemo.PrintDemo> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131427415, "field 'btnSearch'");
    target.btnSearch = finder.castView(view, 2131427415, "field 'btnSearch'");
    view = finder.findRequiredView(source, 2131427420, "field 'btnSendDraw'");
    target.btnSendDraw = finder.castView(view, 2131427420, "field 'btnSendDraw'");
    view = finder.findRequiredView(source, 2131427419, "field 'btnSend'");
    target.btnSend = finder.castView(view, 2131427419, "field 'btnSend'");
    view = finder.findRequiredView(source, 2131427421, "field 'btnClose'");
    target.btnClose = finder.castView(view, 2131427421, "field 'btnClose'");
    view = finder.findRequiredView(source, 2131427417, "field 'checkBox'");
    target.checkBox = finder.castView(view, 2131427417, "field 'checkBox'");
    view = finder.findRequiredView(source, 2131427416, "field 'edtContext'");
    target.edtContext = finder.castView(view, 2131427416, "field 'edtContext'");
    view = finder.findRequiredView(source, 2131427418, "field 'webView'");
    target.webView = finder.castView(view, 2131427418, "field 'webView'");
  }

  @Override public void unbind(T target) {
    target.btnSearch = null;
    target.btnSendDraw = null;
    target.btnSend = null;
    target.btnClose = null;
    target.checkBox = null;
    target.edtContext = null;
    target.webView = null;
  }
}
