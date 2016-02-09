package net.ddns.andremartynov.criminalintent;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.util.Date;
import java.util.UUID;

public class CrimeFragment extends Fragment {
	private static final String ARG_CRIME_ID = "crime_id";
	private static final String DIALOG_DATE = "DialogDate";
	private static final String DIALOG_PHOTO = "DialogPhoto";

	private static final int REQUEST_DATE = 0;
	private static final int REQUEST_CONTACT = 1;
	private static final int REQUEST_PHOTO = 2;

	private Crime mCrime;
	private EditText mTitleField;
	private Button mDateButton;
	private CheckBox mSolvedCheckBox;
	private Button mReportButton;
	private Button mSuspectButton;
	private ImageButton mPhotoButton;
	private ImageView mPhotoView;
	private File mPhotoFile;
	private Point mPhotoViewSize;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
		mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
		mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_crime, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_item_delete_crime:
				CrimeLab.get(getActivity()).deleteCrime(mCrime);
				getActivity().finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		CrimeLab.get(getActivity()).updateCrime(mCrime);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_crime, container, false);

		mTitleField = (EditText) v.findViewById(R.id.crime_title);
		mTitleField.setText(mCrime.getTitle());
		mTitleField.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				mCrime.setTitle(s.toString());
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		mDateButton = (Button) v.findViewById(R.id.crime_date);
		updateDate();
		mDateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FragmentManager fragmentManager = getFragmentManager();
				DatePickerFragment fragment = DatePickerFragment.newInstance(mCrime.getDate());
				fragment.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
				fragment.show(fragmentManager, DIALOG_DATE);
			}
		});

		mSolvedCheckBox = (CheckBox) v.findViewById(R.id.crime_solved);
		mSolvedCheckBox.setChecked(mCrime.isSolved());
		mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mCrime.setSolved(isChecked);
			}
		});

		mReportButton = (Button) v.findViewById(R.id.crime_report);
		mReportButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = ShareCompat.IntentBuilder.from(getActivity())
						.setType("text/plain")
						.setText(getCrimeReport())
						.setSubject(getString(R.string.crime_report_subject))
						.setChooserTitle(getString(R.string.send_report))
						.createChooserIntent();
				startActivity(i);
			}
		});

		final Intent pickContact = new Intent(Intent.ACTION_PICK,
				ContactsContract.Contacts.CONTENT_URI);
		mSuspectButton = (Button) v.findViewById(R.id.crime_suspect);
		mSuspectButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(pickContact, REQUEST_CONTACT);
			}
		});

		if (mCrime.getSuspect() != null) {
			mSuspectButton.setText(mCrime.getSuspect());
		}

		PackageManager packageManager = getActivity().getPackageManager();
		if (packageManager.resolveActivity(pickContact,
				PackageManager.MATCH_DEFAULT_ONLY) == null) {
			mSuspectButton.setEnabled(false);
		}

		mPhotoButton = (ImageButton) v.findViewById(R.id.crime_camera);
		final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		boolean canTakePhoto = mPhotoFile != null &&
				captureImage.resolveActivity(packageManager) != null;
		mPhotoButton.setEnabled(canTakePhoto);

		if (canTakePhoto) {
			Uri uri = Uri.fromFile(mPhotoFile);
			captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		}

		mPhotoButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(captureImage, REQUEST_PHOTO);
			}
		});

		mPhotoView = (ImageView) v.findViewById(R.id.crime_photo);
		mPhotoView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FragmentManager fragmentManager = getFragmentManager();
				PhotoViewFragment fragment = PhotoViewFragment.newInstance(mPhotoFile);
				fragment.show(fragmentManager, DIALOG_PHOTO);
			}
		});

		mPhotoView.getViewTreeObserver()
				.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						updatePhotoView();
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
							mPhotoView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					}
				});

		updatePhotoView();

		return v;
	}

	private void updatePhotoView() {
		if (mPhotoFile == null || !mPhotoFile.exists()) {
			mPhotoView.setImageDrawable(null);
			mPhotoView.setEnabled(false);
		} else {
			Bitmap bitmap = PictureUtils.getScaledBitmap(
					mPhotoFile.getPath(), mPhotoView.getWidth(), mPhotoView.getHeight());
			mPhotoView.setImageBitmap(bitmap);
			mPhotoView.setEnabled(true);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			return;
		}

		if (requestCode == REQUEST_DATE) {
			Date date = (Date) data
					.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
			mCrime.setDate(date);
			updateDate();
		} else if (requestCode == REQUEST_CONTACT && data != null) {
			Uri contactUri = data.getData();
			String[] queryFields = new String[] {
					ContactsContract.Contacts.DISPLAY_NAME
			};

			Cursor cursor = getActivity().getContentResolver()
					.query(contactUri, queryFields, null, null, null);

			try {
				if (cursor.getCount() == 0) {
					return;
				}

				cursor.moveToFirst();
				String suspect = cursor.getString(0);
				mCrime.setSuspect(suspect);
				mSuspectButton.setText(suspect);
			} finally {
				cursor.close();
			}
		} else if (requestCode == REQUEST_PHOTO) {
			updatePhotoView();
		}
	}

	private void updateDate() {
		mDateButton.setText(mCrime.getDate().toString());
	}

	public static CrimeFragment newInstance(UUID crimeId) {
		Bundle args = new Bundle();
		args.putSerializable(ARG_CRIME_ID, crimeId);

		CrimeFragment fragment = new CrimeFragment();
		fragment.setArguments(args);
		return fragment;
	}

	private String getCrimeReport() {
		String solvedString = null;
		if (mCrime.isSolved()) {
			solvedString = getString(R.string.crime_report_solved);
		} else {
			solvedString = getString(R.string.crime_report_unsolved);
		}

		String dateFormat = "EEE, MMM dd";
		String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();

		String suspect = mCrime.getSuspect();
		if (suspect == null) {
			suspect = getString(R.string.crime_report_no_suspect);
		} else {
			suspect = getString(R.string.crime_report_suspect, suspect);
		}

		return getString(R.string.crime_report,
				mCrime.getTitle(), dateString, solvedString, suspect);
	}
}