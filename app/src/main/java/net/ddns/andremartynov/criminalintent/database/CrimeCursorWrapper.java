package net.ddns.andremartynov.criminalintent.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import net.ddns.andremartynov.criminalintent.Crime;
import net.ddns.andremartynov.criminalintent.database.CrimeDbSchema.CrimeTable;

import java.util.Date;
import java.util.UUID;

public class CrimeCursorWrapper extends CursorWrapper {

	public CrimeCursorWrapper(Cursor cursor) {
		super(cursor);
	}

	public Crime getCrime() {
		Crime crime = new Crime(UUID.fromString(getString(getColumnIndex(CrimeTable.Cols.UUID))));
		crime.setTitle(getString(getColumnIndex(CrimeTable.Cols.TITLE)));
		crime.setDate(new Date(getLong(getColumnIndex(CrimeTable.Cols.DATE))));
		crime.setSolved(getInt(getColumnIndex(CrimeTable.Cols.SOLVED)) != 0);
		return crime;
	}
}
