package jp.co.abs.tetrisver2_1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class StartActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private static final String DB_TABLE_NAME = "scoreDB";
    private static final String COLUMN_NAME_SCORE = "score";
    private static final String COLUMN_NAME_LINE = "line";
    private static final String ORDER_BY = "score DESC, line ASC";

    private static final int COLUMN_INDEX_SCORE = 0;
    private static final int COLUMN_INDEX_LINE = 1;

    private SwitchCompat mBGMSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

//DB作成
        ScoreDB scoreDB = new ScoreDB(getApplicationContext());

        TextView highScoreView = findViewById(R.id.highScore);

        SQLiteDatabase db = scoreDB.getReadableDatabase();

        Cursor cursor = db.query(
                DB_TABLE_NAME,
                new String[]{COLUMN_NAME_SCORE, COLUMN_NAME_LINE},
                null,
                null,
                null,
                null,
                ORDER_BY
        );

        boolean mov = cursor.moveToFirst();

        StringBuilder sBuilder = new StringBuilder();

        if (mov) {
            for (int i = 0; i < 3; i++) {
                sBuilder.append("Score: ")
                        .append(cursor.getInt(COLUMN_INDEX_SCORE))
                        .append("\t Line: ")
                        .append(cursor.getInt(COLUMN_INDEX_LINE))
                        .append("\n");
                cursor.moveToNext();
            }
        }

        cursor.close();

        highScoreView.setText(sBuilder.toString());
        mBGMSwitch = findViewById(R.id.BGMSwitch);

        mBGMSwitch.setOnCheckedChangeListener(this);
    }

    public void onClick(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        SharedPreferences Count = getSharedPreferences(PrefConst.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor Editor = Count.edit();

        Editor.putBoolean(PrefConst.KEY_BGM_STATUS, isChecked);

        Editor.apply();
        Editor.commit();

        if (isChecked) {
            mBGMSwitch.setText(getString(R.string.bgm_switch_on));
        } else {
            mBGMSwitch.setText(getString(R.string.bgm_switch_off));
        }
    }
}