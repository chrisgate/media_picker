package android.mediapicker.src.main.java.com.pm.mediapicker.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.pm.mediapicker.R;

import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * @author pm
 * @date 2019/6/17
 * @email puming@zdsoft.cn
 *
 */
public class AppBar extends ConstraintLayout {
    public static final int DEFAULT_LEFT_ICON = R.mipmap.button_back;
    public static final int DEFAULT_RIGHT_ICON = R.mipmap.button_logout;
    public static final String DEFAULT_LEFT_TEXT = "返回";

    private ImageView mNavbarBackIcon;
    private TextView mNavbarBackText;
    private LinearLayout mNavbarLeftContainer;
    private TextView mNavbarTitle;
    private TextView mNavbarMenuText;
    private ImageView mNavbarMenuIcon;
    private FrameLayout mNavbarRightContainer;

    public AppBar(Context context) {
        this(context, null);
    }

    public AppBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = LayoutInflater.from(context).inflate(R.layout.appbar, this, true);
        mNavbarBackIcon = (ImageView) view.findViewById(R.id.navbar_back_icon);
        mNavbarBackText = (TextView) view.findViewById(R.id.navbar_back_text);
        mNavbarLeftContainer = (LinearLayout) view.findViewById(R.id.navbar_left_container);
        mNavbarTitle = (TextView) view.findViewById(R.id.navbar_title);
        mNavbarMenuText = (TextView) view.findViewById(R.id.navbar_menu_text);
        mNavbarMenuIcon = (ImageView) view.findViewById(R.id.navbar_menu_icon);
        mNavbarRightContainer = (FrameLayout) view.findViewById(R.id.navbar_right_container);
    }

    public ViewGroup getNavbarLeftContainer() {
        return mNavbarLeftContainer;
    }

    public ViewGroup getNavbarRightContainer() {
        return mNavbarRightContainer;
    }

    public AppBar setNavbarBackIcon(int resId) {
        mNavbarBackIcon.setImageResource(resId);
        return this;
    }

    public AppBar setNavbarBackText(CharSequence text) {
        mNavbarBackText.setText(text);
        return this;
    }

    public AppBar setNavbarTitle(CharSequence text) {
        mNavbarTitle.setText(text);
        return this;
    }

    public AppBar setNavbarMenuText(CharSequence text) {
        mNavbarMenuText.setText(text);
        return this;
    }

    public AppBar setNavbarMenuIcon(int resId) {
        mNavbarMenuIcon.setBackgroundResource(resId);
        return this;
    }

    public AppBar showNavbarMenuIcon(boolean show) {
        if (show) {
            mNavbarMenuIcon.setVisibility(VISIBLE);
            mNavbarMenuText.setVisibility(GONE);
        } else {
            mNavbarMenuIcon.setVisibility(GONE);
            mNavbarMenuText.setVisibility(VISIBLE);
        }
        return this;
    }

    public AppBar showNavbarTitle(boolean show) {
        if (show) {
            mNavbarTitle.setVisibility(VISIBLE);
        } else {
            mNavbarTitle.setVisibility(GONE);
        }
        return this;
    }

    public AppBar showNavbarBackIcon(boolean show) {
        if (show) {
            mNavbarBackIcon.setVisibility(VISIBLE);
        } else {
            mNavbarBackIcon.setVisibility(GONE);
        }
        return this;
    }

    public AppBar showNavbarBackText(boolean show) {
        if (show) {
            mNavbarBackText.setVisibility(VISIBLE);
        } else {
            mNavbarBackText.setVisibility(GONE);
        }
        return this;
    }

    public AppBar showNavbarRightContainer(boolean show) {
        if (show) {
            mNavbarRightContainer.setVisibility(VISIBLE);
        } else {
            mNavbarRightContainer.setVisibility(GONE);
        }
        return this;
    }

    public AppBar showNavbarLeftContainer(boolean show) {
        if (show) {
            mNavbarLeftContainer.setVisibility(VISIBLE);
        } else {
            mNavbarLeftContainer.setVisibility(GONE);
        }
        return this;
    }


}
