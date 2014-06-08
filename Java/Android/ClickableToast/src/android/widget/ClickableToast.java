/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * 改変部分：
 *  1.クラス名の変更（Toast → ClickableToast）
 *  2.setOnClickListenerメソッドを追加
 *  3.makeTextメソッドのオーバーロードを追加
 *  4.makeTextWithCancelメソッドの追加
 *  5.各内部リソースをResources.getSystem().getIdentifierで取得するよう変更
 *  6.ServiceManagerをServiceLocator経由で取得するよう変更
 *  7.TNのコンストラクタで設定しているWindowManager.LayoutParamsのプロパティを変更
 *    (flags = FLAG_KEEP_SCREEN_ON | FLAG_NOT_FOCUSABLE, type = TYPE_SYSTEM_ALERT)
 *  8.TN#trySendAccessibilityEventメソッド内のAccessibilityManagerの取得方法を変更
 */

package android.widget;

import android.app.ITransientNotification;
import android.app.INotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.WindowManager;

/**
 * A toast is a view containing a quick little message for the user.  The toast class
 * helps you create and show those.
 * {@more}
 *
 * <p>
 * When the view is shown to the user, appears as a floating view over the
 * application.  It will never receive focus.  The user will probably be in the
 * middle of typing something else.  The idea is to be as unobtrusive as
 * possible, while still showing the user the information you want them to see.
 * Two examples are the volume control, and the brief message saying that your
 * settings have been saved.
 * <p>
 * The easiest way to use this class is to call one of the static methods that constructs
 * everything you need and returns a new Toast object.
 *
 * <div class="special reference">
 * <h3>Developer Guides</h3>
 * <p>For information about creating Toast notifications, read the
 * <a href="{@docRoot}guide/topics/ui/notifiers/toasts.html">Toast Notifications</a> developer
 * guide.</p>
 * </div>
 */
public class ClickableToast {
    private static final String TAG = "ClickableToast";
    private static final boolean localLOGV = false;

    private final Context mContext;
    private final TN mTN;
    private int mDuration;
    private View mNextView;
    private static Resources mInternalResources;

    /**
     * Construct an empty Toast object.  You must call {@link #setView} before you
     * can call {@link #show}.
     *
     * @param context  The context to use.  Usually your {@link android.app.Application}
     *                 or {@link android.app.Activity} object.
     */
    public ClickableToast(Context context) {
    	mContext = context;
        if(mInternalResources == null) mInternalResources = Resources.getSystem();
        mTN = new TN(mInternalResources.getIdentifier("Animation.Toast", "style", "android"));

        mTN.mY = context.getResources()
        		.getDimensionPixelSize(mInternalResources.getIdentifier("toast_y_offset", "dimen", "android"));
    }

    /**
     * Show the view for the specified duration.
     */
    public void show() {
        if (mNextView == null) {
            throw new RuntimeException("setView must have been called");
        }

        INotificationManager service = getService();
        String pkg = mContext.getPackageName();
        TN tn = mTN;
        tn.mNextView = mNextView;

        try {
            service.enqueueToast(pkg, tn, mDuration);
        } catch (RemoteException e) {
            // Empty
        }
    }

    /**
     * Close the view if it's showing, or don't show it if it isn't showing yet.
     * You do not normally have to call this.  Normally view will disappear on its own
     * after the appropriate duration.
     */
    public void cancel() {
        mTN.hide();
        // TODO this still needs to cancel the inflight notification if any
    }

    /**
     * Set the view to show.
     * @see #getView
     */
    public void setView(View view) {
        mNextView = view;
    }

    /**
     * Return the view.
     * @see #setView
     */
    public View getView() {
        return mNextView;
    }

    /**
     * 表示されるViewにOnClickListenerをセットします
     * @param listener
     */
    public void setOnClickListener(OnClickListener listener) {
    	if(mNextView != null) mNextView.setOnClickListener(listener);
    }

    /**
     * Set how long to show the view for.
     * @see #LENGTH_SHORT
     * @see #LENGTH_LONG
     */
    public void setDuration(int duration) {
        mDuration = duration;
    }

    /**
     * Return the duration.
     * @see #setDuration
     */
    public int getDuration() {
        return mDuration;
    }

    /**
     * Set the margins of the view.
     *
     * @param horizontalMargin The horizontal margin, in percentage of the
     *        container width, between the container's edges and the
     *        notification
     * @param verticalMargin The vertical margin, in percentage of the
     *        container height, between the container's edges and the
     *        notification
     */
    public void setMargin(float horizontalMargin, float verticalMargin) {
        mTN.mHorizontalMargin = horizontalMargin;
        mTN.mVerticalMargin = verticalMargin;
    }

    /**
     * Return the horizontal margin.
     */
    public float getHorizontalMargin() {
        return mTN.mHorizontalMargin;
    }

    /**
     * Return the vertical margin.
     */
    public float getVerticalMargin() {
        return mTN.mVerticalMargin;
    }

    /**
     * Set the location at which the notification should appear on the screen.
     * @see android.view.Gravity
     * @see #getGravity
     */
    public void setGravity(int gravity, int xOffset, int yOffset) {
        mTN.mGravity = gravity;
        mTN.mX = xOffset;
        mTN.mY = yOffset;
    }

     /**
     * Get the location at which the notification should appear on the screen.
     * @see android.view.Gravity
     * @see #getGravity
     */
    public int getGravity() {
        return mTN.mGravity;
    }

    /**
     * Return the X offset in pixels to apply to the gravity's location.
     */
    public int getXOffset() {
        return mTN.mX;
    }

    /**
     * Return the Y offset in pixels to apply to the gravity's location.
     */
    public int getYOffset() {
        return mTN.mY;
    }

    /**
     * クリックイベントを持ったToastを作成します
     * @param context
     * @param text
     * @param duration
     * @param listener
     * @return
     */
    public static ClickableToast makeText(Context context, CharSequence text, int duration, OnClickListener listener) {
    	final ClickableToast result = new ClickableToast(context);

        LayoutInflater inflate = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(mInternalResources == null) mInternalResources = Resources.getSystem();
        View v = inflate.inflate(
        		mInternalResources.getIdentifier("transient_notification", "layout", "android"), null);

        if(listener != null) v.setOnClickListener(listener);

        TextView tv = (TextView)v.findViewById(
        		mInternalResources.getIdentifier("message", "id", "android"));
        tv.setText(text);

        result.mNextView = v;
        result.mDuration = duration;

        return result;
    }

    /**
     *
     * @param context
     * @param resId
     * @param duration
     * @param listener
     * @return
     * @throws Resources.NotFoundException
     */
    public static ClickableToast makeText(Context context, int resId, int duration, OnClickListener listener)
            throws Resources.NotFoundException {
    	return makeText(context, context.getResources().getText(resId), duration, listener);
	}

    /**
     * クリックすると表示をキャンセルするToastを作成します
     * @param context
     * @param resId
     * @param duration
     * @return
     */
    public static ClickableToast makeTextWithCancel(Context context, int resId, int duration) {
    	return makeTextWithCancel(context, context.getResources().getText(resId), duration);
    }

    /**
     * クリックすると表示をキャンセルするToastを作成します
     * @param context
     * @param text
     * @param duration
     * @return
     */
    public static ClickableToast makeTextWithCancel(Context context, CharSequence text, int duration) {
    	final ClickableToast toast = makeText(context, text, duration, null);
    	toast.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toast.cancel();
			}
		});

    	return toast;
    }

    /**
     * Make a standard toast that just contains a text view.
     *
     * @param context  The context to use.  Usually your {@link android.app.Application}
     *                 or {@link android.app.Activity} object.
     * @param text     The text to show.  Can be formatted text.
     * @param duration How long to display the message.  Either {@link #LENGTH_SHORT} or
     *                 {@link #LENGTH_LONG}
     *
     */
    public static ClickableToast makeText(Context context, CharSequence text, int duration) {
    	return makeText(context, text, duration, null);
    }

    /**
     * Make a standard toast that just contains a text view with the text from a resource.
     *
     * @param context  The context to use.  Usually your {@link android.app.Application}
     *                 or {@link android.app.Activity} object.
     * @param resId    The resource id of the string resource to use.  Can be formatted text.
     * @param duration How long to display the message.  Either {@link #LENGTH_SHORT} or
     *                 {@link #LENGTH_LONG}
     *
     * @throws Resources.NotFoundException if the resource can't be found.
     */
    public static ClickableToast makeText(Context context, int resId, int duration)
                                throws Resources.NotFoundException {
        return makeText(context, context.getResources().getText(resId), duration, null);
    }

    /**
     * Update the text in a Toast that was previously created using one of the makeText() methods.
     * @param resId The new text for the Toast.
     */
    public void setText(int resId) {
        setText(mContext.getText(resId));
    }

    /**
     * Update the text in a Toast that was previously created using one of the makeText() methods.
     * @param s The new text for the Toast.
     */
    public void setText(CharSequence s) {
        if (mNextView == null) {
            throw new RuntimeException("This Toast was not created with Toast.makeText()");
        }
        TextView tv = (TextView) mNextView.findViewById(
        		mInternalResources.getIdentifier("message", "id", "android"));
        if (tv == null) {
            throw new RuntimeException("This Toast was not created with Toast.makeText()");
        }
        tv.setText(s);
    }

    // =======================================================================================
    // All the gunk below is the interaction with the Notification Service, which handles
    // the proper ordering of these system-wide.
    // =======================================================================================

    private static INotificationManager sService;

    static private INotificationManager getService() {
        if (sService != null) {
            return sService;
        }

        sService = (INotificationManager) ServiceLocator.getServiceStub("notification", "android.app.INotificationManager$Stub");
        return sService;
    }

    private static class TN extends ITransientNotification.Stub {
        final Runnable mShow = new Runnable() {
            public void run() {
                handleShow();
            }
        };

        final Runnable mHide = new Runnable() {
            public void run() {
                handleHide();
                // Don't do this in handleHide() because it is also invoked by handleShow()
                mNextView = null;
            }
        };

        private final WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();
        final Handler mHandler = new Handler();

        int mGravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        int mX, mY;
        float mHorizontalMargin;
        float mVerticalMargin;


        View mView;
        View mNextView;

        WindowManager mWM;

        TN(int windowAnimationsId) {
            // XXX This should be changed to use a Dialog, with a Theme.Toast
            // defined that sets up the layout params appropriately.
            final WindowManager.LayoutParams params = mParams;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            			//| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            			| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            params.format = PixelFormat.TRANSLUCENT;
            params.windowAnimations = windowAnimationsId;
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            params.setTitle("Toast");

        }

        /**
         * schedule handleShow into the right thread
         */
        public void show() {
            if (localLOGV) Log.v(TAG, "SHOW: " + this);
            mHandler.post(mShow);
        }

        /**
         * schedule handleHide into the right thread
         */
        public void hide() {
            if (localLOGV) Log.v(TAG, "HIDE: " + this);
            mHandler.post(mHide);
        }

        public void handleShow() {
            if (localLOGV) Log.v(TAG, "HANDLE SHOW: " + this + " mView=" + mView
                    + " mNextView=" + mNextView);
            if (mView != mNextView) {
                // remove the old view if necessary
                handleHide();
                mView = mNextView;
                Context context = mView.getContext().getApplicationContext();
                if (context == null) {
                	context = mView.getContext();
                }
                mWM = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
                final int gravity = mGravity;
                mParams.gravity = gravity;
                if ((gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.FILL_HORIZONTAL) {
                    mParams.horizontalWeight = 1.0f;
                }
                if ((gravity & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.FILL_VERTICAL) {
                    mParams.verticalWeight = 1.0f;
                }
                mParams.x = mX;
                mParams.y = mY;
                mParams.verticalMargin = mVerticalMargin;
                mParams.horizontalMargin = mHorizontalMargin;
                if (mView.getParent() != null) {
                    if (localLOGV) Log.v(TAG, "REMOVE! " + mView + " in " + this);
                    mWM.removeView(mView);
                }
                if (localLOGV) Log.v(TAG, "ADD! " + mView + " in " + this);
                mWM.addView(mView, mParams);
                trySendAccessibilityEvent();
            }
        }

        private void trySendAccessibilityEvent() {
        	AccessibilityManager accessibilityManager =
        	        (AccessibilityManager) mView.getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
            if (!accessibilityManager.isEnabled()) {
                return;
            }
            // treat toasts as notifications since they are used to
            // announce a transient piece of information to the user
            AccessibilityEvent event = AccessibilityEvent.obtain(
                    AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED);
            event.setClassName(getClass().getName());
            event.setPackageName(mView.getContext().getPackageName());
            mView.dispatchPopulateAccessibilityEvent(event);
            accessibilityManager.sendAccessibilityEvent(event);
        }

        public void handleHide() {
            if (localLOGV) Log.v(TAG, "HANDLE HIDE: " + this + " mView=" + mView);
            if (mView != null) {
                // note: checking parent() just to make sure the view has
                // been added...  i have seen cases where we get here when
                // the view isn't yet added, so let's try not to crash.
                if (mView.getParent() != null) {
                    if (localLOGV) Log.v(TAG, "REMOVE! " + mView + " in " + this);
                    mWM.removeView(mView);
                }

                mView = null;
            }
        }
    }
}
