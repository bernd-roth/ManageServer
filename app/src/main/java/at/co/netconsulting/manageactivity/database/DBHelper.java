package at.co.netconsulting.manageactivity.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;
import at.co.netconsulting.manageactivity.general.StaticFields;
import at.co.netconsulting.manageactivity.poj.EntryPoj;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        super(context, StaticFields.DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table wifi " +
                        "(id integer primary key autoincrement, hostname String, groupname String, ip_address String, broadcast String, nic_mac String, comment String, username String, password String, ssh_port String)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS wifi");
        onCreate(db);
    }

    public boolean insertWifi (String hostname, String group_name, String ip_address, String broadcast, String nic_mac, String comment, String username, String password, String ssh_port) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("hostname", hostname);
        contentValues.put("nic_mac", nic_mac);
        contentValues.put("ip_address", ip_address);
        contentValues.put("broadcast", broadcast);
        contentValues.put("groupname", group_name);
        contentValues.put("comment", comment);
        contentValues.put("username", username);
        contentValues.put("password", password);
        contentValues.put("ssh_port", ssh_port);
        db.insert(StaticFields.WIFI_TABLE_NAME, null, contentValues);
        db.close();
        return true;
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, StaticFields.WIFI_TABLE_NAME);
        return numRows;
    }

    public List<EntryPoj> getAllEntries() {
        List<EntryPoj> allEntries = new ArrayList<EntryPoj>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT id, hostname, groupname, ip_address, broadcast, nic_mac, comment, username, password, ssh_port FROM wifi ORDER BY id ASC", null );
        res.moveToFirst();

        while(!res.isAfterLast()){
            String id = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_ID));
            String hostname = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_HOSTNAME));
            String groupname = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_GROUP_NAME));
            String ip_address = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_IP_ADDRESS));
            String broadcast = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_BROADCAST_ADDRESS));
            String nic_mac = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_NIC_MAC));
            String comment = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_COMMENT));
            String username = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_USERNAME));
            String password = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_PASSWORD));
            String ssh_port = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_SSH_PORT));

            allEntries.add(new EntryPoj(Integer.valueOf(id), hostname, groupname, ip_address, broadcast, nic_mac, comment, username, password, ssh_port));

            res.moveToNext();
        }
        return allEntries;
    }

    public void deleteAllEntries () {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ StaticFields.WIFI_TABLE_NAME);
    }

    public void deleteSelectedEntry (EntryPoj entryPoj) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM "+ StaticFields.WIFI_TABLE_NAME + " WHERE hostname = '" + entryPoj.getHostname() + "' AND ip_address = '" + entryPoj.getIp_address() + "';");
        db.close();
    }

    public void update (int id, String hostname, String groupName, String ip, String broadcast, String nicMac, String comment, String username, String password, String ssh_port) {
        // calling a method to get writable database.
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        // on below line we are passing all values
        // along with its key and value pair.
        contentValues.put(StaticFields.WIFI_COLUMN_ID, id);
        contentValues.put(StaticFields.WIFI_COLUMN_HOSTNAME, hostname);
        contentValues.put(StaticFields.WIFI_COLUMN_GROUP_NAME, groupName);
        contentValues.put(StaticFields.WIFI_COLUMN_IP_ADDRESS, ip);
        contentValues.put(StaticFields.WIFI_COLUMN_BROADCAST_ADDRESS, broadcast);
        contentValues.put(StaticFields.WIFI_COLUMN_NIC_MAC, nicMac);
        contentValues.put(StaticFields.WIFI_COLUMN_COMMENT, comment);
        contentValues.put(StaticFields.WIFI_COLUMN_USERNAME, username);
        contentValues.put(StaticFields.WIFI_COLUMN_PASSWORD, password);
        contentValues.put(StaticFields.WIFI_COLUMN_SSH_PORT, ssh_port);

        // on below line we are calling a update method to update our database and passing our values.
        // and we are comparing it with name of our course which is stored in original name variable.
        db.execSQL("UPDATE " + StaticFields.WIFI_TABLE_NAME
                + " SET hostname = '" + hostname
                + "', groupname = '" + groupName
                + "', ip_address = '" + ip
                + "', broadcast = '" + broadcast
                + "', nic_mac = '" + nicMac
                + "', comment = '" + comment
                + "', username = '" + username
                + "', password = '" + password
                + "', ssh_port = '" + ssh_port
                + "' WHERE id = " + id + ";");
//        db.update(WIFI_TABLE_NAME, contentValues, id + " = ? " , new String[]{String.valueOf(id)});
        db.close();
    }

    public List<EntryPoj> getAllEntriesByGroupName(String groupName) {
        List<EntryPoj> allEntries = new ArrayList<EntryPoj>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT id, hostname, groupname, ip_address, broadcast, nic_mac, comment, username, password, ssh_port FROM wifi WHERE groupname = '" + groupName + "' ORDER BY id ASC", null );

        if (res .moveToFirst()) {
            do {
                String id = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_ID));
                String hostname = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_HOSTNAME));
                String groupname = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_GROUP_NAME));
                String ip_address = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_IP_ADDRESS));
                String broadcast = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_BROADCAST_ADDRESS));
                String nic_mac = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_NIC_MAC));
                String comment = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_COMMENT));
                String username = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_USERNAME));
                String password = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_PASSWORD));
                String ssh_port = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_SSH_PORT));

                allEntries.add(new EntryPoj(Integer.valueOf(id), hostname, groupname, ip_address, broadcast, nic_mac, comment, username, password, ssh_port));
            } while (res .moveToNext());
        }
        res .close();
        return allEntries;
    }

    public List<EntryPoj> getSelectedEntry(String hostName, String ip) {
        List<EntryPoj> allEntries = new ArrayList<EntryPoj>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT id, hostname, groupname, ip_address, broadcast, nic_mac, comment FROM wifi WHERE hostname = '" + hostName + "' AND ip_address = '" + ip + "' ORDER BY id ASC", null );

        if (res .moveToFirst()) {
            do {
                String id = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_ID));
                String hostname = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_HOSTNAME));
                String groupname = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_GROUP_NAME));
                String ip_address = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_IP_ADDRESS));
                String broadcast = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_BROADCAST_ADDRESS));
                String nic_mac = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_NIC_MAC));
                String comment = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_COMMENT));
                String username = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_USERNAME));
                String password = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_PASSWORD));
                String ssh_port = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_SSH_PORT));

                allEntries.add(new EntryPoj(Integer.valueOf(id), hostname, groupname, ip_address, broadcast, nic_mac, comment, username, password, ssh_port));
            } while (res .moveToNext());
        }
        res .close();
        return allEntries;
    }

    public List<EntryPoj> exportAllEntries() {
        List<EntryPoj> allEntries = new ArrayList<EntryPoj>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT id, hostname, groupname, ip_address, broadcast, nic_mac, comment, username, password, ssh_port FROM wifi", null );

        if (res .moveToFirst()) {
            do {
                String id = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_ID));
                String hostname = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_HOSTNAME));
                String groupname = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_GROUP_NAME));
                String ip_address = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_IP_ADDRESS));
                String broadcast = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_BROADCAST_ADDRESS));
                String nic_mac = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_NIC_MAC));
                String comment = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_COMMENT));
                String username = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_USERNAME));
                String password = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_PASSWORD));
                String ssh_port = res.getString(res.getColumnIndex(StaticFields.WIFI_COLUMN_SSH_PORT));

                allEntries.add(new EntryPoj(Integer.valueOf(id), hostname, groupname, ip_address, broadcast, nic_mac, comment, username, password, ssh_port));
            } while (res .moveToNext());
        }
        res .close();
        return allEntries;
    }
}