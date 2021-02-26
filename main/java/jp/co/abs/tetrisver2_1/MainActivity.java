package jp.co.abs.tetrisver2_1;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = FieldView.class.getSimpleName();

    private class FieldView extends SurfaceView {

        private Random mRand = new Random(System.currentTimeMillis());

        private int[][] mBlock = Block.values()[mRand.nextInt(Block.values().length)].getBlock();
        private int[][] mBlock1 = Block.values()[mRand.nextInt(Block.values().length)].getBlock();
        private int[][] mBlock2 = Block.values()[mRand.nextInt(Block.values().length)].getBlock();
        private int[][] mBlock3 = Block.values()[mRand.nextInt(Block.values().length)].getBlock();
        private int[][][] mBlockBox = {mBlock1, mBlock2, mBlock3};

        private int mStartPosX = 4, mStartPosY = 0;
        private static final int NEXT_POS_X = 12;
        private static final int NEXT_POS_Y = 1;
        private int mMapWidth = 10;
        private int mMapHeight = 23;
        private int[][] mMap = new int[mMapHeight][];

        private int mGameStatus = 0;
        private static final int GAME_PLAYING = 0;
        private static final int GAME_OVER = 1;
        private int mScore;
        private int mLine;
        private int mSpeed = 1;

        // ブロックの移動処理
        private static final float ADJUST_X = 100.0f; // 作業において問題なく動いたため100.0fに設定
        private static final float ADJUST_Y = 100.0f; // 作業において問題なく動いたため100.0fに設定
        private float touchX;
        private float touchY;
        private float nowTouchX;
        private float nowTouchY;

        // onDraw内の変数
        private ShapeDrawable mBackGroundDrawable;
        private ShapeDrawable mFieldDrawable;
        private Paint mPaintText;

        {
            mBackGroundDrawable = new ShapeDrawable(new RectShape());
            mBackGroundDrawable.setBounds(0, 0, 705, 1733);
            mBackGroundDrawable.getPaint().setColor(0xFF000000);

            mFieldDrawable = new ShapeDrawable(new RectShape());
            mFieldDrawable.setBounds(0, 0, 700, 1731);
            mFieldDrawable.getPaint().setColor(0xFFDCDCDC);

            mPaintText = new Paint();
            mPaintText.setTextSize(50);
        }

        public FieldView(Context context) {
            super(context);

            setBackgroundColor(0xFFFFFFFF);
            setFocusable(true);
            setFocusableInTouchMode(true);
            requestFocus();
        }

        // 盤面を作る処理
        public void initGame() {
            for (int y = 0; y < mMapHeight; y++) {
                mMap[y] = new int[mMapWidth];
                for (int x = 0; x < mMapWidth; x++) {
                    mMap[y][x] = 0;
                }
            }
        }

        void gameOver() {
            for (int i = 0; i < 10; i++) {
                if (mMap[0][i] != 0) {
                    mGameStatus = GAME_OVER;

                    startActivity(EndActivity.createMainActivityIntent(MainActivity.this, mScore, mLine));
                    finish();
                }
            }
        }

        // ブロックに形をつける
        private void paintMatrix(Canvas canvas, int[][] matrix, int offsetX, int offsetY) {
            ShapeDrawable rect = new ShapeDrawable(new RectShape());
            int h = matrix.length;
            int w = matrix[0].length;

            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    if (matrix[y][x] != 0) {
                        if (mGameStatus == GAME_PLAYING) {
                            rect.getPaint().setColor(Block.findColor(matrix[y][x]));
                        } else if (mGameStatus == GAME_OVER) {
                            rect.getPaint().setColor(0xFF696969);
                        }
                        int px = (x + offsetX) * 70;
                        int py = (y + offsetY) * 70;
                        rect.setBounds(px, py, px + 69, py + 69);
                        rect.draw(canvas);

                    }
                }
            }
        }

        // ブロックが存在していいかどうか判別
        boolean check(int[][] block, int offsetX, int offsetY) {
            // 地面に落ちたかの処理
            if (offsetX < 0 || offsetY < 0 ||
                    mMapHeight < offsetY + block.length ||
                    mMapWidth < offsetX + block[0].length) {
                return false;
            }

            if (mMapHeight < offsetY + block.length * 70) {
                gameOver();
            }

            for (int y = 0; y < block.length; y++) {
                for (int x = 0; x < block[y].length; x++) {
                    if (block[y][x] != 0 && mMap[y + offsetY][x + offsetX] != 0) {
                        return false;
                    }
                }
            }

            for (int[] ints : mBlock1) {
                for (int anInt : ints) {
                    if (anInt != 0) {
                        mHandler.sendEmptyMessage(INVALIDATE);
                    }
                }
            }
            return true;
        }

        // ブロックをマップ上に表示
        void mergeMatrix(int[][] block, int offsetX, int offsetY) {
            for (int y = 0; y < block.length; y++) {
                for (int x = 0; x < block[0].length; x++) {
                    if (block[y][x] != 0) {
                        mMap[offsetY + y][offsetX + x] = block[y][x];
                    }
                }
            }
        }

        // 列がそろったら消す
        void clearRows() {

            int deleteLine = 0;
            // 埋まった行は消す。nullで一旦マーキング
            for (int y = 0; y < mMapHeight; y++) {
                boolean full = true;
                for (int x = 0; x < mMapWidth; x++) {
                    if (mMap[y][x] == 0) {
                        full = false;
                        break;
                    }
                }

                if (full) {
                    mMap[y] = null;
                }

            }

            // 新しいmapにnull以外の行を詰めてコピーする
            int[][] newMap = new int[mMapHeight][];
            int y2 = mMapHeight - 1;
            for (int y = mMapHeight - 1; y >= 0; y--) {
                if (mMap[y] == null) {
                    deleteLine++;
                    mLine++;
                    continue;
                } else {
                    newMap[y2--] = mMap[y];
                    switch (deleteLine) {
                        case 1:
                            mScore += 10;
                            break;
                        case 2:
                            mScore += 30;
                            break;
                        case 3:
                            mScore += 50;
                            break;
                        case 4:
                            mScore += 80;
                            break;
                    }
                }
                invalidate();
                deleteLine = 0;
            }

            // 消えた行数分新しい行を追加する
            for (int i = 0; i <= y2; i++) {
                int[] newRow = new int[mMapWidth];
                for (int j = 0; j < mMapWidth; j++) {
                    newRow[j] = 0;
                }
                newMap[i] = newRow;
            }
            mMap = newMap;
        }

        // 盤面に色を付ける
        @Override
        protected void onDraw(Canvas canvas) {
            mBackGroundDrawable.draw(canvas);

            mFieldDrawable.draw(canvas);

            String row = String.valueOf(mLine);
            String level = String.valueOf(mSpeed);

            canvas.drawText("Line:" + row, 800, 1250, mPaintText);
            canvas.drawText("Speed:" + level, 800, 1350, mPaintText);

            // 配置済みのブロックに色をつける
            paintMatrix(canvas, mMap, 0, 0);

            // 操作中のブロックに色をつける
            paintMatrix(canvas, mBlock, mStartPosX, mStartPosY);

            // Nextブロックに色をつける
            int k = 0;
            for (int i = 0; i < 3; i++) {
                paintMatrix(canvas, mBlockBox[i], NEXT_POS_X, NEXT_POS_Y + k);
                k = k + 5;
            }
        }

        // ブロックの回転処理
        int[][] rotate(final int[][] block) {
            int[][] rotated = new int[block[0].length][];
            for (int x = 0; x < block[0].length; x++) {
                rotated[x] = new int[block.length];
                for (int y = 0; y < block.length; y++) {
                    rotated[x][block.length - y - 1] = block[y][x];
                }
            }
            return rotated;
        }

        @Override
        public boolean performClick() {
            super.performClick();
            return true;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    performClick();
                    touchX = event.getX();
                    touchY = event.getY();
                    break;
                case MotionEvent.ACTION_UP:
                    performClick();
                    nowTouchX = event.getX();
                    nowTouchY = event.getY();
                    flickCheck();
                    break;
            }
            mHandler.sendEmptyMessage(INVALIDATE);
            return true;
        }

        /**
         * どの方向にフリックしたかチェック
         */
        private void flickCheck() {
            Log.d("FlickPoint", "startX:" + touchX + " endX:" + nowTouchX
                    + " startY:" + touchY + " endY:" + nowTouchY);
            // 左フリック
            if (touchX > nowTouchX) {
                if (touchX - nowTouchX > ADJUST_X) {
                    if (check(mBlock, mStartPosX - 1, mStartPosY)) {
                        mStartPosX = mStartPosX - 1;
                    }
                    return;
                }
            }
            // 右フリック
            if (nowTouchX > touchX) {
                if (nowTouchX - touchX > ADJUST_X) {
                    if (check(mBlock, mStartPosX + 1, mStartPosY)) {
                        mStartPosX = mStartPosX + 1;
                    }
                    return;
                }
            }
            // 上フリック
            if (touchY > nowTouchY) {
                if (touchY - nowTouchY > ADJUST_Y) {
                    int[][] newBlock = rotate(mBlock);
                    if (check(newBlock, mStartPosX, mStartPosY)) {
                        mBlock = newBlock;
                    }
                    return;
                }
            }
            // 下フリック
            if (nowTouchY > touchY) {
                if (nowTouchY - touchY > ADJUST_Y) {
                    int y = mStartPosY;
                    while (check(mBlock, mStartPosX, y)) {
                        y++;
                    }
                    if (y > 0) mStartPosY = y - 1;
                }
            }
        }

        /**
         * アニメーション開始
         */
        public void startAnimation() {
            mHandler.sendEmptyMessage(INVALIDATE);
            mHandler.sendEmptyMessage(DROP_BLOCK);
        }

        public void stopAnimation() {
            mHandler.removeMessages(INVALIDATE);
            mHandler.removeMessages(DROP_BLOCK);
        }

        private static final int INVALIDATE = 1;
        private static final int DROP_BLOCK = 2;
        // 時間経過でブロックを落とす
        private Handler mHandler = new DropBlockHandler(Looper.getMainLooper());

        private class DropBlockHandler extends Handler {

            DropBlockHandler(Looper looper) {
                super(looper);
            }

            @Override
            public void handleMessage(@NonNull Message msg) {
                if (mGameStatus == 0) {

                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle("Score: " + mScore);
                    }
                    Log.d("Block", Arrays.deepToString(mBlock1));

                    switch (msg.what) {
                        case INVALIDATE:
                            invalidate();
                            break;
                        case DROP_BLOCK:
                            if (check(mBlock, mStartPosX, mStartPosY + 1)) {
                                mStartPosY++;
                            } else {
                                mergeMatrix(mBlock, mStartPosX, mStartPosY);
                                clearRows();
                                mStartPosX = 4;
                                mStartPosY = 0;

                                mBlock = mBlockBox[0];
                                mBlockBox[0] = mBlockBox[1];
                                mBlockBox[1] = mBlockBox[2];
                                mBlockBox[2] = Block.values()[mRand.nextInt(Block.values().length)].getBlock();
                            }

                            invalidate();
                            Message massage = new Message();
                            massage.what = DROP_BLOCK;
                            if (mLine <= 14) {
                                sendMessageDelayed(massage, 500);
                            } else if (mLine <= 29) {
                                sendMessageDelayed(massage, 300);
                                mSpeed = 2;
                            } else {
                                sendMessageDelayed(massage, 100);
                                mSpeed = 3;
                            }
                            break;
                    }
                }
            }
        }
    }

    private static final int BLOCK_BLANK = 0;
    private static final int BLOCK_L = 1;
    private static final int BLOCK_LL = 2;
    private static final int BLOCK_O = 3;
    private static final int BLOCK_T = 4;
    private static final int BLOCK_S = 5;
    private static final int BLOCK_Z = 6;
    private static final int BLOCK_I = 7;

    private enum Block {
        L(BLOCK_L,
                new int[][]{
                        {BLOCK_L, BLOCK_L},
                        {BLOCK_BLANK, BLOCK_L},
                        {BLOCK_BLANK, BLOCK_L}},
                0xFF008000),
        LL(BLOCK_LL,
                new int[][]{
                        {BLOCK_LL, BLOCK_LL},
                        {BLOCK_LL, BLOCK_BLANK},
                        {BLOCK_LL, BLOCK_BLANK}},
                0xFF008080),
        O(BLOCK_O,
                new int[][]{
                        {BLOCK_BLANK, BLOCK_BLANK},
                        {BLOCK_O, BLOCK_O},
                        {BLOCK_O, BLOCK_O}},
                0xFFFFFF00),
        T(BLOCK_T,
                new int[][]{
                        {BLOCK_T, BLOCK_BLANK},
                        {BLOCK_T, BLOCK_T},
                        {BLOCK_T, BLOCK_BLANK}},
                0xFF800080),
        S(BLOCK_S,
                new int[][]{
                        {BLOCK_S, BLOCK_BLANK},
                        {BLOCK_S, BLOCK_S},
                        {BLOCK_BLANK, BLOCK_S}},
                0xFF00FF00),
        Z(BLOCK_Z,
                new int[][]{
                        {BLOCK_BLANK, BLOCK_Z},
                        {BLOCK_Z, BLOCK_Z},
                        {BLOCK_Z, BLOCK_BLANK}},
                0xFFFF0000),
        I(BLOCK_I,
                new int[][]{
                        {BLOCK_I},
                        {BLOCK_I},
                        {BLOCK_I},
                        {BLOCK_I}},
                0xFF0000FF),
        ;

        private int mBlockNumber;
        private int[][] mBlock;
        private int mColor;

        Block(int blockNumber, int[][] block, int color) {
            mBlockNumber = blockNumber;
            mBlock = block;
            mColor = color;
        }

        public int getBlockNumber() {
            return mBlockNumber;
        }

        public int[][] getBlock() {
            return mBlock;
        }

        public int getColor() {
            return mColor;
        }

        /**
         * 指定されたブロックの色を取得する
         *
         * @param blockNumber ブロックの種類
         * @return 指定されたブロックの色　ブロックの種類が一致しなかった場合、白色を返す
         */
        public static int findColor(int blockNumber) {
            for (Block block : Block.values()) {
                if (blockNumber == block.getBlockNumber()) {
                    Log.i(TAG, "block: " + blockNumber);
                    Log.i(TAG, "block: " + block);
                    return block.getColor();
                }
            }
            return 0xFFDCDCDC;
        }
    }

    private BGMPlayer bgm;
    private FieldView mFieldView;

    private void setFieldView() {
        if (mFieldView == null) {
            mFieldView = new FieldView(getApplication());
            setContentView(mFieldView);
        }
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        this.bgm = new BGMPlayer(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setFieldView();

        SharedPreferences Count = getSharedPreferences(PrefConst.PREF_NAME, Context.MODE_PRIVATE);
        boolean isBgmON = Count.getBoolean(PrefConst.KEY_BGM_STATUS, false);

        if (isBgmON) {
            bgm.start();
        } else {
            bgm.stop();
        }

        mFieldView.initGame();
        mFieldView.startAnimation();
        Looper.myQueue().addIdleHandler(new Idler());

        mFieldView.mGameStatus = FieldView.GAME_PLAYING;
    }

    @Override
    protected void onPause() {
        super.onPause();
        bgm.stop();
        mFieldView.stopAnimation();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFieldView.stopAnimation();
    }

    static class Idler implements MessageQueue.IdleHandler {
        public Idler() {
            super();
        }

        public final boolean queueIdle() {
            return false;
        }
    }
}