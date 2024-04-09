// Generated by view binder compiler. Do not edit!
package com.example.project.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.project.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivitySelectBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final AppCompatButton backBtn;

  @NonNull
  public final ImageView cocktailImg;

  @NonNull
  public final TextView cocktailName;

  @NonNull
  public final LinearLayout main;

  @NonNull
  public final TextView textView;

  private ActivitySelectBinding(@NonNull LinearLayout rootView, @NonNull AppCompatButton backBtn,
      @NonNull ImageView cocktailImg, @NonNull TextView cocktailName, @NonNull LinearLayout main,
      @NonNull TextView textView) {
    this.rootView = rootView;
    this.backBtn = backBtn;
    this.cocktailImg = cocktailImg;
    this.cocktailName = cocktailName;
    this.main = main;
    this.textView = textView;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivitySelectBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivitySelectBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_select, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivitySelectBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.backBtn;
      AppCompatButton backBtn = ViewBindings.findChildViewById(rootView, id);
      if (backBtn == null) {
        break missingId;
      }

      id = R.id.cocktail_img;
      ImageView cocktailImg = ViewBindings.findChildViewById(rootView, id);
      if (cocktailImg == null) {
        break missingId;
      }

      id = R.id.cocktail_name;
      TextView cocktailName = ViewBindings.findChildViewById(rootView, id);
      if (cocktailName == null) {
        break missingId;
      }

      LinearLayout main = (LinearLayout) rootView;

      id = R.id.textView;
      TextView textView = ViewBindings.findChildViewById(rootView, id);
      if (textView == null) {
        break missingId;
      }

      return new ActivitySelectBinding((LinearLayout) rootView, backBtn, cocktailImg, cocktailName,
          main, textView);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}