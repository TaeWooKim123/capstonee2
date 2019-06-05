package com.example.capstonee;

import android.animation.Animator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.AttrRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.capstonee.Capcha.BitmapLoaderTask;
import com.example.capstonee.Capcha.CaptchaStrategy;
import com.example.capstonee.Capcha.PictureVertifyView;
import com.example.capstonee.Capcha.TextSeekbar;
import com.example.capstonee.Capcha.Utils;


public class Captcha extends LinearLayout {

    private PictureVertifyView vertifyView;         //퍼즐조각
    private TextSeekbar seekbar;                    //슬라이드 바
    private View accessSuccess, accessFailed;       //성공,실패 표시보기 확인
    private TextView accessText, accessFailedText;  //성공, 실패 텍스트
    private ImageView refreshView;                  //새로 고침 버튼

    private int drawableId = -1;          //이미지 리소스 id
    private int progressDrawableId;  //슬라이더 배경 id
    private int thumbDrawableId;     //슬라이더 id
    private int mMode;               //모드(슬라이더 / 드래그 앤 드랍)
    private int maxFailedCount;      //최대 실패 횟수
    private int failCount;           //실패 횟수
    private int blockSize;           //퍼블 블록 크기

    //슬라이더 logic 처리
    private boolean isResponse;
    private boolean isDown;

    private CaptchaListener mListener;

    private BitmapLoaderTask mTask;
    /**
     * 슬라이더 모드 사용
     */
    public static final int MODE_BAR = 1;
    /**
     * 드래그앤 드랍 모드(터치 모드) 사용
     */
    public static final int MODE_NONBAR = 2;

    @IntDef(value = {MODE_BAR, MODE_NONBAR})
    public @interface Mode {
    }


    public interface CaptchaListener {
        /**
         * Called when captcha access.
         *
         * @param time cost of access time
         * @return text to show,show default when return null
         */
        String onAccess(long time);

        /**
         * Called when captcha failed.
         *
         * @param failCount fail count
         * @return text to show,show default when return null
         */
        String onFailed(int failCount);

        /**
         * Called when captcha failed
         *
         * @return text to show,show default when return null
         */
        String onMaxFailed();

    }


    public Captcha(@NonNull Context context) {
        super(context);
    }

    public Captcha(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Captcha(@NonNull final Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Captcha);
        drawableId = typedArray.getResourceId(R.styleable.Captcha_src, R.drawable.cat);
        progressDrawableId = typedArray.getResourceId(R.styleable.Captcha_progressDrawable, R.drawable.po_seekbar);
        thumbDrawableId = typedArray.getResourceId(R.styleable.Captcha_thumbDrawable, R.drawable.thumb);
        mMode = typedArray.getInteger(R.styleable.Captcha_mode, MODE_BAR);
        maxFailedCount = typedArray.getInteger(R.styleable.Captcha_max_fail_count, 3);
        blockSize = typedArray.getDimensionPixelSize(R.styleable.Captcha_blockSize, Utils.dp2px(getContext(), 50));
        typedArray.recycle();
        init();
    }


    private void init() {
        View parentView = LayoutInflater.from(getContext()).inflate(R.layout.container, this, true);
        vertifyView = (PictureVertifyView) parentView.findViewById(R.id.vertifyView);
        seekbar = (TextSeekbar) parentView.findViewById(R.id.seekbar);
        accessSuccess = parentView.findViewById(R.id.accessRight);
        accessFailed = parentView.findViewById(R.id.accessFailed);
        accessText = (TextView) parentView.findViewById(R.id.accessText);
        accessFailedText = (TextView) parentView.findViewById(R.id.accessFailedText);
        refreshView = (ImageView) parentView.findViewById(R.id.refresh);
        setMode(mMode);
        if(drawableId!=-1){
            vertifyView.setImageResource(drawableId);
        }
        setBlockSize(blockSize);
        vertifyView.callback(new PictureVertifyView.Callback() {
            @Override
            public void onSuccess(long time) {
                if (mListener != null) {
                    String s = mListener.onAccess(time);
                    if (s != null) {
                        accessText.setText(s);
                    } else {
                        accessText.setText(String.format(getResources().getString(R.string.vertify_access), time));
                    }
                }
                accessSuccess.setVisibility(VISIBLE);
                accessFailed.setVisibility(GONE);
            }

            @Override
            public void onFailed() {
                seekbar.setEnabled(false);
                vertifyView.setTouchEnable(false);
                failCount = failCount > maxFailedCount ? maxFailedCount : failCount + 1;
                accessFailed.setVisibility(VISIBLE);
                accessSuccess.setVisibility(GONE);
                if (mListener != null) {
                    if (failCount == maxFailedCount) {
                        String s = mListener.onMaxFailed();
                        if (s != null) {
                            accessFailedText.setText(s);
                        } else {//默认文案
                            accessFailedText.setText(String.format(getResources().getString(R.string.vertify_failed), maxFailedCount - failCount));
                        }
                    } else {
                        String s = mListener.onFailed(failCount);
                        if (s != null) {
                            accessFailedText.setText(s);
                        } else {//默认文案
                            accessFailedText.setText(String.format(getResources().getString(R.string.vertify_failed), maxFailedCount - failCount));
                        }
                    }
                }
            }

        });
        setSeekBarStyle(progressDrawableId, thumbDrawableId);
        //슬라이더 슬립 로직 처리 부분
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isDown) {
                    isDown = false;
                    if (progress > 10) {
                        isResponse = false;
                    } else {
                        isResponse = true;
                        accessFailed.setVisibility(GONE);
                        vertifyView.down(0);
                    }
                }
                if (isResponse) {
                    vertifyView.move(progress);
                } else {
                    seekBar.setProgress(0);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isDown = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (isResponse) {
                    vertifyView.loose();
                }
            }
        });
        refreshView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startRefresh(v);
            }
        });
    }

    private void startRefresh(View v) {
        //새로고침 로직
        v.animate().rotationBy(360).setDuration(500)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        reset(false);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
    }


    public void setCaptchaListener(CaptchaListener listener) {
        this.mListener = listener;
    }

    public void setCaptchaStrategy(CaptchaStrategy strategy) {
        if (strategy != null) {
            vertifyView.setCaptchaStrategy(strategy);
        }
    }

    public void setSeekBarStyle(@DrawableRes int progressDrawable, @DrawableRes int thumbDrawable) {
        seekbar.setProgressDrawable(getResources().getDrawable(progressDrawable));
        seekbar.setThumb(getResources().getDrawable(thumbDrawable));
        seekbar.setThumbOffset(0);
    }

    /**
     * 슬라이더 이미지 크기 설정(px로 설정)
     */
    public void setBlockSize(int blockSize) {
        vertifyView.setBlockSize(blockSize);
    }

    /**
     * 슬라이더 확인 모드 설정
     */
    public void setMode(@Mode int mode) {
        this.mMode = mode;
        vertifyView.setMode(mode);
        if (mMode == MODE_NONBAR) {
            seekbar.setVisibility(GONE);
            vertifyView.setTouchEnable(true);
        } else {
            seekbar.setVisibility(VISIBLE);
            seekbar.setEnabled(true);
        }
        hideText();
    }

    public int getMode() {
        return this.mMode;
    }

    public void setMaxFailedCount(int count) {
        this.maxFailedCount = count;
    }

    public int getMaxFailedCount() {
        return this.maxFailedCount;
    }


    public void setBitmap(int drawableId) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), drawableId);
        setBitmap(bitmap);
    }

    public void setBitmap(Bitmap bitmap) {
        vertifyView.setImageBitmap(bitmap);
        reset(false);
    }

    public void setBitmap(String url) {
        mTask = new BitmapLoaderTask(new BitmapLoaderTask.Callback() {
            @Override
            public void result(Bitmap bitmap) {
                setBitmap(bitmap);
            }
        });
        mTask.execute(url);
    }

    @Override
    protected void onDetachedFromWindow() {
        if(mTask!=null&&mTask.getStatus().equals(AsyncTask.Status.RUNNING)){
            mTask.cancel(true);
        }
        super.onDetachedFromWindow();
    }

    /**
     * 재설정
     * @param clearFailed 실패 횟수를 지울지 여부
     */
    public void reset(boolean clearFailed) {
        hideText();
        vertifyView.reset();
        if (clearFailed) {
            failCount = 0;
        }
        if (mMode == MODE_BAR) {
            seekbar.setEnabled(true);
            seekbar.setProgress(0);
        } else {
            vertifyView.setTouchEnable(true);
        }
    }
    
    /**
     * 실패한 텍스트 표시
     * */
    public void hideText() {
        accessFailed.setVisibility(GONE);
        accessSuccess.setVisibility(GONE);
    }
}