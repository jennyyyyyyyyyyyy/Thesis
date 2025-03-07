// Generated by view binder compiler. Do not edit!
package thesis.filconnected.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;
import thesis.filconnected.R;

public final class ApTwoAudioBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final ImageView audioIcon;

  @NonNull
  public final LinearLayout textLayout;

  private ApTwoAudioBinding(@NonNull ConstraintLayout rootView, @NonNull ImageView audioIcon,
      @NonNull LinearLayout textLayout) {
    this.rootView = rootView;
    this.audioIcon = audioIcon;
    this.textLayout = textLayout;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ApTwoAudioBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ApTwoAudioBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.ap_two_audio, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ApTwoAudioBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.audioIcon;
      ImageView audioIcon = ViewBindings.findChildViewById(rootView, id);
      if (audioIcon == null) {
        break missingId;
      }

      id = R.id.text_layout;
      LinearLayout textLayout = ViewBindings.findChildViewById(rootView, id);
      if (textLayout == null) {
        break missingId;
      }

      return new ApTwoAudioBinding((ConstraintLayout) rootView, audioIcon, textLayout);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
