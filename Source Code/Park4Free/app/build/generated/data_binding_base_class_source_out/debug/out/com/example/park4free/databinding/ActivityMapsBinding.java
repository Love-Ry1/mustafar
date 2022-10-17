// Generated by view binder compiler. Do not edit!
package com.example.park4free.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.park4free.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityMapsBinding implements ViewBinding {
  @NonNull
  private final RelativeLayout rootView;

  @NonNull
  public final RecyclerView addressName;

  @NonNull
  public final BottomNavigationView bottomNavigation;

  @NonNull
  public final FloatingActionButton button1;

  @NonNull
  public final SearchView searchView1;

  @NonNull
  public final Switch switch1;

  private ActivityMapsBinding(@NonNull RelativeLayout rootView, @NonNull RecyclerView addressName,
      @NonNull BottomNavigationView bottomNavigation, @NonNull FloatingActionButton button1,
      @NonNull SearchView searchView1, @NonNull Switch switch1) {
    this.rootView = rootView;
    this.addressName = addressName;
    this.bottomNavigation = bottomNavigation;
    this.button1 = button1;
    this.searchView1 = searchView1;
    this.switch1 = switch1;
  }

  @Override
  @NonNull
  public RelativeLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityMapsBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityMapsBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_maps, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityMapsBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.addressName;
      RecyclerView addressName = ViewBindings.findChildViewById(rootView, id);
      if (addressName == null) {
        break missingId;
      }

      id = R.id.bottom_navigation;
      BottomNavigationView bottomNavigation = ViewBindings.findChildViewById(rootView, id);
      if (bottomNavigation == null) {
        break missingId;
      }

      id = R.id.button1;
      FloatingActionButton button1 = ViewBindings.findChildViewById(rootView, id);
      if (button1 == null) {
        break missingId;
      }

      id = R.id.searchView1;
      SearchView searchView1 = ViewBindings.findChildViewById(rootView, id);
      if (searchView1 == null) {
        break missingId;
      }

      id = R.id.switch1;
      Switch switch1 = ViewBindings.findChildViewById(rootView, id);
      if (switch1 == null) {
        break missingId;
      }

      return new ActivityMapsBinding((RelativeLayout) rootView, addressName, bottomNavigation,
          button1, searchView1, switch1);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
