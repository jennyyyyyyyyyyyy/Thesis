package thesis.filconnected.test;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import thesis.filconnected.R;

public class ProgressDialog extends AppCompatActivity {

    AlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_progress_dialog_btn);

        Button showDialogButton = findViewById(R.id.showDialogButton);

        showDialogButton.setOnClickListener(view -> {
            showProgressDialog(); // Show the progress dialog

            // Simulate a delay for loading (e.g., network request)
            view.postDelayed(() -> {
                dismissProgressDialog(); // Dismiss the progress dialog
            }, 3000); // 3-second delay
        });
    }

    private void showProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.progress_dialog, null);
        builder.setView(dialogView);
        builder.setCancelable(false); // Prevent dismiss by tapping outside
        progressDialog = builder.create();
//        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // Make background transparent
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
