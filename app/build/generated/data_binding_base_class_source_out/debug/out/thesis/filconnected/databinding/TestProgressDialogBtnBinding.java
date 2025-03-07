// Generated by view binder compiler. Do not edit!
package thesis.filconnected.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;
import thesis.filconnected.R;

public final class TestProgressDialogBtnBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final Button showDialogButton;

  private TestProgressDialogBtnBinding(@NonNull LinearLayout rootView,
      @NonNull Button showDialogButton) {
    this.rootView = rootView;
    this.showDialogButton = showDialogButton;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static TestProgressDialogBtnBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static TestProgressDialogBtnBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.test_progress_dialog_btn, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static TestProgressDialogBtnBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.showDialogButton;
      Button showDialogButton = ViewBindings.findChildViewById(rootView, id);
      if (showDialogButton == null) {
        break missingId;
      }

      return new TestProgressDialogBtnBinding((LinearLayout) rootView, showDialogButton);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
