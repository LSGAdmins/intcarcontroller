package com.lsg.solarsteuerung;



public class db_object /*extends SQLiteOpenHelper*/ {
	public static final String DB_NAME               = "intcar";
	public static final String DB_TABLE              = "devices";
	public static final String DB_DEVICE_NAME        = "device_name";
	public static final String DB_DEVICE_DESCRIPTION = "device_description";
	public static final String DB_STANDARD_DEVICE    = "standard_device";
	public static final String DB_ROWID              = "_id";
	public static final String TAG                   = "intcar";

	/*private static final int DATABASE_VERSION = 1;
	
	private static final String CREATE_DATABASE = "CREATE TABLE " + db_object.DB_TABLE
			+ " (" + db_object.DB_ROWID + " integer primary key autoincrement, "
			+ db_object.DB_DEVICE_NAME + " text not null, "
			+ db_object.DB_STANDARD_DEVICE + " text not null, "
			+ db_object.DB_DEVICE_DESCRIPTION + " text not null) "
			+";";

	public db_object(Context context) {
		super(context, DB_NAME, null, DATABASE_VERSION);
	}

	// Method is called during creation of the database
	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_DATABASE);
	}

	// Method is called during an upgrade of the database,
	// e.g. if you increase the database version
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		Log.w(db_object.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS todo");
		onCreate(database);
	}*/
}