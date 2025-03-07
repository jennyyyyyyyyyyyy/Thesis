// Generated by view binder compiler. Do not edit!
package thesis.filconnected.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;
import thesis.filconnected.R;

public final class ActivityDevInfoBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final LinearLayout firstRow;

  @NonNull
  public final LinearLayout lastRow;

  @NonNull
  public final LinearLayout middleRow;

  @NonNull
  public final TextView titleText;

  private ActivityDevInfoBinding(@NonNull ConstraintLayout rootView, @NonNull LinearLayout firstRow,
      @NonNull LinearLayout lastRow, @NonNull LinearLayout middleRow, @NonNull TextView titleText) {
    this.rootView = rootView;
    this.firstRow = firstRow;
    this.lastRow = lastRow;
    this.middleRow = middleRow;
    this.titleText = titleText;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityDevInfoBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityDevInfoBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_dev_info, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityDevInfoBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.firstRow;
      LinearLayout firstRow = ViewBindings.findChildViewById(rootView, id);
      if (firstRow == null) {
        break missingId;
      }

      id = R.id.lastRow;
      LinearLayout lastRow = ViewBindings.findChildViewById(rootView, id);
      if (lastRow == null) {
        break missingId;
      }

      id = R.id.middleRow;
      LinearLayout middleRow = ViewBindings.findChildViewById(rootView, id);
      if (middleRow == null) {
        break missingId;
      }

      id = R.id.titleText;
      TextView titleText = ViewBindings.findChildViewById(rootView, id);
      if (titleText == null) {
        break missingId;
      }

      return new ActivityDevInfoBinding((ConstraintLayout) rootView, firstRow, lastRow, middleRow,
          titleText);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
