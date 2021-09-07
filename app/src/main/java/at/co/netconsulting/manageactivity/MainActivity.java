package at.co.netconsulting.manageactivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.opencsv.CSVReader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import at.co.netconsulting.manageactivity.database.DBHelper;
import at.co.netconsulting.manageactivity.general.BaseActivity;
import at.co.netconsulting.manageactivity.general.IPAddressFilter;
import at.co.netconsulting.manageactivity.general.MinMaxFilter;
import at.co.netconsulting.manageactivity.general.SharedPreferenceModel;
import at.co.netconsulting.manageactivity.general.StaticFields;
import at.co.netconsulting.manageactivity.poj.EntryPoj;

public class MainActivity extends BaseActivity {

    private int permissionWriteExternalStorage,
            permissionReadExternalStorage,
            permissionAccessWifiState,
            permissionAccessNetworkState,
            permissionInternet;
    private DBHelper dbHelper;
    //    private ImageView imageView;
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout, swipeRefreshLayoutShutdownServer;
    private ListView entryListView, shutdownServerListView;
    private EntryAdapter entryAdapter;
    private EntryAdapterShutdownServer entryAdapterShutdownServer;
    private SharedPreferences sharedPreferences;
    private String magicPacket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set the toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_main);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                entryAdapter.clear();
                entryAdapterShutdownServer.clear();
                getEntriesFromDbOrCsv();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        swipeRefreshLayoutShutdownServer = (SwipeRefreshLayout) findViewById(R.id.refreshLayoutShutdownServer);
        swipeRefreshLayoutShutdownServer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                entryAdapter.clear();
                entryAdapterShutdownServer.clear();
                getEntriesFromDbOrCsv();
                swipeRefreshLayoutShutdownServer.setRefreshing(false);
            }
        });

        if (checkAndRequestPermissions()) {
            //imageView = (ImageView) findViewById(R.id.entry_icon);
            initializeObjects();

            // Populate the list, through the adapter
            getEntriesFromDbOrCsv();

            entryListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                               int position, long id) {
                    openDialog(entryAdapter.getItem(position), position);
                    return true;
                }
            });
            entryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    SharedPreferenceModel sharedPreferenceModel = new SharedPreferenceModel(getApplicationContext());
                    int savedRadioIndex = sharedPreferenceModel.getIntSharedPreference("Server_Or_Group");

                    sharedPreferences = getSharedPreferences("PREFS_PORT", 0);
                    int port = sharedPreferences.getInt("PREFS_PORT", 9);

                    sharedPreferences = getSharedPreferences("PREFS_CHECKBOX_CSV", 0);
                    boolean checkboxCsvEvaluation = sharedPreferences.getBoolean("PREFS_CHECKBOX_CSV", false);

                    EntryPoj entryPoj;
                    sharedPreferences = getSharedPreferences("PREFS_ARP_REQUEST", 0);
                    int arpRequest = sharedPreferences.getInt("PREFS_ARP_REQUEST", 1);

                    if (checkboxCsvEvaluation && savedRadioIndex == 1) {
                        entryPoj = entryAdapter.getItem(position);
                        String ip = entryPoj.getIp_address();
                        String broadcast = entryPoj.getBroadcast();
                        String nicMac = entryPoj.getNic_mac();
                        String groupName = entryPoj.getGroup_name();
                        String hostname = entryPoj.getHostname();
                        for (int i = 0; i < entryAdapter.getCount(); i++) {
                            entryPoj = entryAdapter.getItem(i);
                            String groupNamePosition = entryPoj.getGroup_name();
                            if (groupNamePosition.equals(groupName)) {
                                AsyncTask<Object, Object, Object> send;
                                for (int j = 0; j < arpRequest; j++)
                                    send = new MagicPacket(broadcast, nicMac, port).execute();
                                magicPacket = getString(R.string.magic_packet_group, hostname);
                                Toast.makeText(getApplicationContext(), magicPacket, Toast.LENGTH_LONG).show();
                            }
                        }
                    } else
                        //Only for one server/client to start
                        if (savedRadioIndex == 0) {
                            entryPoj = entryAdapter.getItem(position);
                            String ip = entryPoj.getIp_address();
                            String broadcast = entryPoj.getBroadcast();
                            String nicMac = entryPoj.getNic_mac();
                            String hostname = entryPoj.getHostname();
                            AsyncTask<Object, Object, Object> send;
                            for (int j = 0; j < arpRequest; j++)
                                send = new MagicPacket(broadcast, nicMac, port).execute();
                            magicPacket = getString(R.string.magic_packet_host, hostname);
                            Toast.makeText(getApplicationContext(), magicPacket, Toast.LENGTH_LONG).show();
                            //Start more than 1 server/client
                        } else if (savedRadioIndex == 1 && !checkboxCsvEvaluation) {
                            entryPoj = entryAdapter.getItem(position);
                            List<EntryPoj> listPoj = dbHelper.getAllEntriesByGroupName(entryPoj.getGroup_name());
                            for (int i = 0; i < listPoj.size(); i++) {
                                String ip = listPoj.get(i).getIp_address();
                                String broadcast = listPoj.get(i).getBroadcast();
                                String nicMac = listPoj.get(i).getNic_mac();
                                String group = listPoj.get(i).getGroup_name();
                                AsyncTask<Object, Object, Object> send;
                                for (int j = 0; j < arpRequest; j++)
                                    send = new MagicPacket(broadcast, nicMac, port).execute();
                                magicPacket = getString(R.string.magic_packet_group, group);
                                Toast.makeText(getApplicationContext(), magicPacket, Toast.LENGTH_LONG).show();
                            }
                        }
                }
            });
            // shutdownserver list
            shutdownServerListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    openDialog(entryAdapterShutdownServer.getItem(position), position);
                    return true;
                }
            });

            shutdownServerListView.setOnItemClickListener((parent, view, position, id) -> {
                SharedPreferenceModel sharedPreferenceModel = new SharedPreferenceModel(getApplicationContext());
                int savedRadioIndex = sharedPreferenceModel.getIntSharedPreference(StaticFields.Server_Or_Group);

                sharedPreferences = getSharedPreferences(StaticFields.PREFS_CHECKBOX_CSV, 0);
                boolean checkboxCsvEvaluation = sharedPreferences.getBoolean(StaticFields.PREFS_CHECKBOX_CSV, false);

                EntryPoj entryPoj;

                if (checkboxCsvEvaluation && savedRadioIndex == 1) {
                    entryPoj = entryAdapterShutdownServer.getItem(position);
                    String hostname = entryPoj.getHostname();
                    String ip = entryPoj.getIp_address();
                    String groupName = entryPoj.getGroup_name();
                    String username = entryPoj.getUsername();
                    String password = entryPoj.getPassword();
                    String ssh_port = entryPoj.getSsh_port();
                    for (int i = 0; i < entryAdapterShutdownServer.getCount(); i++) {
                        entryPoj = entryAdapterShutdownServer.getItem(i);
                        String groupNamePosition = entryPoj.getGroup_name();
                        if (groupNamePosition.equals(groupName)) {
                            new AsyncTask<Integer, Void, Void>(){
                                @Override
                                protected Void doInBackground(Integer... params) {
                                    try {
                                        boolean isExecuted = executeSSHcommand(username, password, ssh_port, ip);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    return null;
                                }
                            }.execute(1);
                            Toast.makeText(getApplicationContext(), "Host " + hostname + " was shut down.", Toast.LENGTH_LONG).show();
                        }
                    }
                } else
                    //Only for one server/client to shut down
                    if (savedRadioIndex == 0) {
                        entryPoj = entryAdapterShutdownServer.getItem(position);
                        String hostname = entryPoj.getHostname();
                        String ip = entryPoj.getIp_address();
                        String username = entryPoj.getUsername();
                        String password = entryPoj.getPassword();
                        String ssh_port = entryPoj.getSsh_port();

                        new AsyncTask<Integer, Void, Void>(){
                              @Override
                              protected Void doInBackground(Integer... params) {
                                  try {
                                      boolean isExecuted = executeSSHcommand(username, password, ssh_port, ip);
                                  } catch (Exception e) {
                                      e.printStackTrace();
                                  }
                                  return null;
                              }
                        }.execute(1);
                        Toast.makeText(getApplicationContext(), "Host " + hostname + " was shut down.", Toast.LENGTH_LONG).show();
                        //Shut down more than 1 server/client
                    } else if (savedRadioIndex == 1 && !checkboxCsvEvaluation) {
                        entryPoj = entryAdapterShutdownServer.getItem(position);
                        List<EntryPoj> listPoj = dbHelper.getAllEntriesByGroupName(entryPoj.getGroup_name());
                        for (int i = 0; i < listPoj.size(); i++) {
                            String hostname = listPoj.get(i).getHostname();
                            String ip = listPoj.get(i).getIp_address();
                            String username = listPoj.get(i).getUsername();
                            String password = listPoj.get(i).getPassword();
                            String ssh_port = listPoj.get(i).getSsh_port();

                            new AsyncTask<Integer, Void, Void>() {
                                @Override
                                protected Void doInBackground(Integer... params) {
                                    try {
                                        boolean isExecuted = executeSSHcommand(username, password, ssh_port, ip);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    return null;
                                }
                            }.execute(1);
                            Toast.makeText(getApplicationContext(), "Host " + hostname + " was shut down.", Toast.LENGTH_LONG).show();
                        }
                    }
            });
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_WIFI_STATE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_NETWORK_STATE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.INTERNET)) {
                showDialogOK(R.string.go_to_settings,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        checkAndRequestPermissions();
                                        break;
                                    case DialogInterface.BUTTON_NEGATIVE:
                                        Toast.makeText(getApplicationContext(), R.string.go_to_settings, Toast.LENGTH_LONG).show();
                                        finish();
                                        break;
                                }
                            }
                        });
            } else {
                //permission is denied (and never ask again is checked)
                //shouldShowRequestPermissionRationale will return false
                Toast.makeText(this, R.string.go_to_settings, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private boolean executeSSHcommand(String username, String password, String ssh_port, String ip) {
        boolean isConnected = true;
        int port = Integer.valueOf(ssh_port);
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(username, ip, port);
            session.setPassword(password);

            // Avoid asking for key confirmation
            Properties prop = new Properties();
            prop.put("StrictHostKeyChecking", "no");
            session.setConfig(prop);

            session.connect();

            // SSH Channel
            ChannelExec channelssh = (ChannelExec) session.openChannel("exec");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            channelssh.setOutputStream(baos);

            // Execute command
            channelssh.setCommand("sudo shutdown -h now");
            channelssh.setPty(false);

            channelssh.connect();
            channelssh.disconnect();
            session.disconnect();
        } catch (JSchException JSchEx) {
            return isConnected = false;
        }
        return isConnected;
    }

    private void showDialogOK(int message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

    private void getEntriesFromDbOrCsv() {
        //CSV or DB
        sharedPreferences = getSharedPreferences("PREFS_FILENAME",0);
        String checkboxEvaluation = sharedPreferences.getString("PREFS_FILENAME", "");

        sharedPreferences = getSharedPreferences("PREFS_CHECKBOX_CSV",0);
        boolean checkboxCsvEvaluation = sharedPreferences.getBoolean("PREFS_CHECKBOX_CSV", false);

        //DB
        if(checkboxCsvEvaluation==false){
            List<EntryPoj> listEntryPoj = dbHelper.getAllEntries();
            entryAdapter.addAll(listEntryPoj);
            // shutdownserver list
            List<EntryPoj> listShutdownServerEntryPoj = dbHelper.getAllEntries();
            entryAdapterShutdownServer.addAll(listShutdownServerEntryPoj);
        //CSV
        }else if(checkboxCsvEvaluation && !checkboxEvaluation.equals("")){
            importCSV();
        }
    }

    private void openDialog(EntryPoj entryPoj, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.dialog_hosts, null);
        EditText editTextId = (EditText) dialogView.findViewById(R.id.id);
        EditText editTextHostname = (EditText) dialogView.findViewById(R.id.hostname);
        EditText editTextGroupname = (EditText) dialogView.findViewById(R.id.groupname);
        EditText editTextIpaddress = (EditText) dialogView.findViewById(R.id.ipaddress);
        EditText editTextBroadcast = (EditText) dialogView.findViewById(R.id.broadcast);
        EditText editTextNicMac = (EditText) dialogView.findViewById(R.id.nicmac);
        EditText editTextComment = (EditText) dialogView.findViewById(R.id.comment);
        EditText editTextUsername = (EditText) dialogView.findViewById(R.id.username);
        EditText editTextPassword = (EditText) dialogView.findViewById(R.id.password);
        EditText editTextSSH_Port = (EditText) dialogView.findViewById(R.id.ssh_port);
        editTextSSH_Port.setFilters( new InputFilter[]{ new MinMaxFilter( 1, 65535 )}) ;

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_hosts, null))
                // Add action buttons
                .setPositiveButton(getResources().getString(R.string.update), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        id = entryPoj.getId();
                        String hostname = editTextHostname.getText().toString();
                        String groupName = editTextGroupname.getText().toString();
                        String ip = editTextIpaddress.getText().toString();
                        String broadcast = editTextBroadcast.getText().toString();
                        String nicmac = editTextNicMac.getText().toString();
                        String comment = editTextComment.getText().toString();
                        String username = editTextUsername.getText().toString();
                        String password = editTextPassword.getText().toString();
                        String ssh_port = editTextSSH_Port.getText().toString();

                        dbHelper.update(id, hostname, groupName, ip, broadcast, nicmac, comment, username, password, ssh_port);
                        entryAdapter.clear();
                        entryAdapterShutdownServer.clear();
                        getEntriesFromDbOrCsv();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .setNeutralButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String hostname = editTextHostname.getText().toString();
                        String groupName = editTextGroupname.getText().toString();
                        String ip = editTextIpaddress.getText().toString();
                        String broadcast = editTextBroadcast.getText().toString();
                        String nicmac = editTextNicMac.getText().toString();
                        String comment = editTextComment.getText().toString();
                        String username = editTextComment.getText().toString();
                        String password = editTextComment.getText().toString();
                        String ssh_port = editTextComment.getText().toString();

                        List<EntryPoj> entryPoj = dbHelper.getSelectedEntry(hostname, ip);

                        EntryPoj entryPoj2;
                        for(int id = 0; id<entryPoj.size(); id++) {
                            entryPoj2 = new EntryPoj(id, hostname, groupName, ip, broadcast, nicmac, comment, username, password, ssh_port);
                            dbHelper.deleteSelectedEntry(entryPoj2);
                        }
                        entryAdapter.clear();
                        entryAdapterShutdownServer.clear();
                        getEntriesFromDbOrCsv();
                    }
                });

        builder.setView(dialogView);

        editTextId.setText(String.valueOf(entryPoj.getId()));
        editTextId.setEnabled(false);
        editTextHostname.setText(entryPoj.getHostname());
        editTextGroupname.setText(entryPoj.getGroup_name());
        editTextIpaddress.setText(entryPoj.getIp_address());
           editTextIpaddress.setFilters( new InputFilter[]{ new IPAddressFilter()}) ;
        editTextBroadcast.setText(entryPoj.getBroadcast());
        editTextNicMac.setText(entryPoj.getNic_mac());
        editTextComment.setText(entryPoj.getComment());
        editTextUsername.setText(entryPoj.getUsername());
        editTextPassword.setText(entryPoj.getPassword());
        editTextSSH_Port.setText(entryPoj.getSsh_port());
        editTextSSH_Port.setFilters( new InputFilter[]{ new MinMaxFilter( 0, 65535 )}) ;

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //Initializing objects
    private void initializeObjects() {
        dbHelper = new DBHelper(getApplicationContext());
        // set up the list view for wake on lan
        entryListView = (ListView) findViewById(R.id.list);
        entryAdapter = new EntryAdapter(this, R.layout.entry_list_item);
        entryListView.setAdapter(entryAdapter);
        sharedPreferences = getSharedPreferences("PREFS_RADIOGROUP_SETTINGS",0);

        // set up the list for shutdown server
        shutdownServerListView = (ListView) findViewById(R.id.listshutdown);
        entryAdapterShutdownServer = new EntryAdapterShutdownServer(this, R.layout.shutdownlist_item);
        shutdownServerListView.setAdapter(entryAdapterShutdownServer);
    }

    public void showMenu(MenuItem item) {
        onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        entryAdapter.clear();
        entryAdapterShutdownServer.clear();
        getEntriesFromDbOrCsv();
    }

    public void importCSV() {
        try {
            sharedPreferences = getSharedPreferences("PREFS_FILENAME", 0);
            String csvFileName = sharedPreferences.getString("PREFS_FILENAME", "/server_client.txt");

            String hostname = null, groupName, ip, broadcast, mac, comment, username, password, ssh_port;
            List<EntryPoj> listEntryPoj = new ArrayList<EntryPoj>();
            File csvfile = new File(Environment.getExternalStorageDirectory() + "/" + csvFileName);
            CSVReader reader = new CSVReader(new FileReader(csvfile.getAbsolutePath()));
            String[] nextLine;
            int id = 1;
            while ((nextLine = reader.readNext()) != null) {
                String[] splitLine = nextLine[0].split(";");
                hostname = splitLine[0];
                groupName = splitLine[1];
                ip = splitLine[2];
                broadcast = splitLine[3];
                mac = splitLine[4];
                comment = splitLine[5];
                username = splitLine[6];
                password = splitLine[7];
                ssh_port = splitLine[8];
                EntryPoj poj = new EntryPoj(id, hostname, groupName, ip, broadcast, mac, comment, username, password, ssh_port);
                listEntryPoj.add(poj);
                id++;
            }
            entryAdapter.addAll(listEntryPoj);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.server_file), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkAndRequestPermissions() {
        permissionWriteExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissionReadExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        permissionAccessWifiState = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE);
        permissionAccessNetworkState = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE);
        permissionInternet = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);

        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionWriteExternalStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissionReadExternalStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (permissionAccessWifiState != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_WIFI_STATE);
        }
        if (permissionAccessNetworkState != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_NETWORK_STATE);
        }
        if (permissionInternet != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.INTERNET);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), StaticFields.REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public boolean shouldShowRequestPermissionRationale(@NonNull String permission) {
        return super.shouldShowRequestPermissionRationale(permission);
    }
}