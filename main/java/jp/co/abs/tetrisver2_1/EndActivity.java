package jp.co.abs.tetrisver2_1;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class EndActivity extends AppCompatActivity {

    private ScoreDB mScoreDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);

        //DB作成
        mScoreDB = new ScoreDB(getApplicationContext());

        TextView scoreView = findViewById(R.id.Score);
        TextView lineView = findViewById(R.id.Line);

        Intent intent = getIntent();
        int gameScore = intent.getIntExtra("score", 0);
        String score = String.valueOf(gameScore);
        int deleteLine = intent.getIntExtra("line", 0);
        String line = String.valueOf(deleteLine);

        scoreView.setText("Score: " + score);
        lineView.setText("Line: " + line);

        SQLiteDatabase db = mScoreDB.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("score", gameScore);
        values.put("line", deleteLine);

        db.insert("scoreDB", null, values);

    }

    public void onClick(View view) {
        Intent intent = new Intent(this, StartActivity.class);
        startActivity(intent);
        finish();
    }
}