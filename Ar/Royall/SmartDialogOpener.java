package Neet.Royall.Admin;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.fragment.app.FragmentActivity;

public class SmartDialogOpener {
    public interface OnResult { void onResult(View v); }

    public static void openDialog(Activity act, Object fragment, Runnable onError, OnResult onResult) {
        openDialog(act, fragment, onError, onResult, true);
    }

    public static void openDialog(Activity act, Object fragment, Runnable onError, OnResult onResult, boolean cancelable) {
        if (fragment == null) { if (onError != null) onError.run(); return; }
        try {
            // AndroidX DialogFragment
            if (fragment instanceof androidx.fragment.app.DialogFragment) {
                if (!(act instanceof FragmentActivity)) { if (onError != null) onError.run(); return; }
                androidx.fragment.app.DialogFragment f = (androidx.fragment.app.DialogFragment) fragment;
                FragmentActivity fa = (FragmentActivity) act;
                f.setCancelable(cancelable);
                String tag = f.getClass().getSimpleName().replace("FragmentActivity", "");
                androidx.fragment.app.DialogFragment ex = (androidx.fragment.app.DialogFragment) fa.getSupportFragmentManager().findFragmentByTag(tag);
                if (ex != null && ex.getDialog() != null && ex.getDialog().isShowing()) smartDismiss(ex);
                f.show(fa.getSupportFragmentManager(), tag);
                fa.getSupportFragmentManager().executePendingTransactions();
                applyDialogSettings(f.getDialog(), cancelable, onError, onResult);
                return;
            }
            // Framework DialogFragment
            if (fragment instanceof DialogFragment) {
                DialogFragment f = (DialogFragment) fragment;
                f.setCancelable(cancelable);
                String tag = f.getClass().getSimpleName().replace("FragmentActivity", "");
                DialogFragment ex = (DialogFragment) act.getFragmentManager().findFragmentByTag(tag);
                if (ex != null && ex.getDialog() != null && ex.getDialog().isShowing()) smartDismiss(ex);
                f.show(act.getFragmentManager(), tag);
                act.getFragmentManager().executePendingTransactions();
                applyDialogSettings(f.getDialog(), cancelable, onError, onResult);
                return;
            }
            if (onError != null) onError.run();
        } catch (Exception e) { if (onError != null) onError.run(); }
    }

    private static void applyDialogSettings(final Dialog dialog, boolean cancelable, final Runnable onError, final OnResult onResult) {
        try {
            if (dialog == null || dialog.getWindow() == null) { if (onError != null) onError.run(); return; }
            Window w = dialog.getWindow();
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            w.setGravity(Gravity.BOTTOM);
            w.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            w.setWindowAnimations(android.R.style.Animation_Dialog);
            dialog.setCancelable(cancelable);
            dialog.setCanceledOnTouchOutside(cancelable);
            if (!cancelable) dialog.setOnKeyListener((di, k, ev) -> k == android.view.KeyEvent.KEYCODE_BACK);
            else dialog.setOnKeyListener(null);

            new Handler().post(() -> {
                Dialog d = dialog;
                if (d == null || d.getWindow() == null) { if (onError != null) onError.run(); return; }
                View root = d.getWindow().getDecorView();
                if (root == null) { if (onError != null) onError.run(); return; }
                root.post(() -> { try { if (onResult != null) onResult.onResult(root); } catch (Exception e) { if (onError != null) onError.run(); } });
            });
        } catch (Exception e) { if (onError != null) onError.run(); }
    }

    public static void smartDismiss(Object fragment) {
        try {
            if (fragment == null) return;
            if (fragment instanceof androidx.fragment.app.DialogFragment) {
                androidx.fragment.app.DialogFragment f = (androidx.fragment.app.DialogFragment) fragment;
                if (f.getDialog() != null) f.getDialog().dismiss();
                f.dismissAllowingStateLoss();
                return;
            }
            if (fragment instanceof DialogFragment) {
                DialogFragment f = (DialogFragment) fragment;
                if (f.getDialog() != null) f.getDialog().dismiss();
                f.dismissAllowingStateLoss();
            }
        } catch (Exception ignored) {}
    }

    public static void dismissByClassName(Activity act, String fullFragmentClassName) {
        try {
            String tag = fullFragmentClassName.substring(fullFragmentClassName.lastIndexOf(".") + 1).replace("FragmentActivity", "");
            if (act instanceof FragmentActivity) {
                androidx.fragment.app.DialogFragment xf = (androidx.fragment.app.DialogFragment) ((FragmentActivity) act).getSupportFragmentManager().findFragmentByTag(tag);
                if (xf != null) smartDismiss(xf);
                return;
            }
            DialogFragment f = (DialogFragment) act.getFragmentManager().findFragmentByTag(tag);
            if (f != null) smartDismiss(f);
        } catch (Exception ignored) {}
    }
}