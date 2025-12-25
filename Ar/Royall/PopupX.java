package Ar.Royall;

import android.app.Activity;
import android.view.View;
import android.widget.PopupMenu;

public class PopupX {

    /* =======================
       INNER DATA CLASS
       ======================= */
    public static class MItem {
        public String title;
        public int icon;
        public Runnable click;
        public MItem[] sub;

        public MItem(String t, int i, Runnable r) {
            title = t;
            icon = i;
            click = r;
        }

        public MItem(String t, MItem[] s) {
            title = t;
            sub = s;
        }
    }

    /* =======================
       SINGLE ACTIVE CONTROL.
       ======================= */
    private static PopupMenu active;
    private static int token = 0; // 👈 generation guard

    /* =======================
       PUBLIC API
       ======================= */
    public static void show(
            View v,
            Activity cntx,
            MItem[] items,
            Runnable onDismiss
    ) {

        // new request → invalidate previous ones
        final int myToken = ++token;

        // dismiss currently visible popup
        if (active != null) {
            try { active.dismiss(); } catch (Exception ignored) {}
            active = null;
        }

        v.post(() -> {

            // ❌ outdated request → do nothing
            if (myToken != token) return;

            PopupMenu p = new PopupMenu(cntx, v);
            active = p;

            addItems(p.getMenu(), items);

            // optional: force icons (ROM dependent)
            try {
                java.lang.reflect.Method m =
                    p.getMenu().getClass()
                        .getDeclaredMethod("setOptionalIconsVisible", boolean.class);
                m.setAccessible(true);
                m.invoke(p.getMenu(), true);
            } catch (Exception ignored) {}

            p.setOnDismissListener(new PopupMenu.OnDismissListener() {
                @Override
                public void onDismiss(PopupMenu menu) {
                    if (active == menu) active = null;
                    if (onDismiss != null) onDismiss.run();
                }
            });

            p.show();
        });
    }

    /* =======================
       INTERNAL RECURSION
       ======================= */
    private static void addItems(android.view.Menu menu, MItem[] items) {

        for (MItem it : items) {

            if (it.sub == null) {

                menu.add(it.title)
                    .setIcon(it.icon)
                    .setOnMenuItemClickListener(i -> {
                        if (it.click != null) it.click.run();
                        return true;
                    });

            } else {
// test.
                android.view.SubMenu sm = menu.addSubMenu(it.title);
                addItems(sm, it.sub);
            }
        }
    }
}