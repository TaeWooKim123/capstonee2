package com.example.capstonee.Capcha;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;


public abstract class CaptchaStrategy {

    protected Context mContext;

    public CaptchaStrategy(Context ctx) {
        this.mContext = ctx;
    }

    protected Context getContext() {
        return mContext;
    }

    /**
     * 누락 된 블록 모양 정의
     *
     * @param blockSize 단위 dp, 픽셀로 변환
     * @return path of the shape
     */
    public abstract Path getBlockShape(int blockSize);

    /**
     * 누락 된 블록에 대한 위치 정보 정의
     *
     * @param width  picture width unit:px
     * @param height picture height unit:px
     * @param blockSize
     * @return position info of the block
     */
    public abstract PositionInfo getBlockPostionInfo(int width, int height, int blockSize);

    /**
     * 슬라이더 이미지의 위치 정보를 정의 (슬라이더 모드가 아닌 경우에만 유용함).
     *
     * @param width  picture width
     * @param height picture height
     * @return position info of the block
     */
    public PositionInfo getPositionInfoForSwipeBlock(int width, int height, int blockSize){
        return getBlockPostionInfo(width,height,blockSize);
    }

    /**
     * 그림자
     */
    public abstract Paint getBlockShadowPaint();

    /**
     * 슬라이더 이미지 가져오기
     */
    public abstract Paint getBlockBitmapPaint();

    /**
     *
     * 슬라이더 이미지를 장식하고 이미지를 그린 후에 실행. 즉, 슬라이더의 전경을 그림.
     */
    public void decoreateSwipeBlockBitmap(Canvas canvas, Path shape) {

    }
}
