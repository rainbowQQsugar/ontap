package com.salesforce.dsa.app.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.abinbev.dsa.R;
import java.lang.reflect.Method;
import butterknife.Bind;
import butterknife.ButterKnife;

public class DownDialog {


    @Bind(R.id.alert_title)
    TextView alertTitle;
    @Bind(R.id.alert_msg)
    TextView alertMsg;
    @Bind(R.id.precent)
    TextView precent;
    @Bind(R.id.progress_bar)
    ProgressBar progressBar;
    @Bind(R.id.progress_layout)
    RelativeLayout progressLayout;
    @Bind(R.id.alert_left_btn)
    Button alertLeftBtn;
    @Bind(R.id.btn_line)
    View btnLine;
    @Bind(R.id.alert_right_btn)
    Button alertRightBtn;
    @Bind(R.id.btn_layout)
    LinearLayout btnLayout;
    @Bind(R.id.dialog_alert_layout)
    LinearLayout dialogAlertLayout;
    private Context context;

    private Display display;

    private int screenWidth = 0;


    private boolean showTitle;

    private boolean showMsg;

    private boolean showNeBtn;

    private boolean showPoBtn;


    private Dialog dialog;

    public Dialog getDialog() {
        return dialog;
    }
    public DownDialog(Context context) {
        this.context = context;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        this.display = windowManager.getDefaultDisplay();
        this.screenWidth = (int) (getScreenRealWidth((Activity) context) / 3 * 2);

    }

    public LinearLayout getBtnLayout() {
        return btnLayout;
    }

    public RelativeLayout getProgressLayout() {
        return progressLayout;
    }

    public DownDialog builder() {

        View view = LayoutInflater.from(context).inflate(R.layout.view_alert_ios_dialog, null);

        ButterKnife.bind(this, view);

        alertTitle.setVisibility(View.GONE);

        alertMsg.setVisibility(View.GONE);

        alertRightBtn.setVisibility(View.GONE);

        alertLeftBtn.setVisibility(View.GONE);


        btnLine.setVisibility(View.GONE);

        dialog = new Dialog(context, R.style.AlertDialogStyle);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {

                if (listener != null) {
                    listener.onDismiss(dialogInterface);
                }
            }
        });
        dialog.setContentView(view);

        dialogAlertLayout.setLayoutParams(new FrameLayout.LayoutParams(screenWidth, FrameLayout.LayoutParams.WRAP_CONTENT));

        return this;
    }

    /**
     * Set the title
     *If the value is null the default setting is null
     * @return
     */
    public DownDialog setTitle(String title) {

        showTitle = true;

        alertTitle.setText(TextUtils.isEmpty(title) ? "":title);

        return this;
    }

    /**
     * Set message body
     *If the value is null the default setting is null
     * @return
     */
    public DownDialog setMessage(String msg) {

        showMsg = true;

        alertMsg.setText(TextUtils.isEmpty(msg) ? "":msg);

        return this;
    }


    /**
     * The set up button
     *If the value is null the default setting is null
     * @param poBtnName
     * @return
     */
    public DownDialog setPoBtn(String poBtnName, final View.OnClickListener listener) {
        showPoBtn = true;
        if (poBtnName == null) {
            //If the button title is empty, it is hidden
            alertRightBtn.setVisibility(View.INVISIBLE);
        } else {
            //  set positive Button title
            alertRightBtn.setText(poBtnName);
        }
        if (listener != null) {
            alertRightBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(v);
                }
            });
        }
        return this;
    }

    /**
     * The set up button
     *If the value is null the default setting is null
     * @param poBtnName
     * @return
     */
    public DownDialog setPoBtn(String poBtnName) {
        showPoBtn = true;
        alertRightBtn.setText(TextUtils.isEmpty(poBtnName) ? "":poBtnName);
        return this;
    }

    /**
     * Set the button to listen for events
     * @param poBtnName
     * @return
     */
    public DownDialog setPoBtn(String poBtnName, final View.OnClickListener listener, final boolean noDismiss) {
        showPoBtn = true;
        if (poBtnName == null) {
            //If the button title is empty, it is hidden
            alertRightBtn.setVisibility(View.INVISIBLE);
        } else {
            //set positive Button title
            alertRightBtn.setText(poBtnName);
        }
        if (listener != null) {
            alertRightBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(v);
                    if (noDismiss) {
                        dialog.dismiss();
                    }
                }
            });
        }
        return this;
    }

    /**
     *Set button color
     * @param color
     * @return
     */
    public DownDialog setPosBtnColor(String color) {
        showPoBtn = true;
        if (alertRightBtn != null) {
            alertRightBtn.setTextColor(Color.parseColor(color));
        }
        return this;
    }

    /**Set postive button BackGroundColor
     * @param color
     * @return
     */
    public DownDialog setPosBtnBackGroundColor(String color) {
        showPoBtn = true;
        if (alertRightBtn != null) {
            alertRightBtn.setBackgroundColor(Color.parseColor(color));
        }
        return this;
    }

    /**
     * set negative button titile
     * @param neBtnName
     * @return
     */
    public DownDialog setNeBtn(String neBtnName, final View.OnClickListener listener) {
        showNeBtn = true;
        if (neBtnName == null) {
            //If the button title is empty, it is hidden
            alertLeftBtn.setVisibility(View.INVISIBLE);
        } else {
            //set negative button titile
            alertLeftBtn.setText(neBtnName);
        }
        //Set listening events
        if (listener != null) {

            alertLeftBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    listener.onClick(v);
                    dialog.dismiss();
                }
            });
        }

        return this;
    }

    /**
     * Set negative the background color of the button
     * @param color
     * @return
     */
    public DownDialog setNeBtnColor(String color) {
        showNeBtn = true;
        if (alertLeftBtn != null) {
            alertLeftBtn.setTextColor(Color.parseColor(color));
        }
        return this;
    }

    /**
     * Set negative the background color of the button
     * @param color
     * @return
     */
    public DownDialog setNeBtnBackGroundColor(String color) {
        showNeBtn = true;
        if (alertLeftBtn != null) {
            alertLeftBtn.setBackgroundColor(Color.parseColor(color));
        }
        return this;
    }

    /**
     * Set whether to cancel the dialog box by clicking outside the screen
     * @param cancel
     * @return
     */
    public DownDialog setCancelable(boolean cancel) {
        dialog.setCancelable(cancel);
        return this;
    }

    /**
     * Set whether to cancel the dialog box by clicking outside the screen
     * @return
     */
    public DownDialog setCancleAble(boolean cancel) {
        dialog.setCancelable(cancel);

        return this;
    }


    /**
     * set show Layout
     */
    public void setLayout() {

        if (!showTitle && !showMsg) {

            alertTitle.setVisibility(View.VISIBLE);
            alertTitle.setText("");
        }

        if (showTitle) {
            alertTitle.setVisibility(View.VISIBLE);
        }

        if (showMsg) {
            alertMsg.setVisibility(View.VISIBLE);
        }

        if (!showPoBtn && !showNeBtn) {

            alertRightBtn.setText(context.getResources().getString(R.string.yes));
            alertRightBtn.setVisibility(View.VISIBLE);
            alertRightBtn.setBackgroundResource(R.drawable.alertdialog_single_selector);

            alertRightBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub

                    if (dialog != null) {

                        dialog.dismiss();
                    }
                }
            });
        }

        if (showPoBtn && !showNeBtn) {
            alertRightBtn.setVisibility(View.VISIBLE);
            alertRightBtn.setBackgroundResource(R.drawable.alertdialog_single_selector);
        }

        if (!showPoBtn && showNeBtn) {

            alertLeftBtn.setVisibility(View.VISIBLE);
            alertLeftBtn.setBackgroundResource(R.drawable.alertdialog_single_selector);
        }

        if (showNeBtn && showPoBtn) {

            alertRightBtn.setVisibility(View.VISIBLE);
            alertRightBtn.setBackgroundResource(R.drawable.alertdialog_right_selector);
            btnLine.setVisibility(View.VISIBLE);
            alertLeftBtn.setVisibility(View.VISIBLE);
            alertLeftBtn.setBackgroundResource(R.drawable.alertdialog_right_selector);
        }
    }

    /**
     * show Dialog
     */
    public void show() {

        setLayout();

        if (dialog != null) {
            dialog.show();
        }
    }

    /**
     * Destroy dialog box
     */
    public void dismiss() {
        if (dialog != null && dialog.isShowing())
            dialog.cancel();
        dialog = null;
    }


    /**
     * According to the button
     * @return
     */
    public boolean isShow() {

        return (dialog != null && dialog.isShowing());
    }

    private IOSDismissListener listener;

    /**
     * Set button Postive visibility
     * @param isClick
     */
    public void setPoBtnClick(boolean isClick) {

        if (alertRightBtn != null) {
            alertRightBtn.setClickable(isClick);
        }
    }

    /**
     * Dialog box type
     */
    public void setType() {
        if (dialog != null) {
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
    }

    /**
     *  Set progress visibility
     * @param visibilityProgressBar
     */
    public void setVisibilityProgressBar(boolean visibilityProgressBar) {

        if (visibilityProgressBar) {
            progressLayout.setVisibility(View.VISIBLE);
        } else {
            progressLayout.setVisibility(View.GONE);
        }
    }


    /**
     * Monitor button
     * @param activity
     */
    public void setOnKeyListener(Activity activity) {

        if (dialog != null) {
            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                        if (dialog != null)
                            dialog.dismiss();
                        if (activity != null)
                            activity.finish();

                    }
                    return false;
                }
            });
        }

    }

    /**
     * Set button and download layout visibility
     * true VISIBLE
     * false GONE
     * @param visibilityBtn
     */
    public void setVisibilityBtn(boolean visibilityBtn) {
        if (visibilityBtn) {
            btnLayout.setVisibility(View.VISIBLE);
        } else {
            btnLayout.setVisibility(View.GONE);
        }
    }

    /**
     * According to progress
     * @param total
     * @param progress
     * @param percent
     */
    public void setProgress(float total, float progress, float percent) {

        if (progressBar != null) {
            progressBar.setProgress((int) percent);
        }

        if (precent != null) {
            int pro = (int) percent;
            precent.setText(pro + "%");
        }

    }

    /**
     *Button event callback
     */
    public interface IOSDismissListener {
        public void onDismiss(DialogInterface listener);
    }

    /**
     * Set callback class
     * @param listener
     */
    public void setOnDismissListener(IOSDismissListener listener) {

        this.listener = listener;
    }

    /**
     * Get screen width
     * @param activity
     * @return
     */
    public float getScreenRealWidth(Activity activity) {
        float dpi = -1f;
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        @SuppressWarnings("rawtypes")
        Class c;
        try {
            c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);

            dpi = dm.widthPixels;
//			dpi = dm.widthPixels + "*" + dm.heightPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dpi;
    }
}
