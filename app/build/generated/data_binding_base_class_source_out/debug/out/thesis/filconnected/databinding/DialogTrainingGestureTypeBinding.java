// Generated by view binder compiler. Do not edit!
package thesis.filconnected.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;
import thesis.filconnected.R;

public final class DialogTrainingGestureTypeBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final TextView dynamicGesture;

  @NonNull
  public final TextView staticGesture;

  @NonNull
  public final TextView title;

  private DialogTrainingGestureTypeBinding(@NonNull LinearLayout rootView,
      @NonNull TextView dynamicGesture, @NonNull TextView staticGesture, @NonNull TextView title) {
    this.rootView = rootView;
    this.dynamicGesture = dynamicGesture;
    this.staticGesture = staticGesture;
    this.title = title;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static DialogTrainingGestureTypeBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static DialogTrainingGestureTypeBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.dialog_training_gesture_type, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static DialogTrainingGestureTypeBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.dynamicGesture;
      TextView dynamicGesture = ViewBindings.findChildViewById(rootView, id);
      if (dynamicGesture == null) {
        break missingId;
      }

      id = R.id.staticGesture;
      TextView staticGesture = ViewBindings.findChildViewById(rootView, id);
      if (staticGesture == null) {
        break missingId;
      }

      id = R.id.title;
      TextView title = ViewBindings.findChildViewById(rootView, id);
      if (title == null) {
        break missingId;
      }

      return new DialogTrainingGestureTypeBinding((LinearLayout) rootView, dynamicGesture,
          staticGesture, title);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
