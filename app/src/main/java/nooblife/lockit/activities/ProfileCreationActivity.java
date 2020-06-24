package nooblife.lockit.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;

import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import nooblife.lockit.R;
import nooblife.lockit.adapter.ApplicationAdapter;
import nooblife.lockit.database.BlacklistDatabase;
import nooblife.lockit.model.Application;
import nooblife.lockit.model.Blacklist;
import nooblife.lockit.util.MarginDividerItemDecoration;
import nooblife.lockit.util.Utils;

public class ProfileCreationActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    private ArrayList<Application> applicationArrayList = new ArrayList<>();
    private ArrayList<Application> whitelistedApplicationList = new ArrayList<>();

    private ApplicationAdapter applicationAdapter;
    private ApplicationAdapter whitelistedApplicationAdapter;
    private RecyclerView applicationListRecycler;
    private EditText profileNameInput;
    private TextInputLayout profileName;
    private ConstraintLayout whiteListCard;
    private Button addButton;
    private TextView cancelButton;
    private PackageManager pm;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_creator);

        pm = getPackageManager();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        profileName = findViewById(R.id.profile_name_layout);
        profileNameInput = findViewById(R.id.profile_name_edittext);
        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.VISIBLE);
        applicationListRecycler = findViewById(R.id.apps_list);
        applicationListRecycler.addItemDecoration(new MarginDividerItemDecoration(this));
        applicationAdapter = new ApplicationAdapter(this, applicationArrayList, R.layout.app_item);
        applicationListRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        applicationListRecycler.setAdapter(applicationAdapter);

        applicationAdapter.setOnItemClicked(new ApplicationAdapter.onItemClicked() {
            @Override
            public void onHolderClick(int position, Application application) {
                whitelistedApplicationList.add(application);
                whitelistedApplicationAdapter.notifyItemInserted(whitelistedApplicationList.size() - 1);
                applicationArrayList.remove(position);
                applicationAdapter.notifyItemRemoved(position);
                TransitionManager.beginDelayedTransition((ViewGroup) findViewById(android.R.id.content));
                whiteListCard.setVisibility(View.VISIBLE);
                profileName.setVisibility(View.VISIBLE);
            }
        });

        whitelistedApplicationAdapter = new ApplicationAdapter(this, whitelistedApplicationList, R.layout.app_item_grid);
        whitelistedApplicationAdapter.setOnItemClicked(new ApplicationAdapter.onItemClicked() {
            @Override
            public void onHolderClick(int position, Application application) {

            }
        });

        RecyclerView whiteListRecycler = findViewById(R.id.whitelisted_apps);
        whiteListRecycler.setAdapter(whitelistedApplicationAdapter);
        whiteListRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        whiteListCard = findViewById(R.id.whitelist_card);

        addButton = findViewById(R.id.add_to_whitelist_button);
        cancelButton = findViewById(R.id.cancel_button);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String profileName = profileNameInput.getText().toString();

                if (profileName.equals("")) {
                    profileNameInput.setError("Profile Name cannot be empty");
                    return;
                }

                final ArrayList<String> pkgList = new ArrayList<>();

                Log.d(TAG, "Creating Profile " + profileName);

                for (Application a : whitelistedApplicationList) {
                    pkgList.add(a.getApplicationPackageName());
                }

                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        BlacklistDatabase.getInstance(ProfileCreationActivity.this).blacklistDao().createBlacklist(new Blacklist(pkgList));
                    }
                });

//                if (status == -1) {
//                    profileNameInput.setError("This profile already exists");
//                } else {
                Toast.makeText(ProfileCreationActivity.this.getApplicationContext(),
                        "Profile '" + profileName + "' created", Toast.LENGTH_LONG).show();
                finish();
//                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (whitelistedApplicationList.size() == 0) return;

                whitelistedApplicationList.clear();
                // FIXME This is bad ;-;
                applicationArrayList = new ArrayList<>();
                applicationAdapter.notifyDataSetChanged();
                applicationListRecycler.setAdapter(null);
                applicationListRecycler.setAdapter(applicationAdapter);
                TransitionManager.beginDelayedTransition((ViewGroup) findViewById(android.R.id.content));
                whiteListCard.setVisibility(View.GONE);
                profileName.setVisibility(View.GONE);
            }
        });

        Utils.retrieveApplicationList(this, new Utils.AppRetrivalInterface() {
            @Override
            public void onProgress() {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onComplete(List<Application> applications) {
                applicationArrayList.clear();
                applicationArrayList.addAll(applications);
                applicationAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

}
