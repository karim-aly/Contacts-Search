package com.example.karim.contactssearch;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

	private EditText nameEditText;
	private ListView contactsListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		nameEditText = (EditText) findViewById(R.id.name);
		Button findButton = (Button) findViewById(R.id.find);

		contactsListView = (ListView) findViewById(R.id.contactsList);

		findButton.setOnClickListener(this);
	}

	public String[] findContactsByName(String name) {
		String[] projection = new String[]{ContactsContract.Contacts._ID};
		String selection = ContactsContract.Contacts.DISPLAY_NAME + " like '" + name + "%' AND "
				+ ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1";
		Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
				projection, selection, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			String[] contactIds = new String[cursor.getCount()];

			int idColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
			for(int i=0; i<contactIds.length; i++) {
				contactIds[i] = cursor.getString(idColumnIndex);
				cursor.moveToNext();
			}

			cursor.close();
			return contactIds;
		}

		return null;
	}

	public ArrayList<String> getPhoneNumbers(String[] contactsIds)
	{
		ArrayList<String> list = new ArrayList<>();
		String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER,
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.TYPE};
		String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " in (";
		for (int i=0; i< contactsIds.length-1; i++) {
			selection += "?,";
		}

		selection += "?)";
		Cursor cursor = getContentResolver().query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection,
				selection, contactsIds, null);

		if(cursor != null && cursor.moveToFirst())
		{
			int numberColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
			int nameColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
			int typeColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
			do {
				String name = cursor.getString(nameColumnIndex);
				String number = cursor.getString(numberColumnIndex);
				String numberType = "Other";
				int type = cursor.getInt(typeColumnIndex);
				switch (type) {
					case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
						numberType = "Home";
						break;
					case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
						numberType = "Mobile";
						break;
					case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
						numberType = "Work";
						break;
				}
				list.add(name + "\n" + numberType + ": " + number);
			} while (cursor.moveToNext());
			cursor.close();
		}

		return list;
	}

	@Override
	public void onClick(View view) {
		String name = nameEditText.getText().toString();
		String[] contactIds = findContactsByName(name);
		if(contactIds != null) {
			contactsListView.setAdapter(new ArrayAdapter<>(
					this, android.R.layout.simple_list_item_1, getPhoneNumbers(contactIds)));
		}
	}
}
