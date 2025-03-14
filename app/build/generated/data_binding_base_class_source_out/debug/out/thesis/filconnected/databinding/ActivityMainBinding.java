// Generated by view binder compiler. Do not edit!
package thesis.filconnected.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.ui.PlayerView;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;
import thesis.filconnected.R;

public final class ActivityMainBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final Button btnDelete;

  @NonNull
  public final Button btnListVideos;

  @NonNull
  public final Button btnRename;

  @NonNull
  public final Button btnShowVideo;

  @NonNull
  public final Button btnUpload;

  @NonNull
  public final EditText etCustomFilename;

  @NonNull
  public final EditText etDeleteFilename;

  @NonNull
  public final EditText etNewName;

  @NonNull
  public final EditText etOldName;

  @NonNull
  public final EditText etShowFilename;

  @NonNull
  public final TextView tvResult;

  @NonNull
  public final PlayerView videoView;

  private ActivityMainBinding(@NonNull LinearLayout rootView, @NonNull Button btnDelete,
      @NonNull Button btnListVideos, @NonNull Button btnRename, @NonNull Button btnShowVideo,
      @NonNull Button btnUpload, @NonNull EditText etCustomFilename,
      @NonNull EditText etDeleteFilename, @NonNull EditText etNewName, @NonNull EditText etOldName,
      @NonNull EditText etShowFilename, @NonNull TextView tvResult, @NonNull PlayerView videoView) {
    this.rootView = rootView;
    this.btnDelete = btnDelete;
    this.btnListVideos = btnListVideos;
    this.btnRename = btnRename;
    this.btnShowVideo = btnShowVideo;
    this.btnUpload = btnUpload;
    this.etCustomFilename = etCustomFilename;
    this.etDeleteFilename = etDeleteFilename;
    this.etNewName = etNewName;
    this.etOldName = etOldName;
    this.etShowFilename = etShowFilename;
    this.tvResult = tvResult;
    this.videoView = videoView;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityMainBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityMainBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_main, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityMainBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.btnDelete;
      Button btnDelete = ViewBindings.findChildViewById(rootView, id);
      if (btnDelete == null) {
        break missingId;
      }

      id = R.id.btnListVideos;
      Button btnListVideos = ViewBindings.findChildViewById(rootView, id);
      if (btnListVideos == null) {
        break missingId;
      }

      id = R.id.btnRename;
      Button btnRename = ViewBindings.findChildViewById(rootView, id);
      if (btnRename == null) {
        break missingId;
      }

      id = R.id.btnShowVideo;
      Button btnShowVideo = ViewBindings.findChildViewById(rootView, id);
      if (btnShowVideo == null) {
        break missingId;
      }

      id = R.id.btnUpload;
      Button btnUpload = ViewBindings.findChildViewById(rootView, id);
      if (btnUpload == null) {
        break missingId;
      }

      id = R.id.etCustomFilename;
      EditText etCustomFilename = ViewBindings.findChildViewById(rootView, id);
      if (etCustomFilename == null) {
        break missingId;
      }

      id = R.id.etDeleteFilename;
      EditText etDeleteFilename = ViewBindings.findChildViewById(rootView, id);
      if (etDeleteFilename == null) {
        break missingId;
      }

      id = R.id.etNewName;
      EditText etNewName = ViewBindings.findChildViewById(rootView, id);
      if (etNewName == null) {
        break missingId;
      }

      id = R.id.etOldName;
      EditText etOldName = ViewBindings.findChildViewById(rootView, id);
      if (etOldName == null) {
        break missingId;
      }

      id = R.id.etShowFilename;
      EditText etShowFilename = ViewBindings.findChildViewById(rootView, id);
      if (etShowFilename == null) {
        break missingId;
      }

      id = R.id.tvResult;
      TextView tvResult = ViewBindings.findChildViewById(rootView, id);
      if (tvResult == null) {
        break missingId;
      }

      id = R.id.videoView;
      PlayerView videoView = ViewBindings.findChildViewById(rootView, id);
      if (videoView == null) {
        break missingId;
      }

      return new ActivityMainBinding((LinearLayout) rootView, btnDelete, btnListVideos, btnRename,
          btnShowVideo, btnUpload, etCustomFilename, etDeleteFilename, etNewName, etOldName,
          etShowFilename, tvResult, videoView);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
