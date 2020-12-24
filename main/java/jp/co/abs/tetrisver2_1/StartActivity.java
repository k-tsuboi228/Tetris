package jp.co.abs.tetrisver2_1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private ScoreDB mScoreDB;
    private TextView mHighScoreView;
    private Switch mBGMSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
//DB作成
        mScoreDB = new ScoreDB(getApplicationContext());

        mHighScoreView = findViewById(R.id.highScore);

        SQLiteDatabase db = mScoreDB.getReadableDatabase();

        String order_by = "score DESC, line ASC";

        Cursor cursor = db.query(
                "scoreDB",
                new String[]{"score", "line"},
                null,
                null,
                null,
                null,
                order_by
        );

        boolean mov = cursor.moveToFirst();

        StringBuilder sBuilder = new StringBuilder();

        if (mov) {
            for (int i = 0; i < 3; i++) {
                sBuilder.append("Score: ");
                sBuilder.append(cursor.getInt(0));
                sBuilder.append("\t" + "Line: ");
                sBuilder.append(cursor.getInt(1) + "\n");
                cursor.moveToNext();
            }
        }

        cursor.close();

        mHighScoreView.setText(sBuilder.toString());
        mBGMSwitch = findViewById(R.id.BGMSwitch);

        mBGMSwitch.setOnCheckedChangeListener(this);
    }

    public void onClick(View view) {
        Intent intent = new Intent(this, TetrisActivity.class);
        startActivity(intent);
        finish();
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        SharedPreferences Count = getSharedPreferences("DataSave", Context.MODE_PRIVATE);
        SharedPreferences.Editor Editor = Count.edit();

        Editor.putBoolean("Switch", isChecked);

        Editor.apply();
        Editor.commit();

        if (isChecked == true) {
            mBGMSwitch.setText("ON");
        } else {
            mBGMSwitch.setText("OFF");
        }
    }
}