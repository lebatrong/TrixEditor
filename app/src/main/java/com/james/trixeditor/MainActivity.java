package com.james.trixeditor;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private TrixEditor mEditor;
    private TrixEditor mResult;
    private Button btnGetData;
    private Button btnSetData;

    private String resultHTML;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditor = findViewById(R.id.mEditor);
        mResult = findViewById(R.id.mResult);
        btnGetData = findViewById(R.id.btnGetContent);
        btnSetData = findViewById(R.id.btnSetContent);

        btnGetData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.getHtml(new TrixEditor.ICallbackContent() {
                    @Override
                    public void onResult(String content) {
                        resultHTML = content;
                        Log.d(TAG, "Content: " + resultHTML);
                    }
                });


            }
        });

        btnSetData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mResult.setHtml(resultHTML);
            }
        });
    }
}