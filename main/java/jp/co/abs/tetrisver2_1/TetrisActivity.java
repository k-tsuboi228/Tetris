package jp.co.abs.tetrisver2_1;

import android.content.Context;
import android.content.Intent;
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

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class TetrisActivity extends AppCompatActivity {

    private class FieldView extends SurfaceView {

        private Random mRand = new Random(System.currentTimeMillis());

        private int[][][] mBlocks = {
                {
                        {1, 1},
                        {0, 1},
                        {0, 1}
                },
                {
                        {2, 2},
                        {2, 0},
                        {2, 0}
                },
                {
                        {0, 0},
                        {3, 3},
                        {3, 3}

                },
                {
                        {4, 0},
                        {4, 4},
                        {4, 0}
                },
                {
                        {5, 0},
                        {5, 5},
                        {0, 5}
                },
                {
                        {0, 6},
                        {6, 6},
                        {6, 0}
                },
                {
                        {7},
                        {7},
                        {7},
                        {7}
                }
        };

        private int[][] mBlock = mBlocks[mRand.nextInt(mBlocks.length)];
        private int[][] mBlock1 = mBlocks[mRand.nextInt(mBlocks.length)];
        private int[][] mBlock2 = mBlocks[mRand.nextInt(mBlocks.length)];
        private int[][] mBlock3 = mBlocks[mRand.nextInt(mBlocks.length)];
        private int[][][] mBlockBox = {mBlock1, mBlock2, mBlock3};

        private int mStartPosx = 4, mStartPosy = 0;
        private int mNextPosx = 12, mNextPosy = 1;
        private int mMapWidth = 10;
        private int mMapHeight = 23;
        private int[][] mMap = new int[mMapHeight][];

        private int mGameStatus = 0;
        private static final int GAME_PLAYING = 0;
        private static final int GAME_OVER = 1;
        private int mScore;
        private int mLine;
        private int mSpeed = 1;

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

        void GameOver() {
            for (int i = 0; i < 10; i++) {
                if (mMap[0][i] != 0) {
                    mGameStatus = GAME_OVER;

                    Intent intent = new Intent(TetrisActivity.this, EndActivity.class);
                    intent.putExtra("score", mScore);
                    intent.putExtra("line", mLine);
                    startActivity(intent);
                    finish();
                }
            }
        }

        // ブロックに形をつける
        private void paintMatrix(Canvas canvas, int[][] matrix, int offsetx, int offsety, int color) {
            ShapeDrawable rect = new ShapeDrawable(new RectShape());
            rect.getPaint().setColor(color);
            int h = matrix.length;
            int w = matrix[0].length;

            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    if (matrix[y][x] != 0) {
                        int px = (x + offsetx) * 70;
                        int py = (y + offsety) * 70;
                        rect.setBounds(px, py, px + 69, py + 69);
                        rect.draw(canvas);
                    }

                }
            }
        }

        // ブロックが存在していいかどうか判別
        boolean check(int[][] block, int offsetx, int offsety) {
            // 地面に落ちたかの処理
            if (offsetx < 0 || offsety < 0 ||
                    mMapHeight < offsety + block.length ||
                    mMapWidth < offsetx + block[0].length) {
                return false;
            }

            if (mMapHeight < offsety + block.length * 70) {
                GameOver();
            }

            for (int y = 0; y < block.length; y++) {
                for (int x = 0; x < block[y].length; x++) {
                    if (block[y][x] != 0 && mMap[y + offsety][x + offsetx] != 0) {
                        return false;
                    }
                }
            }

            for (int y = 0; y < mBlock1.length; y++) {
                for (int x = 0; x < mBlock1[y].length; x++) {
                    if (mBlock1[y][x] != 0) {
                        mHandler.sendEmptyMessage(INVALIDATE);
                    }
                }
            }
            return true;
        }

        // ブロックをマップ上に表示
        void mergeMatrix(int[][] block, int offsetx, int offsety) {
            for (int y = 0; y < block.length; y++) {
                for (int x = 0; x < block[0].length; x++) {
                    if (block[y][x] != 0) {
                        mMap[offsety + y][offsetx + x] = block[y][x];
                    }
                }
            }
        }

        // 列がそろったら消す
        void clearRows() {

            int deleteline = 0;
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
                    deleteline++;
                    mLine++;
                    continue;
                } else {
                    newMap[y2--] = mMap[y];
                    switch (deleteline) {
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
                deleteline = 0;
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

            ShapeDrawable Background = new ShapeDrawable(new RectShape());
            Background.setBounds(0, 0, 705, 1733);
            Background.getPaint().setColor(0xFF000000);
            Background.draw(canvas);

            ShapeDrawable rect = new ShapeDrawable(new RectShape());
            rect.setBounds(0, 0, 700, 1731);
            rect.getPaint().setColor(0xFFDCDCDC);
            rect.draw(canvas);

            Paint paint = new Paint();
            String row = String.valueOf(mLine);
            String level = String.valueOf(mSpeed);
            paint.setTextSize(50);

            canvas.drawText("Line:" + row, 800, 1250, paint);
            canvas.drawText("Speed:" + level, 800, 1350, paint);

            ShapeDrawable putBlock = new ShapeDrawable(new RectShape());

            if (mGameStatus == GAME_PLAYING) {
                for (int y = 0; y < mMap.length; y++) {
                    for (int x = 0; x < mMap[0].length; x++) {
                        int px = x * 70;
                        int py = y * 70;
                        switch (mMap[y][x]) {
                            case 0:
                                putBlock.getPaint().setColor(0xFFDCDCDC);
                                break;
                            case 1:
                                putBlock.getPaint().setColor(0xFF008000);
                                break;
                            case 2:
                                putBlock.getPaint().setColor(0xFF008080);
                                break;
                            case 3:
                                putBlock.getPaint().setColor(0xFFFFFF00);
                                break;
                            case 4:
                                putBlock.getPaint().setColor(0xFF800080);
                                break;
                            case 5:
                                putBlock.getPaint().setColor(0xFF00FF00);
                                break;
                            case 6:
                                putBlock.getPaint().setColor(0xFFFF0000);
                                break;
                            case 7:
                                putBlock.getPaint().setColor(0xFF0000FF);
                                break;
                        }
                        putBlock.setBounds(px, py, px + 69, py + 69);
                        putBlock.draw(canvas);
                    }
                }

                Block2(canvas, mBlock, mStartPosx, mStartPosy);

                int k = 0;
                for (int i = 0; i < 3; i++) {
                    Block2(canvas, mBlockBox[i], mNextPosx, mNextPosy + k);
                    k = k + 5;
                }
            }
            if (mGameStatus == GAME_OVER) {
                paintMatrix(canvas, mMap, 0, 0, 0xFF696969);
            }
        }

        private void Block2(Canvas canvas, int[][] block, int posx, int posy) {
            for (int y = 0; y < block.length; y++) {
                for (int x = 0; x < block[0].length; x++) {
                    switch (block[y][x]) {
                        case 1:
                            paintMatrix(canvas, block, posx, posy, 0xFF008000);
                            break;
                        case 2:
                            paintMatrix(canvas, block, posx, posy, 0xFF008080);
                            break;
                        case 3:
                            paintMatrix(canvas, block, posx, posy, 0xFFFFFF00);
                            break;
                        case 4:
                            paintMatrix(canvas, block, posx, posy, 0xFF800080);
                            break;
                        case 5:
                            paintMatrix(canvas, block, posx, posy, 0xFF00FF00);
                            break;
                        case 6:
                            paintMatrix(canvas, block, posx, posy, 0xFFFF0000);
                            break;
                        case 7:
                            paintMatrix(canvas, block, posx, posy, 0xFF0000FF);
                            break;
                    }
                }
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

        // ブロックの移動処理
        private float adjustX = 100.0f;
        private float adjustY = 100.0f;
        private float touchX;
        private float touchY;
        private float nowTouchX;
        private float nowTouchY;

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
                    FlickCheck();
                    break;
            }
            mHandler.sendEmptyMessage(INVALIDATE);
            return true;
        }

        // どの方向にフリックしたかチェック
        private void FlickCheck() {
            Log.d("FlickPoint", "startX:" + touchX + " endX:" + nowTouchX
                    + " startY:" + touchY + " endY:" + nowTouchY);
            // 左フリック
            if (touchX > nowTouchX) {
                if (touchX - nowTouchX > adjustX) {
                    if (check(mBlock, mStartPosx - 1, mStartPosy)) {
                        mStartPosx = mStartPosx - 1;
                    }
                    return;
                }
            }
            // 右フリック
            if (nowTouchX > touchX) {
                if (nowTouchX - touchX > adjustX) {
                    if (check(mBlock, mStartPosx + 1, mStartPosy)) {
                        mStartPosx = mStartPosx + 1;
                    }
                    return;
                }
            }
            // 上フリック
            if (touchY > nowTouchY) {
                if (touchY - nowTouchY > adjustY) {
                    int[][] newBlock = rotate(mBlock);
                    if (check(newBlock, mStartPosx, mStartPosy)) {
                        mBlock = newBlock;
                    }
                    return;
                }
            }
            // 下フリック
            if (nowTouchY > touchY) {
                if (nowTouchY - touchY > adjustY) {
                    int y = mStartPosy;
                    while (check(mBlock, mStartPosx, y)) {
                        y++;
                    }
                    if (y > 0) mStartPosy = y - 1;
                    return;
                }
            }
        }

        public void startAnime() {
            mHandler.sendEmptyMessage(INVALIDATE);
            mHandler.sendEmptyMessage(DROPBLOCK);
        }

        public void stopAnime() {
            mHandler.removeMessages(INVALIDATE);
            mHandler.removeMessages(DROPBLOCK);
        }

        private static final int INVALIDATE = 1;
        private static final int DROPBLOCK = 2;
        // 時間経過でブロックを落とす
        private final Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (mGameStatus == 0) {

                    getSupportActionBar().setTitle("Score: " + mScore);
                    Log.d("Block", String.valueOf(mBlock1));

                    switch (msg.what) {
                        case INVALIDATE:
                            invalidate();
                            break;
                        case DROPBLOCK:
                            if (check(mBlock, mStartPosx, mStartPosy + 1)) {
                                mStartPosy++;
                            } else {
                                mergeMatrix(mBlock, mStartPosx, mStartPosy);
                                clearRows();
                                mStartPosx = 4;
                                mStartPosy = 0;

                                mBlock = mBlockBox[0];
                                mBlockBox[0] = mBlockBox[1];
                                mBlockBox[1] = mBlockBox[2];
                                mBlockBox[2] = mBlocks[mRand.nextInt(mBlocks.length)];
                            }

                            invalidate();
                            Message massage = new Message();
                            massage.what = DROPBLOCK;
                            if (mLine <= 14) {
                                sendMessageDelayed(massage, 500);
                            } else if (mLine >= 15 && mLine <= 29) {
                                sendMessageDelayed(massage, 300);
                                mSpeed = 2;
                            } else if (mLine >= 30) {
                                sendMessageDelayed(massage, 100);
                                mSpeed = 3;
                            }
                            break;
                    }
                }
            }
        };
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

        SharedPreferences Count = getSharedPreferences("DataSave", Context.MODE_PRIVATE);
        Boolean bgmSwitch = Count.getBoolean("Switch", false);

        if (bgmSwitch == true) {
            bgm.start();
        } else {
            bgm.stop();
        }
        mFieldView.initGame();
        mFieldView.startAnime();
        Looper.myQueue().addIdleHandler(new Idler());

        mFieldView.mGameStatus = mFieldView.GAME_PLAYING;

    }

    @Override
    protected void onPause() {
        super.onPause();
        bgm.stop();
        mFieldView.stopAnime();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFieldView.stopAnime();
    }

    class Idler implements MessageQueue.IdleHandler {
        public Idler() {
            super();
        }

        public final boolean queueIdle() {
            return false;
        }
    }

}