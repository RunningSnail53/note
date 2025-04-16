package edu.hebut.retrofittest.Util;

import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.TextView;

class TypewriterAnimation extends Animation {
    private final CharSequence text;
    private int currentIndex = 0;
    private final TextView textView;

    public TypewriterAnimation(TextView textView, CharSequence text) {
        this.textView = textView;
        this.text = text;
        setDuration(50 * text.length()); // 总时长 = 字符数 × 每个字符延迟
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        int length = (int) (text.length() * interpolatedTime);
        if (length > currentIndex) {
            currentIndex = length;
            textView.setText(text.subSequence(0, currentIndex));
        }
    }
}
