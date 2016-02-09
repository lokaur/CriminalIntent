package net.ddns.andremartynov.criminalintent;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

public class PhotoViewFragment extends DialogFragment {

	private static final String ARG_PHOTO = "photo_path";

	private ImageView mPhotoView;

	public static PhotoViewFragment newInstance(File photoFile) {
		Bundle args = new Bundle();
		args.putSerializable(ARG_PHOTO, photoFile);

		PhotoViewFragment fragment = new PhotoViewFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		File photoFile = (File) getArguments().getSerializable(ARG_PHOTO);

		View v = LayoutInflater.from(getActivity())
				.inflate(R.layout.dialog_photo, null);

		mPhotoView = (ImageView) v.findViewById(R.id.dialog_photo_image_view);

		if (photoFile != null) {
			Bitmap photo = PictureUtils.getScaledBitmap(
					photoFile.getPath(), getActivity());
			mPhotoView.setImageBitmap(photo);
		}

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
}
