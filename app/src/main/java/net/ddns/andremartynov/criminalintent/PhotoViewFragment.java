package net.ddns.andremartynov.criminalintent;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import java.io.File;

public class PhotoViewFragment extends DialogFragment {

	private static final String ARG_PHOTO = "photo_path";

	private ImageView mPhotoView;
	private File mPhotoFile;

	public static PhotoViewFragment newInstance(File photoFile) {
		Bundle args = new Bundle();
		args.putSerializable(ARG_PHOTO, photoFile);

		PhotoViewFragment fragment = new PhotoViewFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mPhotoFile = (File) getArguments().getSerializable(ARG_PHOTO);

		View v = LayoutInflater.from(getActivity())
				.inflate(R.layout.dialog_photo, null);

		mPhotoView = (ImageView) v.findViewById(R.id.dialog_photo_image_view);
		mPhotoView.getViewTreeObserver()
				.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				updatePhotoView();
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
					mPhotoView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
			}
		});

		mPhotoView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PhotoViewFragment.this.dismiss();
			}
		});

		return new AlertDialog.Builder(getActivity())
				.setView(v)
				.create();
	}

	private void updatePhotoView() {
		if (mPhotoFile == null || !mPhotoFile.exists()) {
			mPhotoView.setImageDrawable(null);
		} else {
			Bitmap bitmap = PictureUtils.getScaledBitmap(
					mPhotoFile.getPath(), mPhotoView.getWidth(), mPhotoView.getHeight());
			mPhotoView.setImageBitmap(bitmap);
		}
	}
}
