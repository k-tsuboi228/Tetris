package jp.co.abs.tetrisver2_1;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class EndActivity extends AppCompatActivity {

    private static final String INTENT_PARAM_SCORE = "score";
    private static final String INTENT_PARAM_LINE = "line";

    public static Intent createMainActivityIntent(Context context, int score, int line){
        Intent intent = new Intent(context, EndActivity.class);
        intent.putExtra(INTENT_PARAM_SCORE,score);
        intent.putExtra(INTENT_PARAM_LINE,line);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);

        //DB作成
        ScoreDB scoreDB = new ScoreDB(getApplicationContext());

        TextView scoreView = findViewById(R.id.Score);
        TextView lineView = findViewById(R.id.Line);

        Intent intent = getIntent();
        int gameScore = intent.getIntExtra("score", 0);
        int deleteLine = intent.getIntExtra("line", 0);

        scoreView.setText(getString(R.string.text_score, gameScore));
        lineView.setText(getString(R.string.text_line, deleteLine));

        scoreDB.saveData(gameScore, deleteLine);
    }

    public void onClick(View view) {
        Intent intent = new Intent(this, StartActivity.class);
        startActivity(intent);
        finish();
    }
}