package nooblife.lockit.activities;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

import nooblife.lockit.R;
import nooblife.lockit.util.LockItServer;

public class ConnectActivity extends Activity {

    LockItServer lockItServer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        findViewById(R.id.connect_cancel).setOnClickListener(v1 -> {
            lockItServer.stop();
            setResult(RESULT_CANCELED);
            finish();
        });

        lockItServer = LockItServer.get(this)
                .onPair(() -> {
                    lockItServer.stop();
                    setResult(RESULT_OK);
                    finish();
                });

        lockItServer.start();
    }

}
